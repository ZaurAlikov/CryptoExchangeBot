package ru.algotrade.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.algotrade.enums.BetMode;
import ru.algotrade.enums.TradeType;
import ru.algotrade.model.Fee;
import ru.algotrade.model.PairTriangle;
import ru.algotrade.service.ExchangeService;
import ru.algotrade.service.FakeBalance;
import ru.algotrade.service.TradeOperation;
import ru.algotrade.service.impl.binance.BinanceTradeOperation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Stream;

import static ru.algotrade.util.CalcUtils.*;
import static ru.algotrade.util.Utils.getRequiredCurrency;
import static ru.algotrade.util.Utils.isBaseCurrency;

@Service
@PropertySource("classpath:settings.properties")
public class ExchangeServiceImpl implements ExchangeService {

    @Value("${main_currency}")
    private String mainCur;
    private BetMode betMode;
    private BigDecimal constBet;
    private BigDecimal percentAmt;
    private BigDecimal bound;
    private PairTriangle currentTriangle;
    private TradeType tradeType;
    private TradeOperation tradeOperation;
    private FakeBalance fakeBalance;
    private Logger logger = LoggerFactory.getLogger(ExchangeServiceImpl.class);

    public ExchangeServiceImpl() {
        tradeType = TradeType.TRADE;
        betMode = BetMode.CONSTANT;
        constBet = toBigDec("15");
        percentAmt = toBigDec("50");
        bound = toBigDec("0.2");
    }

    @Override
    public void startTrade() {
        fakeBalance.init(tradeOperation.getAllCoins());
        BigDecimal initAmt;
        List<String> allPairs = tradeOperation.getAllPair();
        List<PairTriangle> triangles = getAllTriangles(allPairs);

        while (true) {
            for (PairTriangle triangle : triangles) {
                initAmt = initBet();
                fakeBalAmtInit(initAmt);
                tradeOperation.setNoTrade(false);
                if (isProfit(triangle, initAmt, bound) && !tradeOperation.isNoTrade()) {
                    trade(triangle, initAmt, mainCur, tradeType);
//                    int i = 2;
                }
                fakeBalance.resetBalance();
            }
        }
    }

    @Override
    public boolean isProfit(PairTriangle triangle, BigDecimal initAmt, BigDecimal bound) {
        BigDecimal beforeBal = fakeBalance.getAllBalanceInMainCur(tradeOperation.getAllPrices());
        trade(triangle, initAmt, mainCur, TradeType.PROFIT);
        if (tradeOperation.isNoTrade()) return false;
        BigDecimal afterBal = fakeBalance.getAllBalanceInMainCur(tradeOperation.getAllPrices());
        BigDecimal profit = subtract(divide(multiply(afterBal, toBigDec("100")), beforeBal), toBigDec("100"));
        boolean result = profit.compareTo(bound) >= 0;
        if (result) logger.debug("Profit size: " + subtract(afterBal, beforeBal).toString() + " " + mainCur + " (" + profit + "%) " + triangle);
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

//        if (triangle.getFirstPair().equals("ETCUSDT") && triangle.getSecondPair().equals("ETCBTC") && triangle.getThirdPair().equals("BTCUSDT")){
//            System.out.println("asdas");
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
        if (betMode == BetMode.CONSTANT){
            return constBet;
        } else if (betMode == BetMode.PERCENT){
            if (percentAmt.compareTo(toBigDec("1")) <= 0){
                return multiply(tradeOperation.getBalance(mainCur), percentAmt);
            }
            else logger.error("The bid can not be more than 100%");
        }
        return BigDecimal.ZERO;
    }

    private void fakeBalanceFilling(String spentCurrency, BigDecimal spent, String boughtCurrency, BigDecimal bought) {
        fakeBalance.reduceBalanceBySymbol(spentCurrency, spent);
        fakeBalance.addBalanceBySymbol(boughtCurrency, bought);
        Fee fee = tradeOperation.getFee(spentCurrency, spent);
        fakeBalance.reduceBalanceBySymbol(fee.getSimbol(), fee.getFee());
    }

    private void fakeBalAmtInit(BigDecimal initAmt) {
        fakeBalance.setBalanceBySymbol(mainCur, multiply(initAmt, "1.2"));
        if (tradeOperation instanceof BinanceTradeOperation){
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

    @Autowired
    public void setTradeOperation(TradeOperation tradeOperation) {
        this.tradeOperation = tradeOperation;
    }

    @Autowired
    public void setFakeBalance(FakeBalance fakeBalance) {
        this.fakeBalance = fakeBalance;
    }
}
