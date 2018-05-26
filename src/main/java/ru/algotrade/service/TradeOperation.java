package ru.algotrade.service;

import ru.algotrade.model.TradePair;

import java.math.BigDecimal;

public interface TradeOperation {
    //TODO Решить что будут возвращать торговые методы
    void buy(String pair, String price, String qty);
    void sell(String pair, String price, String qty);
    void marketBuy(String pair, String qty);
    void marketSell(String pair, String qty);

    BigDecimal getProfit();
    TradePair getTradePairInfo(String pair);
}
