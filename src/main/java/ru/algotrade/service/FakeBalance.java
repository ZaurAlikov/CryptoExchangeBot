package ru.algotrade.service;

import java.math.BigDecimal;
import java.util.Map;

public interface FakeBalance {
    BigDecimal getBalanceBySymbol(String symbol);

    void setBalanceBySymbol(String symbol, BigDecimal value);

    void addBalanceBySymbol(String symbol, BigDecimal value);

    void reduceBalanceBySymbol(String symbol, BigDecimal value);

    BigDecimal getAllBalanceInDollars(Map<String, BigDecimal> prices);

    void resetBalance();

}
