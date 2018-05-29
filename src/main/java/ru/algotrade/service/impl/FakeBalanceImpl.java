package ru.algotrade.service.impl;

import ru.algotrade.service.FakeBalance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FakeBalanceImpl implements FakeBalance {
    private Map<String, BigDecimal> accountFakeBalance;
    private int scale = 8;

    public FakeBalanceImpl(List<String> pairs) {
        accountFakeBalance = new TreeMap<>();
        for (String pair : pairs) {
            accountFakeBalance.put(pair, BigDecimal.ZERO);
        }
    }

    @Override
    public BigDecimal getBalanceBySymbol(String symbol) {
        if (accountFakeBalance.size() > 0) {
            return accountFakeBalance.get(symbol);
        } else return BigDecimal.ZERO;
    }

    @Override
    public void setBalanceBySymbol(String symbol, BigDecimal value) {
        BigDecimal normValue = value.setScale(scale, RoundingMode.DOWN);
        accountFakeBalance.put(symbol, normValue);
    }

    @Override
    public void addBalanceBySymbol(String symbol, BigDecimal value) {
        if (accountFakeBalance.size() > 0) {
            BigDecimal normValue = accountFakeBalance.get(symbol).add(value).setScale(scale, RoundingMode.DOWN);
            setBalanceBySymbol(symbol, normValue);
        }
    }

    @Override
    public void reduceBalanceBySymbol(String symbol, BigDecimal value) {
        if (accountFakeBalance.size() > 0) {
            BigDecimal normValue = accountFakeBalance.get(symbol).subtract(value).setScale(scale, RoundingMode.DOWN);
            if (normValue.compareTo(BigDecimal.ZERO) >= 0.0) {
                setBalanceBySymbol(symbol, normValue);
            } else {
                setBalanceBySymbol(symbol, normValue);
                System.err.println("Баланс по " + symbol + " отрицательный!");
            }
        }
    }

    @Override
    public BigDecimal getAllBalanceInDollars(Map<String, BigDecimal> prices) {
        BigDecimal sum = BigDecimal.ZERO;
        if (accountFakeBalance.size() > 0) {
            for (String pairPrice : prices.keySet()) {
                for (String pairBalance : accountFakeBalance.keySet()) {
                    if (pairPrice.equals(pairBalance)) {
                        sum = sum.add(accountFakeBalance.get(pairBalance).multiply(prices.get(pairPrice)));
                        break;
                    }
                }
            }
        }
        return sum;
    }

    @Override
    public void resetBalance() {
        if (accountFakeBalance.size() > 0) {
            for (String pair : accountFakeBalance.keySet()) {
                accountFakeBalance.replace(pair, BigDecimal.ZERO);
            }
        }
    }
}