package ru.algotrade.service;

import ru.algotrade.enums.TradeType;
import ru.algotrade.model.PairTriangle;

import java.math.BigDecimal;
import java.util.List;

public interface ExchangeService {

    void startTrade();

    List<PairTriangle> getAllTriangles(List<String> Pairs);

    boolean isProfit(PairTriangle triangle, BigDecimal bound);

    void trade(PairTriangle triangle, BigDecimal initAmt, String mainCur, TradeType tradeType);
}
