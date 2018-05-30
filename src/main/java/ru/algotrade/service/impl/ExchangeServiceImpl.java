package ru.algotrade.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.algotrade.model.PairTriangle;
import ru.algotrade.model.TradePair;
import ru.algotrade.service.ExchangeService;
import ru.algotrade.service.TradeOperation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Service
public class ExchangeServiceImpl implements ExchangeService {

    @Autowired
    TradeOperation tradeOperation;
    Logger logger = LoggerFactory.getLogger(ExchangeServiceImpl.class);

    @Override
    public void startTrade() {
        logger.debug("CryptoExchangeBot started...");
        TradePair tradePair = tradeOperation.getTradePairInfo("ADABNB");
        System.out.println(tradePair.getTradeLimits().getMinQty());
        List<String> allPairs = tradeOperation.getAllPair();
        List<String> allCoins = tradeOperation.getAllCoins();
        Map<String, BigDecimal> allPrices = tradeOperation.getAllPrices();
        List<PairTriangle> triangles = getAllTriangles(allPairs, "USDT");
        System.out.println(allPairs.get(0));
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

    void newMarketOrder(String pair, String coin, BigDecimal qty){
        if(isBaseCurrency(pair, coin)){

        }

        tradeOperation.marketBuy(pair, qty.toString());
        tradeOperation.marketSell(pair, qty.toString());
    }

    boolean isBaseCurrency(String pair, String coin){
        return pair.startsWith(coin);
    }

}
