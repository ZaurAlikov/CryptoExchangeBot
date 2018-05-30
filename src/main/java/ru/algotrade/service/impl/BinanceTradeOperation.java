package ru.algotrade.service.impl;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.domain.market.TickerPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.algotrade.mapping.TradePairBinanceMapper;
import ru.algotrade.model.TradePair;
import ru.algotrade.service.TradeOperation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class BinanceTradeOperation implements TradeOperation {

    @Autowired
    private TradePairBinanceMapper tradePairBinanceMapper;
    private BinanceApiRestClient apiRestClient;
    Logger logger = LoggerFactory.getLogger(BinanceTradeOperation.class);

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
        logger.debug("Buy at market price");
    }

    @Override
    public void marketSell(String pair, String qty) {
        logger.debug("Sell at market price");
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

    @Override
    public List<String> getAllPair() {
        List<String> allPairs = new ArrayList<>();
        for (SymbolInfo symbolInfo : apiRestClient.getExchangeInfo().getSymbols()){
            allPairs.add(symbolInfo.getSymbol());
        }
        return allPairs;
    }

    @Override
    public List<String> getAllCoins() {
        List<String> allCoins = new ArrayList<>();
        String baseAsset;
        String quoteAsset;
        for (SymbolInfo symbolInfo : apiRestClient.getExchangeInfo().getSymbols()){
            baseAsset = symbolInfo.getBaseAsset();
            quoteAsset = symbolInfo.getQuoteAsset();
            if (!allCoins.contains(baseAsset)) allCoins.add(baseAsset);
            if (!allCoins.contains(quoteAsset)) allCoins.add(quoteAsset);
        }
        return allCoins;
    }

    @Override
    public Map<String, BigDecimal> getAllPrices() {
        Map<String, BigDecimal> allPrices = new TreeMap<>();
        for (TickerPrice tickerPrice : apiRestClient.getAllPrices()){
            allPrices.put(tickerPrice.getSymbol(), new BigDecimal(tickerPrice.getPrice()));
        }
        return allPrices;
    }


}
