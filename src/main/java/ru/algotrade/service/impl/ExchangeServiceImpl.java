package ru.algotrade.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.algotrade.enums.TradeType;
import ru.algotrade.model.PairTriangle;
import ru.algotrade.service.ExchangeService;
import ru.algotrade.service.FakeBalance;
import ru.algotrade.service.TradeOperation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import  static ru.algotrade.util.CalcUtils.*;
import static ru.algotrade.util.Utils.getRequiredCurrency;
import static ru.algotrade.util.Utils.isBaseCurrency;

@Service
@PropertySource("classpath:settings.properties")
public class ExchangeServiceImpl implements ExchangeService {

    @Value("${main_currency}")
    private String mainCur;
    private TradeType tradeType;
    private TradeOperation tradeOperation;
    private FakeBalance fakeBalance;
    private Logger logger = LoggerFactory.getLogger(ExchangeServiceImpl.class);

    public ExchangeServiceImpl() {
        fakeBalance = new FakeBalanceImpl();
        tradeType = TradeType.TEST;
    }

    @Override
    public void startTrade() {
        fakeBalance.init(tradeOperation.getAllCoins());
        BigDecimal initAmt = new BigDecimal("15");
        BigDecimal bound = new BigDecimal("0.01");
        List<String> allPairs = tradeOperation.getAllPair();
        List<PairTriangle> triangles = getAllTriangles(allPairs, mainCur);
        while (true){
            for (PairTriangle triangle : triangles) {
                fakeBalance.setBalanceBySymbol(mainCur, new BigDecimal("20"));
                if (isProfit(triangle, initAmt, bound)) {
                    trade(triangle, initAmt, mainCur, tradeType);
                }
                fakeBalance.resetBalance();
            }
        }
    }

    @Override
    public boolean isProfit(PairTriangle triangle, BigDecimal initAmt, BigDecimal bound) {
        BigDecimal beforeBal = fakeBalance.getBalanceBySymbol(mainCur);
        trade(triangle, initAmt, mainCur, TradeType.PROFIT);
        BigDecimal afterBal = fakeBalance.getBalanceBySymbol(mainCur);
        BigDecimal profit = divide(multiply(subtract(afterBal, beforeBal), toBigDec("100")), initAmt, 2);
        boolean result = profit.compareTo(bound) >= 0;
        if (result) logger.debug("Profit size: " + subtract(afterBal, beforeBal).toString() + " " + mainCur + " (" + profit + "%)" + triangle);
        return result;
    }

    @Override
    public void trade(PairTriangle triangle, BigDecimal initAmt, String mainCur, TradeType tradeType) {
        BigDecimal resultAmt;
        String requiredCurrency;
        String firstPair = triangle.getFirstPair();
        String secondPair = triangle.getSecondPair();
        String thirdPair = triangle.getThirdPair();
        if (tradeOperation.isAllPairTrading(triangle)) {
            requiredCurrency = getRequiredCurrency(firstPair, mainCur);
            resultAmt = newMarketOrder(firstPair, requiredCurrency, initAmt, tradeType);
            requiredCurrency = getRequiredCurrency(secondPair, requiredCurrency);
            resultAmt = newMarketOrder(secondPair, requiredCurrency, resultAmt, tradeType);
            requiredCurrency = getRequiredCurrency(thirdPair, requiredCurrency);
            resultAmt = newMarketOrder(thirdPair, requiredCurrency, resultAmt, tradeType);
            if (tradeType == TradeType.TRADE) logger.debug("Trade result = " + resultAmt + " " + mainCur);
        } else logger.debug("Trade in one or more pairs is not allowed");
    }

    private BigDecimal newMarketOrder(String pair, String buyCoin, BigDecimal qty, TradeType tradeType) {
        String normalQty;
        BigDecimal resultAmt = BigDecimal.ZERO;
        if (pair.contains(buyCoin)) {
            if (isBaseCurrency(pair, buyCoin)) {
                normalQty = tradeOperation.getQtyForBuy(pair, qty);
                if (normalQty != null) {
                    switch (tradeType){
                        case TRADE:
                            resultAmt = tradeOperation.marketBuy(pair, normalQty, TradeType.TRADE);
                            break;
                        case TEST:
                            resultAmt = tradeOperation.marketBuy(pair, normalQty, TradeType.TEST);
                            fakeBalanceFilling(getRequiredCurrency(pair, buyCoin), qty, buyCoin, toBigDec(normalQty));
                            break;
                        case PROFIT:
                            resultAmt = tradeOperation.marketBuy(pair, normalQty, TradeType.PROFIT);
                            fakeBalanceFilling(getRequiredCurrency(pair, buyCoin), qty, buyCoin, toBigDec(normalQty));
                            break;
                    }
                }
            } else {
                normalQty = tradeOperation.getQtyForSell(pair, qty);
                if (normalQty != null) {
                    switch (tradeType){
                        case TRADE:
                            resultAmt = tradeOperation.marketSell(pair, normalQty, TradeType.TRADE);
                            break;
                        case TEST:
                            resultAmt = tradeOperation.marketSell(pair, normalQty, TradeType.TEST);
                            fakeBalanceFilling(getRequiredCurrency(pair, buyCoin), toBigDec(normalQty), buyCoin, resultAmt);
                            break;
                        case PROFIT:
                            resultAmt = tradeOperation.marketSell(pair, normalQty, TradeType.PROFIT);
                            fakeBalanceFilling(getRequiredCurrency(pair, buyCoin), toBigDec(normalQty), buyCoin, resultAmt);
                            break;
                    }
                }
            }
        } else logger.debug("Pair don`t contain buyCoin");
        return resultAmt;
    }

    private void fakeBalanceFilling(String spentCurrency, BigDecimal spent, String boughtCurrency, BigDecimal bought){
        fakeBalance.reduceBalanceBySymbol(spentCurrency, spent);
        fakeBalance.addBalanceBySymbol(boughtCurrency, bought);
        //TODO Реализовать логику расчета коммиссии
    }

    @Override
    public List<PairTriangle> getAllTriangles(List<String> Pairs, String mainCur) {
        List<String> firstPairs = new ArrayList<>();
        List<String> coins = new ArrayList<>();
        for (String pair : Pairs) {
            if (pair.contains(mainCur)) {
                firstPairs.add(pair);
                coins.add(pair.replace(mainCur, ""));
            }
        }
        List<String> secondPairs = new ArrayList<>();
        for (String coin : coins) {
            for (String coin2 : coins) {
                if (!coin.equals(coin2)) {
                    secondPairs.add(coin.concat(coin2));
                }
            }
        }
        secondPairs.removeIf(s -> !Pairs.contains(s));
        PairTriangle triangle;
        List<PairTriangle> triangles = new ArrayList<>();
        for (String pair1 : firstPairs) {
            for (String pair2 : secondPairs) {
                if (pair2.contains(pair1.replace(mainCur, ""))) {
                    for (String pair3 : firstPairs) {
                        if (pair3.contains(pair2.replace(pair1.replace(mainCur, ""), ""))) {
                            triangle = new PairTriangle(pair1, pair2, pair3, true);
                            triangles.add(triangle);
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
}
