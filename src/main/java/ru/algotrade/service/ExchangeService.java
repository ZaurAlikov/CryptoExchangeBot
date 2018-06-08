package ru.algotrade.service;

import ru.algotrade.model.PairTriangle;

import java.math.BigDecimal;
import java.util.List;

public interface ExchangeService {

    void startTrade();

    List<PairTriangle> getAllTriangles(List<String> Pairs, String mainCur);

    boolean isProfit(PairTriangle triangle, BigDecimal initAmt, BigDecimal bound);

    void trade(PairTriangle triangle, BigDecimal initAmt, String mainCur, boolean isTest);
}
