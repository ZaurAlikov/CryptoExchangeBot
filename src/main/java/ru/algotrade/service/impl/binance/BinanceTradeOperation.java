package ru.algotrade.service.impl.binance;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.*;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.general.SymbolStatus;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.exception.BinanceApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.algotrade.enums.TradeType;
import ru.algotrade.mapping.TradePairBinanceMapper;
import ru.algotrade.model.Fee;
import ru.algotrade.model.PairTriangle;
import ru.algotrade.model.TradePair;
import ru.algotrade.service.TradeOperation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static ru.algotrade.util.CalcUtils.*;

@Service
public class BinanceTradeOperation implements TradeOperation {

    private static boolean NO_TRADE = false;

    private BalanceCacheImpl balanceCacheImpl;
    private TradePairBinanceMapper tradePairBinanceMapper;
    private BinanceApiRestClient apiRestClient;
    private BinanceApiAsyncRestClient apiAsyncRestClient;
    private ExchangeInfo exchangeInfo;
    private List<TickerPrice> prices;
    private List<BookTicker> tradeBooks;
    private boolean isBNBFee;
    private BigDecimal BNBFee;
    private BigDecimal mainFee;
    private Logger logger = LoggerFactory.getLogger(BinanceTradeOperation.class);

    public BinanceTradeOperation(String apiKey, String secretKey) {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, secretKey);
        apiRestClient = factory.newRestClient();
        apiAsyncRestClient = factory.newAsyncRestClient();
        exchangeInfo = apiRestClient.getExchangeInfo();
        prices = apiRestClient.getAllPrices();
        tradeBooks = apiRestClient.getBookTickers();
        isBNBFee = true;
        BNBFee = toBigDec("0.0005");
        mainFee = toBigDec("0.001");
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
    public BigDecimal marketBuy(String pair, String qty, TradeType tradeType) {
        if (tradeType == TradeType.TRADE) {
            NewOrderResponse orderResponse = apiRestClient.newOrder(NewOrder.marketBuy(pair, qty).newOrderRespType(NewOrderResponseType.FULL));
            logger.debug("Buy " + pair + " paid: " + getSellResultFromOrderResponse(orderResponse) + " buy: " +
                    orderResponse.getExecutedQty() + " by price: " + getAvgPriceFromOrderResponse(orderResponse) + " with commission: " +
                    getAvgCommissionFromOrderResponse(orderResponse) + " " + orderResponse.getFills().get(0).getCommissionAsset());
            return new BigDecimal(orderResponse.getExecutedQty());

//            List<Trade> tradeList = apiRestClient.getMyTrades(pair, 1);
//            if (tradeList.size() > 0) {
//                Trade trade = tradeList.get(0);
//                if (trade.getOrderId().equals(orderResponse.getOrderId().toString())) {
//                    logger.debug("Buy " + pair + " paid: " + multiply(trade.getPrice(), trade.getQty()) + " buy: " +
//                            trade.getQty() + " by price: " + trade.getPrice() + " with commission: " + trade.getCommission() + " " + trade.getCommissionAsset());
//                } else printAltBuyLog(pair, orderResponse);
//            } else printAltBuyLog(pair, orderResponse);

//            getMyTradesInNewThread(pair, orderResponse);
//            return new BigDecimal(orderResponse.getExecutedQty());

        } else if (tradeType == TradeType.TEST) {
            apiRestClient.newOrderTest(NewOrder.marketBuy(pair, qty));
            return toBigDec(qty);
        } else if (tradeType == TradeType.PROFIT) {
            if (isNotional(toBigDec(qty), pair, "buy")) {
                return toBigDec(qty);
            }
        }
        return BigDecimal.ZERO;
    }

//    private void printAltBuyLog(String pair, NewOrderResponse orderResponse){
//        logger.debug("Buy " + pair + " paid: " + multiply(getTradePairInfo(pair).getAskPrice(), orderResponse.getExecutedQty()) + " buy: " +
//                orderResponse.getExecutedQty() + " by price: " + getTradePairInfo(pair).getAskPrice());
//    }

    @Override
    public BigDecimal marketSell(String pair, String qty, TradeType tradeType) {
        if (tradeType == TradeType.TRADE) {
            NewOrderResponse orderResponse = apiRestClient.newOrder(NewOrder.marketSell(pair, qty).newOrderRespType(NewOrderResponseType.FULL));
            logger.debug("Sell " + pair + " paid: " + orderResponse.getExecutedQty() + " buy: " + getSellResultFromOrderResponse(orderResponse) + " by price: " +
                    getAvgPriceFromOrderResponse(orderResponse) + " with commission: " + getAvgCommissionFromOrderResponse(orderResponse) + " " +
                    orderResponse.getFills().get(0).getCommissionAsset());
            return getSellResultFromOrderResponse(orderResponse);

//            List<Trade> tradeList = apiRestClient.getMyTrades(pair, 1);
//            if (tradeList.size() > 0) {
//                Trade trade = tradeList.get(0);
//                if (trade.getOrderId().equals(orderResponse.getOrderId().toString())) {
//                    logger.debug("Sell " + pair + " paid: " + trade.getQty() + " buy: " + multiply(trade.getPrice(), trade.getQty()) + " by price: " + trade.getPrice() + " with commission: " + trade.getCommission() + " " + trade.getCommissionAsset());
//                    return multiply(trade.getPrice(), trade.getQty());
//                } else logger.debug("Trade with such id not found");
//            } else logger.debug("Could not retrieve trade list");
//            logger.debug("Unsuccessful sell");

        } else if (tradeType == TradeType.TEST) {
            apiRestClient.newOrderTest(NewOrder.marketSell(pair, qty));
            return multiply(getTradePairInfo(pair).getBidPrice(), qty);
        } else if (tradeType == TradeType.PROFIT) {
            if (isNotional(toBigDec(qty), pair, "sell")) return multiply(getTradePairInfo(pair).getBidPrice(), qty);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getAvgPriceFromOrderResponse(NewOrderResponse orderResponse) {
        OptionalDouble average = orderResponse.getFills().stream().mapToDouble(s -> Double.valueOf(s.getPrice())).average();
        return toBigDec(average.getAsDouble()).setScale(8, RoundingMode.DOWN);
    }

    private BigDecimal getAvgCommissionFromOrderResponse(NewOrderResponse orderResponse) {
        OptionalDouble average = orderResponse.getFills().stream().mapToDouble(s -> Double.valueOf(s.getCommission())).average();
        return toBigDec(average.getAsDouble()).setScale(8, RoundingMode.DOWN);
    }

    private BigDecimal getSellResultFromOrderResponse(NewOrderResponse orderResponse) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Trade trade : orderResponse.getFills()) {
            sum = add(sum, multiply(trade.getPrice(), trade.getQty()));
        }
        sum = sum.setScale(8, RoundingMode.DOWN);
        return sum;
    }

    @Override
    public String getQtyForBuy(String pair, BigDecimal amt, PairTriangle triangle, TradeType tradeType) {
        BigDecimal normalQty = normalizeQuantity(pair, divide(amt, getTradePairInfo(pair).getAskPrice()));
        if (PairTriangle.NUM_PAIR == 1) {
            if (getTradePairInfo(triangle.getFirstPair()).getTradeLimits().getStepSize()
                    .compareTo(getTradePairInfo(triangle.getSecondPair()).getTradeLimits().getStepSize()) < 0) {
                normalQty = normalQty.setScale(getTradePairInfo(triangle.getSecondPair()).getTradeLimits().getStepSize().stripTrailingZeros().scale(), RoundingMode.DOWN);
            }
        }
        if (PairTriangle.NUM_PAIR == 2 && tradeType != TradeType.TRADE) {
            if (getTradePairInfo(triangle.getSecondPair()).getTradeLimits().getStepSize()
                    .compareTo(getTradePairInfo(triangle.getThirdPair()).getTradeLimits().getStepSize()) < 0) {
                NO_TRADE = true;
            }
        }
        if (isValidQty(pair, normalQty)) return normalQty.toString();
        else return null;
    }

    @Override
    public String getQtyForSell(String pair, BigDecimal amt, PairTriangle triangle, TradeType tradeType) {
        BigDecimal normalQty = normalizeQuantity(pair, amt);
        if (PairTriangle.NUM_PAIR == 2 && tradeType != TradeType.TRADE) {
            if (getTradePairInfo(triangle.getThirdPair()).getTradeLimits().getStepSize().stripTrailingZeros().scale() <
                    multiply(amt, getTradePairInfo(pair).getBidPrice()).stripTrailingZeros().scale()) {
                NO_TRADE = true;
            }
        }
        if (PairTriangle.NUM_PAIR == 3 && tradeType != TradeType.TRADE) {
            if (getTradePairInfo(triangle.getThirdPair()).getTradeLimits().getStepSize().stripTrailingZeros().scale() <
                    amt.stripTrailingZeros().scale()) {
                NO_TRADE = true;
            }
        }
        if (isValidQty(pair, normalQty)) return normalQty.toString();
        else return null;
    }

    @Override
    public BigDecimal getBalance(String currency) {
        AssetBalance assetBalance = balanceCacheImpl.getAccountBalanceCache().get(currency);
        return toBigDec(assetBalance.getFree());
    }

    @Override
    public Fee getFee(String spentCurrency, BigDecimal spent) {
        if (isBNBFee) {
            if (spentCurrency.equals("BNB")) {
                return new Fee(spentCurrency, multiply(spent, BNBFee));
            }
            for (String pair : getAllPair()) {
                if (getTradePairInfo(pair).getBaseAsset().equals(spentCurrency) && getTradePairInfo(pair).getQuoteAsset().equals("BNB")) {
                    return new Fee("BNB", multiply(spent, getTradePairInfo(pair).getMarketPrice(), BNBFee));
                }
                if (getTradePairInfo(pair).getQuoteAsset().equals(spentCurrency) && getTradePairInfo(pair).getBaseAsset().equals("BNB")) {
                    return new Fee("BNB", multiply(divide(spent, getTradePairInfo(pair).getMarketPrice()), BNBFee));
                }
            }
            return new Fee("BNB", multiply(divide(multiply(spent, getTradePairInfo(spentCurrency.concat("BTC")).getMarketPrice()),
                    getTradePairInfo("BNBBTC").getMarketPrice()), BNBFee));
        }
        return new Fee(spentCurrency, multiply(spent, mainFee));
    }

    @Override
    public boolean isAllPairTrading(PairTriangle triangle) {
        boolean result;
        result = getTradePairInfo(triangle.getFirstPair()).getTradeLimits().getStatus().equals(SymbolStatus.TRADING.name()) &&
                getTradePairInfo(triangle.getSecondPair()).getTradeLimits().getStatus().equals(SymbolStatus.TRADING.name()) &&
                getTradePairInfo(triangle.getFirstPair()).getTradeLimits().getStatus().equals(SymbolStatus.TRADING.name());
        if (!result) NO_TRADE = true;
        return result;
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
        for (SymbolInfo symbolInfo : exchangeInfo.getSymbols()) {
            allPairs.add(symbolInfo.getSymbol());
        }
        return allPairs;
    }

    @Override
    public List<String> getAllCoins() {
        List<String> allCoins = new ArrayList<>();
        String baseAsset;
        String quoteAsset;
        for (SymbolInfo symbolInfo : exchangeInfo.getSymbols()) {
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
        for (TickerPrice tickerPrice : prices) {
            allPrices.put(tickerPrice.getSymbol(), new BigDecimal(tickerPrice.getPrice()));
        }
        return allPrices;
    }

    @Override
    public boolean isNoTrade() {
        return NO_TRADE;
    }

    @Override
    public void setNoTrade(boolean noTrade) {
        NO_TRADE = noTrade;
    }

    private BigDecimal normalizeQuantity(String pair, BigDecimal pairQuantity) {
        BigDecimal step = getTradePairInfo(pair).getTradeLimits().getStepSize();
        pairQuantity = pairQuantity.setScale(step.stripTrailingZeros().scale(), RoundingMode.DOWN);
        return pairQuantity;
    }

    private Boolean isValidQty(String pair, BigDecimal normalQuantity) {
        boolean result;
        BigDecimal minQty = getTradePairInfo(pair).getTradeLimits().getMinQty();
        BigDecimal maxQty = getTradePairInfo(pair).getTradeLimits().getMaxQty();
        result = normalQuantity.compareTo(minQty) > 0 && normalQuantity.compareTo(maxQty) < 0;
        if (!result) NO_TRADE = true;
        return result;
    }

    private boolean isNotional(BigDecimal qty, String pair, String buyOrSell) {
        boolean result = false;
        if (buyOrSell.equals("buy")) {
            result = multiply(qty, getTradePairInfo(pair).getAskPrice()).compareTo(getTradePairInfo(pair).getTradeLimits().getMinNotional()) >= 0;
        }
        if (buyOrSell.equals("sell")) {
            result = multiply(qty, getTradePairInfo(pair).getBidPrice()).compareTo(getTradePairInfo(pair).getTradeLimits().getMinNotional()) >= 0;
        }
        if (!result) NO_TRADE = true;
        return result;
    }

    private TickerPrice getPrice(String pair) {
        return prices.stream().filter(s -> s.getSymbol().equals(pair)).findFirst().orElse(null);
    }

    private BookTicker getTradeBook(String pair) {
        return tradeBooks.stream().filter(s -> s.getSymbol().equals(pair)).findFirst().orElse(null);
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

//    private void getMyTradesInNewThread(String pair, NewOrderResponse orderResponse) {
//        new Thread(() -> {
//            List<Trade> tradeList = apiRestClient.getMyTrades(pair, 1);
//            if (tradeList.size() > 0) {
//                Trade trade = tradeList.get(0);
//                if (trade.getOrderId().equals(orderResponse.getOrderId().toString())) {
//                    logger.debug("Buy " + pair + " paid: " + multiply(trade.getPrice(), trade.getQty()) + " buy: " +
//                            trade.getQty() + " by price: " + trade.getPrice() + " with commission: " + trade.getCommission() + " " + trade.getCommissionAsset());
//                } else logger.debug("Trade with such id not found");
//            } else logger.debug("Could not retrieve trade list");
//        }).start();
//    }

    @Autowired
    public void setTradePairBinanceMapper(TradePairBinanceMapper tradePairBinanceMapper) {
        this.tradePairBinanceMapper = tradePairBinanceMapper;
    }

    @Autowired
    public void setBalanceCacheImpl(BalanceCacheImpl balanceCacheImpl) {
        this.balanceCacheImpl = balanceCacheImpl;
    }
}
