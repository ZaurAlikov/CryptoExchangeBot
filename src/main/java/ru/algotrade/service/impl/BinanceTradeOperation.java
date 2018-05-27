package ru.algotrade.service.impl;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.domain.market.TickerPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.algotrade.mapping.TradePairBinanceMapper;
import ru.algotrade.model.TradePair;
import ru.algotrade.service.TradeOperation;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BinanceTradeOperation implements TradeOperation {

    @Autowired
    private TradePairBinanceMapper tradePairBinanceMapper;
    private BinanceApiRestClient apiRestClient;

    public BinanceTradeOperation(String apiKey, String secretKey){
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, secretKey);
        apiRestClient = factory.newRestClient();
    }

    @Override
    public void buy(String pair, String price, String qty) {

    }

    @Override
    public void sell(String pair, String price, String qty) {

    }

    @Override
    public void marketBuy(String pair, String qty) {

    }

    @Override
    public void marketSell(String pair, String qty) {

    }

    @Override
    public BigDecimal getProfit() {
        return null;
    }

    @Override
    public TradePair getTradePairInfo(String pair) {
        SymbolInfo symbolInfo = apiRestClient.getExchangeInfo().getSymbolInfo(pair);
        TickerPrice tickerPrice = apiRestClient.getPrice(pair);
        List<BookTicker> bookTickers = apiRestClient.getBookTickers();
        BookTicker tradeBook = bookTickers.stream().filter(s -> s.getSymbol().equals(pair)).findFirst().get();
        return tradePairBinanceMapper.toTradePair(symbolInfo, tickerPrice, tradeBook);
    }
}
