package ru.algotrade.service;

import ru.algotrade.model.TradePair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TradeOperation {
    //TODO Решить что будут возвращать торговые методы
    void buy(String pair, String price, String qty);
    void sell(String pair, String price, String qty);
    void marketBuy(String pair, String qty);
    void marketSell(String pair, String qty);

    BigDecimal getProfit();
    TradePair getTradePairInfo(String pair);
    List<String> getAllPair();
    List<String> getAllCoins();
    Map<String, BigDecimal> getAllPrices();
}
