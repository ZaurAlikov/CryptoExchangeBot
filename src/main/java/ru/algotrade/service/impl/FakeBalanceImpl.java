package ru.algotrade.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import ru.algotrade.service.FakeBalance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import  static ru.algotrade.util.CalcUtils.*;

@Service
@PropertySource("classpath:settings.properties")
public class FakeBalanceImpl implements FakeBalance {

    @Value("${main_currency}")
    private String mainCur;
    private Map<String, BigDecimal> accountFakeBalance;
    private int scale = 8;
    private Logger logger = LoggerFactory.getLogger(FakeBalanceImpl.class);

    public FakeBalanceImpl(){
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
                logger.error(symbol + " balance is negative!");
            }
        }
    }

    @Override
    public BigDecimal getAllBalanceInMainCur(Map<String, BigDecimal> prices) {
        BigDecimal sum = BigDecimal.ZERO;
        if (accountFakeBalance.size() > 0) {
            for (String coin : accountFakeBalance.keySet()) {
                if(!coin.equals(mainCur) && accountFakeBalance.get(coin).compareTo(BigDecimal.ZERO) > 0){
                    for (String pair : prices.keySet()) {
                        if (pair.contains(coin) && pair.contains(mainCur)) {
                            sum = add(sum, multiply(accountFakeBalance.get(coin), prices.get(pair)));
                            break;
                        }
                    }
                }
            }
            sum = add(sum, accountFakeBalance.get(mainCur));
        }
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
