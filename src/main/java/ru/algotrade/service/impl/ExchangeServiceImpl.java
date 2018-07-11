package ru.algotrade.service.impl;

import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.objects.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.algotrade.enums.BetMode;
import ru.algotrade.enums.Interval;
import ru.algotrade.enums.TradeType;
import ru.algotrade.model.Candle;
import ru.algotrade.model.Fee;
import ru.algotrade.model.PairTriangle;
import ru.algotrade.model.ProfitInfo;
import ru.algotrade.service.ExchangeService;
import ru.algotrade.service.FakeBalance;
import ru.algotrade.service.TradeOperation;
import ru.algotrade.service.impl.binance.BinanceTradeOperation;
import ru.algotrade.util.Signals;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.algotrade.util.CalcUtils.*;
import static ru.algotrade.util.Indicators.*;
import static ru.algotrade.util.Signals.downUpCrossing;
import static ru.algotrade.util.Signals.rsiSignal;
import static ru.algotrade.util.Utils.getRequiredCurrency;
import static ru.algotrade.util.Utils.isBaseCurrency;

@Service
@PropertySource("classpath:settings.properties")
public class ExchangeServiceImpl implements ExchangeService {

    @Value("${main_currency}")
    private String mainCur;
    @Value("${vk_token}")
    private String vkToken;
    private BetMode betMode;
    private BigDecimal constBet;
    private BigDecimal percentAmt;
    private BigDecimal bound;
    private PairTriangle currentTriangle;
    private TradeType tradeType;
    private TradeOperation tradeOperation;
    private FakeBalance fakeBalance;
    private ProfitInfo profitInfo;
    private Group group;
    private Map<Integer, ProfitInfo> profitInfoMap;
    private Logger logger = LoggerFactory.getLogger(ExchangeServiceImpl.class);

    @PostConstruct
    private void init() {
//        group = new Group(40542602, vkToken);
    }

    public ExchangeServiceImpl() {
        tradeType = TradeType.TEST;
        betMode = BetMode.CONSTANT;
        profitInfoMap = new HashMap<>();
        constBet = toBigDec("15");
        percentAmt = toBigDec("50");
        bound = toBigDec("0.1");
    }

    @Override
    public void startTrade() {
        List<String> symbols = Arrays.asList("BTCUSDT", "EOSUSDT", "ETHUSDT", "NEOUSDT", "BNBUSDT", "TRXUSDT", "ETCUSDT", "ONTUSDT", "LTCUSDT", "ADAUSDT", "BCCUSDT", "XRPUSDT", "IOTAUSDT", "ICXUSDT", "XLMUSDT", "VENUSDT", "QTUMUSDT");
        tradeOperation.initTradingPairs(symbols, Interval.FIFTEEN_MINUTES, 200);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (String symbol : symbols){
            trade(symbol);
        }



//        fakeBalance.init(tradeOperation.getAllCoins());
//        BigDecimal initAmt;
//        List<String> allPairs = tradeOperation.getAllPair();
//        List<PairTriangle> triangles = getAllTriangles(allPairs);
//
//        while (true) {
//            for (PairTriangle triangle : triangles) {
////                long t1 = System.currentTimeMillis();
//                initAmt = initBet();
//                fakeBalAmtInit(initAmt);
//                tradeOperation.setNoTrade(false);
//                if (isProfit(triangle, initAmt, bound) && !tradeOperation.isNoTrade()) {
//                    trade(triangle, initAmt, mainCur, tradeType);
//                    logger.debug(profitInfoMap.get(1).toString());
//                    logger.debug(profitInfoMap.get(2).toString());
//                    logger.debug(profitInfoMap.get(3).toString());
////                    logger.debug("Profit in " + mainCur + ": " + profitInfo.getMainCurProfit().toString());
////                    logger.debug("Total profit: " + profitInfo.getTotalProfit().toString());
//
//                    String message = profitInfoMap.get(1).toString() + '\n' +
//                            profitInfoMap.get(2).toString() + '\n' +
//                            profitInfoMap.get(3).toString() + '\n' +
//                            "Profit in " + mainCur + ": " + profitInfo.getMainCurProfit().toString() + '\n' +
//                            "Total profit: " + profitInfo.getTotalProfit().toString();
//                    vkBot(message);
////                    logger.debug(message);
////                    logger.debug(String.valueOf(System.currentTimeMillis() - t1));
//                }
//                fakeBalance.resetBalance();
//            }
//        }
    }

    public void trade(String symbol){
        new Thread(() -> {
            BigDecimal initAmt = initBet();
            BigDecimal reqProfit = toBigDec("0.01");
            boolean order = false;
            int rsiMax = 60;
            int rsiMin = 40;
            List<BigDecimal> ema7;
            BigDecimal oldEma7 = new BigDecimal("100");
            List<BigDecimal> ema28;
            BigDecimal oldEma28 = new BigDecimal("0");
            List<BigDecimal> rsi;
            boolean rsiSignal = false;
            boolean crossSignal = false;
            BigDecimal lastPrice;
            BigDecimal buyPrice = BigDecimal.ZERO;
            List<Candle> candles;
            while (true) {
                lastPrice = tradeOperation.getTradePairInfo(symbol).getMarketPrice();
                if (!order){
                    candles = tradeOperation.getTradePairInfo(symbol).getCandles();
                    ema7 = ema(candles, lastPrice, 7, null, 5);
                    ema28 = ema(candles, lastPrice, 28, null, 5);
                    rsi = rsi(candles, lastPrice, 7, null, 6);
                    crossSignal = downUpCrossing(oldEma7, ema7, oldEma28, ema28);
                    oldEma7 = ema7.get(0);
                    oldEma28 = ema28.get(0);
                    rsiSignal = rsiSignal(rsi, rsiMin, rsiMax);

//                    String pattern = "dd.MM.yyyy hh:mm:ss";
//                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//                    String date = simpleDateFormat.format(new Date());
//                    System.out.println(Thread.currentThread().getName() + ", " + symbol + ", " + date + ", " + lastPrice + ", " + ema7.get(0) + ", " + ema28.get(0) + ", " + rsi.get(0));

                }
                if (crossSignal && rsiSignal && !order) {
                    order = true;
                    buyPrice = lastPrice;
                    System.out.println(new Date() + " buy at a price: " +  buyPrice);
                }
                if (order && lastPrice.compareTo(multiply(buyPrice, add(reqProfit, toBigDec("1")))) >= 0){
                    order = false;
                    System.out.println(new Date() + " sell at a price: " +  lastPrice + " 0.9% profit");
                }
                if(order && lastPrice.compareTo(subtract(lastPrice, multiply(buyPrice, reqProfit))) <= 0) {
                    order = false;
                    System.out.println(new Date() + " sell at a price: " +  lastPrice + " -1.1% profit");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public boolean isProfit(PairTriangle triangle, BigDecimal initAmt, BigDecimal bound) {
        BigDecimal beforeTotalBal = fakeBalance.getAllBalanceInMainCur(tradeOperation.getAllPrices());
        BigDecimal beforeBal = fakeBalance.getBalanceBySymbol(mainCur);
        trade(triangle, initAmt, mainCur, TradeType.PROFIT);
        if (tradeOperation.isNoTrade()) return false;
        BigDecimal afterTotalBal = fakeBalance.getAllBalanceInMainCur(tradeOperation.getAllPrices());
        BigDecimal afterBal = fakeBalance.getBalanceBySymbol(mainCur);
        BigDecimal totalProfit = subtract(divide(multiply(afterTotalBal, toBigDec("100")), beforeTotalBal), toBigDec("100"));
        BigDecimal profit = subtract(divide(multiply(afterBal, toBigDec("100")), beforeBal), toBigDec("100"));
        fillProfitInfo(totalProfit, profit);
        boolean result = totalProfit.compareTo(bound) >= 0;
        if (result) {
            logger.debug("Profit in USDT: " + subtract(afterBal, beforeBal).toString() + " " + mainCur + " (" + profit + "%) " + triangle);
            logger.debug("Total profit: " + subtract(afterTotalBal, beforeTotalBal).toString() + " " + mainCur + " (" + totalProfit + "%) " + triangle);
        }
        return result;
    }

    @Override
    public void trade(PairTriangle triangle, BigDecimal initAmt, String mainCur, TradeType tradeType) {
        BigDecimal resultAmt;
        String requiredCurrency;
        currentTriangle = triangle;
        String firstPair = triangle.getFirstPair();
        String secondPair = triangle.getSecondPair();
        String thirdPair = triangle.getThirdPair();

//        if (triangle.getFirstPair().equals("ADAUSDT") && triangle.getSecondPair().equals("ADABTC") && triangle.getThirdPair().equals("BTCUSDT")){
//            System.out.println(" ");
//        }

        if (tradeOperation.isAllPairTrading(triangle)) {
            PairTriangle.NUM_PAIR = 1;
            requiredCurrency = getRequiredCurrency(firstPair, mainCur);
            resultAmt = newMarketOrder(firstPair, requiredCurrency, initAmt, tradeType);
            if (tradeOperation.isNoTrade()) return;
            PairTriangle.NUM_PAIR = 2;
            requiredCurrency = getRequiredCurrency(secondPair, requiredCurrency);
            resultAmt = newMarketOrder(secondPair, requiredCurrency, resultAmt, tradeType);
            if (tradeOperation.isNoTrade()) return;
            PairTriangle.NUM_PAIR = 3;
            requiredCurrency = getRequiredCurrency(thirdPair, requiredCurrency);
            resultAmt = newMarketOrder(thirdPair, requiredCurrency, resultAmt, tradeType);
            if (tradeType == TradeType.TRADE) logger.debug("Trade result = " + resultAmt + " " + mainCur);
        } else logger.debug("Trade in one or more pairs is not allowed");
    }

    private BigDecimal newMarketOrder(String pair, String buyCoin, BigDecimal preQty, TradeType tradeType) {
        BigDecimal qty = preQty;
        String normalQty;
        BigDecimal resultAmt = BigDecimal.ZERO;
        if (isBaseCurrency(pair, buyCoin)) {
            normalQty = tradeOperation.getQtyForBuy(pair, qty, currentTriangle, tradeType);
            if (normalQty != null) {
                if (PairTriangle.NUM_PAIR == 1)
                    qty = multiply(tradeOperation.getTradePairInfo(pair).getAskPrice(), normalQty);
                switch (tradeType) {
                    case TRADE:
                        resultAmt = tradeOperation.marketBuy(pair, normalQty, TradeType.TRADE);
                        break;
                    case TEST:
                        resultAmt = tradeOperation.marketBuy(pair, normalQty, TradeType.TEST);
                        fakeBalanceFilling(getRequiredCurrency(pair, buyCoin), qty, buyCoin, toBigDec(normalQty));
                        break;
                    case PROFIT:
                        resultAmt = tradeOperation.marketBuy(pair, normalQty, TradeType.PROFIT);
                        if (tradeOperation.isNoTrade()) break;
                        fakeBalanceFilling(getRequiredCurrency(pair, buyCoin), qty, buyCoin, toBigDec(normalQty));
                        logger.trace("profit.buy " + PairTriangle.NUM_PAIR + " pair: " + pair + ", Qty: " + qty + ", normalQty: " + normalQty + ", получено: " + resultAmt + ", " + buyCoin + ", по askPrice: " + tradeOperation.getTradePairInfo(pair).getAskPrice());
                        break;
                }
            }
        } else {
            normalQty = tradeOperation.getQtyForSell(pair, qty, currentTriangle, tradeType);
            if (normalQty != null) {
                switch (tradeType) {
                    case TRADE:
                        resultAmt = tradeOperation.marketSell(pair, normalQty, TradeType.TRADE);
                        break;
                    case TEST:
                        resultAmt = tradeOperation.marketSell(pair, normalQty, TradeType.TEST);
                        fakeBalanceFilling(getRequiredCurrency(pair, buyCoin), toBigDec(normalQty), buyCoin, resultAmt);
                        break;
                    case PROFIT:
                        resultAmt = tradeOperation.marketSell(pair, normalQty, TradeType.PROFIT);
                        if (tradeOperation.isNoTrade()) break;
                        fakeBalanceFilling(getRequiredCurrency(pair, buyCoin), toBigDec(normalQty), buyCoin, resultAmt);
                        logger.trace("profit.sell " + PairTriangle.NUM_PAIR + " pair: " + pair + ", Qty: " + qty + ", normalQty: " + normalQty + ", получено: " + resultAmt + ", " + buyCoin + ", по bidPrice: " + tradeOperation.getTradePairInfo(pair).getBidPrice());
                        break;
                }
            }
        }
        return resultAmt;
    }

    @Override
    public List<PairTriangle> getAllTriangles(List<String> pairs) {
        Map<String, List<String>> pairsAndCoins = getFirstPairsAndCoins(pairs);
        List<String> firstPairs = pairsAndCoins.get("firstPairs");
        List<String> coins = pairsAndCoins.get("coins");
        List<String> secondPairs = getSecondPairs(coins, pairs);
        return getPairTriangles(firstPairs, secondPairs);
    }

    private BigDecimal initBet() {
        if (betMode == BetMode.CONSTANT) {
            return constBet;
        } else if (betMode == BetMode.PERCENT) {
            if (percentAmt.compareTo(toBigDec("1")) <= 0) {
                return multiply(tradeOperation.getBalance(mainCur), percentAmt);
            } else logger.error("The bid can not be more than 100%");
        }
        return BigDecimal.ZERO;
    }

    private void fakeBalanceFilling(String spentCurrency, BigDecimal spent, String boughtCurrency, BigDecimal bought) {
        fakeBalance.reduceBalanceBySymbol(spentCurrency, spent);
        fakeBalance.addBalanceBySymbol(boughtCurrency, bought);
        Fee fee = tradeOperation.getFee(spentCurrency, spent);
        fakeBalance.reduceBalanceBySymbol(fee.getSimbol(), fee.getFee());
        ProfitInfo profitInfo = new ProfitInfo(spentCurrency, boughtCurrency, spent, bought, fee);
        profitInfoMap.put(PairTriangle.NUM_PAIR, profitInfo);
    }

    private void fillProfitInfo(BigDecimal totalProfit, BigDecimal mainCurProfit) {
        profitInfo = new ProfitInfo();
        profitInfo.setTotalProfit(totalProfit);
        profitInfo.setMainCurProfit(mainCurProfit);
    }

    private void fakeBalAmtInit(BigDecimal initAmt) {
        fakeBalance.setBalanceBySymbol(mainCur, multiply(initAmt, "1.2"));
        if (tradeOperation instanceof BinanceTradeOperation) {
            fakeBalance.setBalanceBySymbol("BNB", new BigDecimal("0.5"));
        }
    }

    private Map<String, List<String>> getFirstPairsAndCoins(List<String> pairs) {
        Map<String, List<String>> pairsAndCoins = new TreeMap<>();
        List<String> firstPairs = new ArrayList<>();
        List<String> coins = new ArrayList<>();
        for (String pair : pairs) {
            if (pair.contains(mainCur)) {
                firstPairs.add(pair);
                coins.add(pair.replace(mainCur, ""));
            }
        }
        pairsAndCoins.put("firstPairs", firstPairs);
        pairsAndCoins.put("coins", coins);
        return pairsAndCoins;
    }

    private List<String> getSecondPairs(List<String> coins, List<String> pairs) {
        List<String> secondPairs = new ArrayList<>();
        for (String coin : coins) {
            for (String coin2 : coins) {
                if (!coin.equals(coin2)) {
                    secondPairs.add(coin.concat(coin2));
                }
            }
        }
        secondPairs.removeIf(s -> !pairs.contains(s));
        return secondPairs;
    }

    private List<PairTriangle> getPairTriangles(List<String> firstPairs, List<String> secondPairs) {
        List<PairTriangle> triangles = new ArrayList<>();
        for (String pair1 : firstPairs) {
            for (String pair2 : secondPairs) {
                if (pair2.contains(pair1.replace(mainCur, ""))) {
                    for (String pair3 : firstPairs) {
                        if (pair3.contains(pair2.replace(pair1.replace(mainCur, ""), ""))) {
                            triangles.add(new PairTriangle(pair1, pair2, pair3));
                            break;
                        }
                    }
                }
            }
        }
        logger.debug("Created " + triangles.size() + " triangles");
        return triangles;
    }

    private void vkBot(String msg) {
        new Message().from(group).to(3364333).text(msg).send();
    }

    @Autowired
    public void setTradeOperation(TradeOperation tradeOperation) {
        this.tradeOperation = tradeOperation;
    }

    @Autowired
    public void setFakeBalance(FakeBalance fakeBalance) {
        this.fakeBalance = fakeBalance;
    }
}
