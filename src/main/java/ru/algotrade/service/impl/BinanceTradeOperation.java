package ru.algotrade.service.impl;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Trade;
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

import  static ru.algotrade.util.CalcUtils.*;

@Service
public class BinanceTradeOperation implements TradeOperation {

    private TradePairBinanceMapper tradePairBinanceMapper;
    private BinanceApiRestClient apiRestClient;
    private Logger logger = LoggerFactory.getLogger(BinanceTradeOperation.class);

    public BinanceTradeOperation(String apiKey, String secretKey){
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, secretKey);
        apiRestClient = factory.newRestClient();
    }

    @Override
    public BigDecimal buy(String pair, String price, String qty) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal sell(String pair, String price, String qty) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal marketBuy(String pair, String qty) {
        NewOrderResponse orderResponse = apiRestClient.newOrder(NewOrder.marketBuy(pair, qty));
        List<Trade> tradeList = apiRestClient.getMyTrades(pair, 1);
        if (tradeList.size() > 0) {
            Trade trade = tradeList.get(0);
            if (trade.getOrderId().equals(orderResponse.getOrderId().toString())) {
                logger.debug("Buy " + pair + " - price: " + trade.getPrice() + ", qty: " + trade.getQty());
                return new BigDecimal(orderResponse.getExecutedQty());
            } else return BigDecimal.ZERO;
        }
        logger.debug("Unsuccessful buy");
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal marketSell(String pair, String qty) {
        NewOrderResponse orderResponse = apiRestClient.newOrder(NewOrder.marketSell(pair, qty));
        List<Trade> tradeList = apiRestClient.getMyTrades(pair, 1);
        if (tradeList.size() > 0) {
            Trade trade = tradeList.get(0);
            if (trade.getOrderId().equals(orderResponse.getOrderId().toString())) {
                logger.debug("Sell " + pair + " - price: " + trade.getPrice() + ", qty: " + trade.getQty() + ", total: " + multiply(trade.getPrice(), trade.getQty()));
                return multiply(trade.getPrice(), trade.getQty());
            } else return BigDecimal.ZERO;
        }
        logger.debug("Unsuccessful sell");
        return BigDecimal.ZERO;
    }

    @Override
    public String getQtyForBuy(String pair, BigDecimal amt) {
        return null;
    }

    @Override
    public String getQtyForSell(String pair, BigDecimal amt) {
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

    @Autowired
    public void setTradePairBinanceMapper(TradePairBinanceMapper tradePairBinanceMapper) {
        this.tradePairBinanceMapper = tradePairBinanceMapper;
    }
}
