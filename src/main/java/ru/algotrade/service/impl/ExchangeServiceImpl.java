package ru.algotrade.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.algotrade.model.PairTriangle;
import ru.algotrade.service.ExchangeService;
import ru.algotrade.service.TradeOperation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource("classpath:settings.properties")
public class ExchangeServiceImpl implements ExchangeService {

    @Value("${main_currency}")
    private String mainCur;
    private TradeOperation tradeOperation;
    private Logger logger = LoggerFactory.getLogger(ExchangeServiceImpl.class);

    @Override
    public void startTrade() {
        BigDecimal initAmt = new BigDecimal("15");
        BigDecimal bound = new BigDecimal("0.01");
        List<String> allPairs = tradeOperation.getAllPair();
        List<PairTriangle> triangles = getAllTriangles(allPairs, mainCur);
        for(PairTriangle triangle : triangles){
            if(isProfit(triangle, initAmt, bound)){
                trade(triangle, initAmt, mainCur);
            }
        }
//        logger.debug("CryptoExchangeBot started...");
//        TradePair tradePair = tradeOperation.getTradePairInfo("ADABNB");
//        System.out.println(tradePair.getTradeLimits().getMinQty());
//        List<String> allCoins = tradeOperation.getAllCoins();
//        Map<String, BigDecimal> allPrices = tradeOperation.getAllPrices();
//        newMarketOrder(tradePair.getSymbol(), tradePair.getSymbol().replace(mainCur,""), BigDecimal.TEN);
//        System.out.println(allPairs.get(0));
    }

    @Override
    public List<PairTriangle> getAllTriangles(List<String> Pairs, String mainCur){
        List<String> firstPairs = new ArrayList<>();
        List<String> coins = new ArrayList<>();
        for (String pair : Pairs){
            if(pair.contains(mainCur)){
                firstPairs.add(pair);
                coins.add(pair.replace(mainCur,""));
            }
        }
        List<String> secondPairs  = new ArrayList<>();
        for (String coin : coins){
            for (String coin2 : coins){
                if(!coin.equals(coin2)){
                    secondPairs.add(coin.concat(coin2));
                }
            }
        }
        secondPairs.removeIf(s -> !Pairs.contains(s));
        PairTriangle triangle;
        List<PairTriangle> triangles = new ArrayList<>();
        for (String pair1 : firstPairs){
            for (String pair2 : secondPairs){
                if(pair2.contains(pair1.replace(mainCur,""))){
                    for (String pair3 : firstPairs){
                        if(pair3.contains(pair2.replace(pair1.replace(mainCur,""),""))){
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

    @Override
    public void trade(PairTriangle triangle, BigDecimal initAmt, String mainCur){
        BigDecimal resultAmt;
        String requiredCurrency;
        String firstPair = triangle.getFirstPair();
        String secondPair = triangle.getSecondPair();
        String thirdPair = triangle.getThirdPair();

        requiredCurrency = getRequiredCurrency(firstPair, mainCur);
        resultAmt = newMarketOrder(firstPair, requiredCurrency, initAmt);

        requiredCurrency = getRequiredCurrency(secondPair, requiredCurrency);
        resultAmt = newMarketOrder(secondPair, requiredCurrency, resultAmt);

        requiredCurrency = getRequiredCurrency(thirdPair, requiredCurrency);
        resultAmt = newMarketOrder(thirdPair, requiredCurrency, resultAmt);

        logger.debug("Trade result = " + resultAmt + " " + mainCur);
    }

    @Override
    public boolean isProfit(PairTriangle triangle, BigDecimal initAmt, BigDecimal bound) {
        return true;
    }

    private BigDecimal newMarketOrder(String pair, String buyCoin, BigDecimal qty){
        String normalQty;
        BigDecimal resultAmt = BigDecimal.ZERO;
        if(pair.contains(buyCoin)){
            if (isBaseCurrency(pair, buyCoin)) {
                normalQty = tradeOperation.getQtyForBuy(pair, qty);
                resultAmt = tradeOperation.marketBuy(pair, normalQty);
            } else {
                normalQty = tradeOperation.getQtyForSell(pair, qty);
                resultAmt = tradeOperation.marketSell(pair, normalQty);
            }
        } else logger.debug("Pair don`t contain buyCoin");
        return  resultAmt;
    }

    private boolean isBaseCurrency(String pair, String coin){
        return pair.startsWith(coin);
    }

    private String getRequiredCurrency(String pair, String availableCurrency ){
        return pair.replace(availableCurrency, "");
    }

    @Autowired
    public void setTradeOperation(TradeOperation tradeOperation) {
        this.tradeOperation = tradeOperation;
    }
}
