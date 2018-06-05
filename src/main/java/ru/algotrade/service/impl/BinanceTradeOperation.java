package ru.algotrade.service.impl;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.general.SymbolStatus;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.exception.BinanceApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.algotrade.mapping.TradePairBinanceMapper;
import ru.algotrade.model.PairTriangle;
import ru.algotrade.model.TradePair;
import ru.algotrade.service.TradeOperation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import  static ru.algotrade.util.CalcUtils.*;

@Service
@PropertySource("classpath:settings.properties")
public class BinanceTradeOperation implements TradeOperation {

    private TradePairBinanceMapper tradePairBinanceMapper;
    private BinanceApiRestClient apiRestClient;
    private BinanceApiAsyncRestClient apiAsyncRestClient;
    private ExchangeInfo exchangeInfo;
    private List<TickerPrice> prices;
    private List<BookTicker> tradeBooks;
    private Logger logger = LoggerFactory.getLogger(BinanceTradeOperation.class);

    public BinanceTradeOperation(String apiKey, String secretKey){
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, secretKey);
        apiRestClient = factory.newRestClient();
        apiAsyncRestClient = factory.newAsyncRestClient();
        startRefreshingPrices();
        startRefreshingTradeBook();
        startRefreshingExchangeInfo();
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
    public BigDecimal marketBuy(String pair, String qty, boolean isTest) {
        if(!isTest){
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
        } else {
            apiRestClient.newOrderTest(NewOrder.marketBuy(pair, qty));
            return toBigDec(qty);
        }
    }

    @Override
    public BigDecimal marketSell(String pair, String qty, boolean test) {
        if(!test){
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
        } else {
            apiRestClient.newOrderTest(NewOrder.marketSell(pair, qty));
            return multiply(getTradePairInfo(pair).getBidPrice(), qty);
        }
    }

    @Override
    public String getQtyForBuy(String pair, BigDecimal amt) {
        BigDecimal normalQty = normalizeQuantity(pair, divide(amt, getTradePairInfo(pair).getAskPrice()));
        //TODO Необходимо как-то избавиться от этой проверки
        if(pair.equals("QTUMUSDT")) normalQty = downGrade(normalQty);
        if(isValidQty(pair, normalQty)) return normalQty.toString();
        else return null;
    }

    @Override
    public String getQtyForSell(String pair, BigDecimal amt) {
        BigDecimal normalQty = normalizeQuantity(pair, amt);
        if(isValidQty(pair, normalQty)) return normalQty.toString();
        else return null;
    }

    @Override
    public BigDecimal fee() {
        return new BigDecimal("0.0005");
    }

    @Override
    public boolean isAllPairTrading(PairTriangle triangle) {
        return getTradePairInfo(triangle.getFirstPair()).getTradeLimits().getSymbol().equals(SymbolStatus.TRADING.name()) &&
        getTradePairInfo(triangle.getSecondPair()).getTradeLimits().getSymbol().equals(SymbolStatus.TRADING.name()) &&
        getTradePairInfo(triangle.getFirstPair()).getTradeLimits().getSymbol().equals(SymbolStatus.TRADING.name());
    }

    @Override
    public TradePair getTradePairInfo(String pair) {
        SymbolInfo symbolInfo = exchangeInfo.getSymbolInfo(pair);
        TickerPrice tickerPrice = getPrice(pair);
        BookTicker tradeBook = getTradeBook(pair);
        return tradePairBinanceMapper.toTradePair(symbolInfo, tickerPrice, tradeBook);
    }

    @Override
    public List<String> getAllPair() {
        List<String> allPairs = new ArrayList<>();
        for (SymbolInfo symbolInfo : exchangeInfo.getSymbols()){
            allPairs.add(symbolInfo.getSymbol());
        }
        return allPairs;
    }

    @Override
    public List<String> getAllCoins() {
        List<String> allCoins = new ArrayList<>();
        String baseAsset;
        String quoteAsset;
        for (SymbolInfo symbolInfo : exchangeInfo.getSymbols()){
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
        for (TickerPrice tickerPrice : prices){
            allPrices.put(tickerPrice.getSymbol(), new BigDecimal(tickerPrice.getPrice()));
        }
        return allPrices;
    }

    private BigDecimal normalizeQuantity(String pair, BigDecimal pairQuantity) {
        BigDecimal step = getTradePairInfo(pair).getTradeLimits().getStepSize();
        pairQuantity = pairQuantity.setScale(step.stripTrailingZeros().scale(), RoundingMode.DOWN);
        return pairQuantity;
    }

    private BigDecimal downGrade(BigDecimal pairQuantity) {
        pairQuantity = pairQuantity.setScale(pairQuantity.scale() - 1, RoundingMode.DOWN);
        return pairQuantity;
    }

    private Boolean isValidQty(String pair, BigDecimal normalQuantity) {
        BigDecimal minQty = getTradePairInfo(pair).getTradeLimits().getMinQty();
        BigDecimal maxQty = getTradePairInfo(pair).getTradeLimits().getMaxQty();
        return normalQuantity.compareTo(minQty) > 0 && normalQuantity.compareTo(maxQty) < 0;
    }

    private TickerPrice getPrice(String pair) {
        return prices.stream().filter(s -> s.getSymbol().equals(pair)).findFirst().get();
    }

    private BookTicker getTradeBook(String pair) {
        return tradeBooks.stream().filter(s -> s.getSymbol().equals(pair)).findFirst().get();
    }

    private void startRefreshingTradeBook() {
        new Thread(() -> {
            while (true) {
                int timeoutCount = 0;
                do {
                    try {
                        apiAsyncRestClient.getBookTickers((List<BookTicker> response) -> tradeBooks = response);
                        break;
                    } catch (BinanceApiException e) {
                        ++timeoutCount;
                        logger.error("Something went wrong when retrieving data from the server");
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                } while (timeoutCount <= 100);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        }).start();
    }

    private void startRefreshingPrices() {
        new Thread(() -> {
            while (true) {
                int timeoutCount = 0;
                do {
                    try {
                        apiAsyncRestClient.getAllPrices((List<TickerPrice> response) -> prices = response);
                        break;
                    } catch (BinanceApiException e) {
                        ++timeoutCount;
                        logger.error("Something went wrong when retrieving data from the server");
                        System.err.println("");
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                } while (timeoutCount <= 100);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        }).start();
    }

    private void startRefreshingExchangeInfo() {
        new Thread(() -> {
            while (true) {
                int timeoutCount = 0;
                do {
                    try {
                        apiAsyncRestClient.getExchangeInfo((ExchangeInfo response) -> exchangeInfo = response);
                        break;
                    } catch (BinanceApiException e) {
                        ++timeoutCount;
                        logger.error("Something went wrong when retrieving data from the server");
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                } while (timeoutCount <= 100);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        }).start();
    }

    @Autowired
    public void setTradePairBinanceMapper(TradePairBinanceMapper tradePairBinanceMapper) {
        this.tradePairBinanceMapper = tradePairBinanceMapper;
    }
}
