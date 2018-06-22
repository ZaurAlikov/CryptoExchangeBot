package ru.algotrade.service.impl.binance;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import org.springframework.stereotype.Service;
import ru.algotrade.service.BalanceCache;

import java.util.Map;
import java.util.TreeMap;

import static com.binance.api.client.domain.event.UserDataUpdateEvent.UserDataUpdateEventType.ACCOUNT_UPDATE;

@Service
public class BalanceCacheImpl implements BalanceCache{

    private final String listenKey;
    private final BinanceApiClientFactory clientFactory;
    private Map<String, AssetBalance> accountBalanceCache;

    public BalanceCacheImpl(String apiKey, String secret) {
        this.clientFactory = BinanceApiClientFactory.newInstance(apiKey, secret);
        this.listenKey = initializeAssetBalanceCacheAndStreamSession();
        startAccountBalanceEventStreaming(listenKey);
    }

    private String initializeAssetBalanceCacheAndStreamSession() {
        BinanceApiRestClient client = clientFactory.newRestClient();
        Account account = client.getAccount();
        this.accountBalanceCache = new TreeMap<>();
        for (AssetBalance assetBalance : account.getBalances()) {
            accountBalanceCache.put(assetBalance.getAsset(), assetBalance);
        }
        return client.startUserDataStream();
    }

    private void startAccountBalanceEventStreaming(String listenKey) {
        BinanceApiWebSocketClient client = clientFactory.newWebSocketClient();
        client.onUserDataUpdateEvent(listenKey, response -> {
            if (response.getEventType() == ACCOUNT_UPDATE) {
                // Override cached asset balances
                for (AssetBalance assetBalance : response.getAccountUpdateEvent().getBalances()) {
                    accountBalanceCache.put(assetBalance.getAsset(), assetBalance);
                }
                System.out.println(accountBalanceCache);
            }
        });
    }

    @Override
    public Map<String, AssetBalance> getAccountBalanceCache() {
        return accountBalanceCache;
    }
}
