package ru.algotrade.service.impl;

import ru.algotrade.service.FakeBalance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import  static ru.algotrade.util.CalcUtils.*;

public class FakeBalanceImpl implements FakeBalance {
    private Map<String, BigDecimal> accountFakeBalance;
    private int scale = 8;

    FakeBalanceImpl(){
    }

    public void init(List<String> coins) {
        accountFakeBalance = new TreeMap<>();
        for (String coin : coins) {
            accountFakeBalance.put(coin, BigDecimal.ZERO);
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
            if (normValue.compareTo(BigDecimal.ZERO) >= 0) {
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
            for (String coin : accountFakeBalance.keySet()) {
                if(!coin.equals("USDT") && accountFakeBalance.get(coin).compareTo(BigDecimal.ZERO) > 0){
                    for (String pair : prices.keySet()) {
                        if (pair.contains(coin) && pair.contains("USDT")) {
                            sum = add(sum, multiply(accountFakeBalance.get(coin), prices.get(pair)));
                            break;
                        }
                    }
                }
            }
            sum = add(sum, accountFakeBalance.get("USDT"));
        }



        accountFakeBalance.entrySet().forEach(e -> {
            if(!e.getKey().equals("USDT") && !e.getKey().equals("BNB") && e.getValue().compareTo(BigDecimal.ZERO) > 0){
                System.out.println("123");
            }
        });



        return sum;
    }

    @Override
    public void resetBalance() {
        if (accountFakeBalance.size() > 0) {
            for (String coin : accountFakeBalance.keySet()) {
                accountFakeBalance.replace(coin, BigDecimal.ZERO);
            }
        }
    }
}
