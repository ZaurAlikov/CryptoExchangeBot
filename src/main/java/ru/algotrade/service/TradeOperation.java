package ru.algotrade.service;

import ru.algotrade.enums.TradeType;
import ru.algotrade.model.Fee;
import ru.algotrade.model.PairTriangle;
import ru.algotrade.model.TradePair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TradeOperation {

    BigDecimal buy(String pair, String price, String qty);

    BigDecimal sell(String pair, String price, String qty);

    BigDecimal marketBuy(String pair, String qty, TradeType tradeType);

    BigDecimal marketSell(String pair, String qty, TradeType tradeType);

    String getQtyForBuy(String pair, BigDecimal amt, PairTriangle triangle);

    String getQtyForSell(String pair, BigDecimal amt, PairTriangle triangle);

    Fee getFee(String spentCurrency, BigDecimal spent);

    boolean isAllPairTrading(PairTriangle triangle);

    TradePair getTradePairInfo(String pair);

    List<String> getAllPair();

    List<String> getAllCoins();

    Map<String, BigDecimal> getAllPrices();

    boolean isNoTrade();

    void setNoTrade(boolean noTrade);
}
