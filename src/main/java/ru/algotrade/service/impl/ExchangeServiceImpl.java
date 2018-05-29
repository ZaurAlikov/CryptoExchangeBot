package ru.algotrade.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.algotrade.model.TradePair;
import ru.algotrade.service.ExchangeService;
import ru.algotrade.service.TradeOperation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

        List<String> firstPairs  = new ArrayList<>();
        for (String pair : allPairs){
            if(pair.contains("USDT")){
                firstPairs.add(pair);
            }
        }
        System.out.println(allPairs.get(0));
    }
}
