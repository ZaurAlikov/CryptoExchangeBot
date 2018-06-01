package ru.algotrade.service;

import ru.algotrade.model.PairTriangle;
import ru.algotrade.model.TradePair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TradeOperation {
    //TODO Решить что будут возвращать торговые методы
    BigDecimal buy(String pair, String price, String qty);

    BigDecimal sell(String pair, String price, String qty);

    BigDecimal marketBuy(String pair, String qty);

    BigDecimal marketSell(String pair, String qty);

    BigDecimal getQtyForBuy(BigDecimal amt, String pair);

    BigDecimal getQtyForSell(BigDecimal amt, String pair);

    TradePair getTradePairInfo(String pair);

    List<String> getAllPair();

    List<String> getAllCoins();

    Map<String, BigDecimal> getAllPrices();
}
