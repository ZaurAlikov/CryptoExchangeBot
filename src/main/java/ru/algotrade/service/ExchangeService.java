package ru.algotrade.service;

import ru.algotrade.model.PairTriangle;

import java.util.List;

public interface ExchangeService {
    void startTrade();
    List<PairTriangle> getAllTriangles(List<String> Pairs, String mainCur);
}
