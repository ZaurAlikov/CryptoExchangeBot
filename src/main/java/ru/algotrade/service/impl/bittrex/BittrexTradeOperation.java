package ru.algotrade.service.impl.bittrex;

import de.elbatya.cryptocoins.bittrexclient.BittrexClient;
import de.elbatya.cryptocoins.bittrexclient.api.model.accountapi.Balance;
import de.elbatya.cryptocoins.bittrexclient.api.model.accountapi.Order;
import de.elbatya.cryptocoins.bittrexclient.api.model.common.ApiResult;
import de.elbatya.cryptocoins.bittrexclient.api.model.marketapi.OrderCreated;
import de.elbatya.cryptocoins.bittrexclient.config.ApiCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.algotrade.enums.TradeType;
import ru.algotrade.model.Fee;
import ru.algotrade.model.PairTriangle;
import ru.algotrade.model.TradePair;
import ru.algotrade.service.TradeOperation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static ru.algotrade.util.CalcUtils.*;

public class BittrexTradeOperation implements TradeOperation {

    private static boolean NO_TRADE = false;
    private BigDecimal mainFee;
    private Logger logger = LoggerFactory.getLogger(BittrexTradeOperation.class);

    BittrexClient bittrexClient;

    public BittrexTradeOperation(String apiKey, String secretKey){
        ApiCredentials apiCredentials = new ApiCredentials(apiKey, secretKey);
        bittrexClient = new BittrexClient(apiCredentials);
        mainFee = toBigDec("0.0025");
    }

    @Override
    public BigDecimal buy(String pair, String price, String qty) {
        return null;
    }

    @Override
    public BigDecimal sell(String pair, String price, String qty) {
        return null;
    }

    @Override
    public BigDecimal marketBuy(String pair, String qty, TradeType tradeType) {
        if(tradeType == TradeType.TRADE){
            ApiResult<OrderCreated> result = bittrexClient.getMarketApi().buyLimit(pair, toBigDec(qty), getTradePairInfo(pair).getAskPrice());
            String uuid = result.unwrap().getUuid();
            Order order = bittrexClient.getAccountApi().getOrder(uuid).unwrap();
            logger.debug("Buy " + pair + "paid: " + multiply(order.getPrice(), order.getQuantity()) +  " buy: " +
                    order.getQuantity() + " by price: " + order.getPrice() + " with commission: " + order.getCommissionPaid());
            return order.getQuantity();
        } else if (tradeType == TradeType.TEST) {
            return toBigDec(qty);
        } else if (tradeType == TradeType.PROFIT){
            return toBigDec(qty);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal marketSell(String pair, String qty, TradeType tradeType) {
        if(tradeType == TradeType.TRADE){
            ApiResult<OrderCreated> result = bittrexClient.getMarketApi().sellLimit(pair, toBigDec(qty), getTradePairInfo(pair).getBidPrice());
            String uuid = result.unwrap().getUuid();
            Order order = bittrexClient.getAccountApi().getOrder(uuid).unwrap();
            logger.debug("Sell " + pair + "paid: " + order.getQuantity() + " buy: " + multiply(order.getPrice(), order.getQuantity()) + " by price: " + order.getPrice() + " with commission: " + order.getCommissionPaid());
            return multiply(order.getPrice(), order.getQuantity());
        } else if (tradeType == TradeType.TEST) {
            return multiply(getTradePairInfo(pair).getBidPrice(), qty);
        } else if (tradeType == TradeType.PROFIT){
            return multiply(getTradePairInfo(pair).getBidPrice(), qty);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String getQtyForBuy(String pair, BigDecimal amt, PairTriangle triangle, TradeType tradeType) {
        return null;
    }

    @Override
    public String getQtyForSell(String pair, BigDecimal amt, PairTriangle triangle, TradeType tradeType) {
        return null;
    }

    @Override
    public BigDecimal getBalance(String currency) {
        Balance balance = bittrexClient.getAccountApi().getBalance(currency).unwrap();
        return balance.getAvailable();
    }

    @Override
    public Fee getFee(String spentCurrency, BigDecimal spent) {
        return new Fee(spentCurrency, multiply(spent, mainFee));
    }

    @Override
    public boolean isAllPairTrading(PairTriangle triangle) {
        return false;
    }

    @Override
    public TradePair getTradePairInfo(String pair) {
        return null;
    }

    @Override
    public List<String> getAllPair() {
        return null;
    }

    @Override
    public List<String> getAllCoins() {
        return null;
    }

    @Override
    public Map<String, BigDecimal> getAllPrices() {
        return null;
    }

    @Override
    public boolean isNoTrade() {
        return NO_TRADE;
    }

    @Override
    public void setNoTrade(boolean noTrade) {
        NO_TRADE = noTrade;
    }
}
