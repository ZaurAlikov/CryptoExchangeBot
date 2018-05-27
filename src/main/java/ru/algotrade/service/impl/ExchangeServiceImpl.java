package ru.algotrade.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.algotrade.model.TradePair;
import ru.algotrade.service.ExchangeService;
import ru.algotrade.service.TradeOperation;

@Service
public class ExchangeServiceImpl implements ExchangeService {

    @Autowired
    TradeOperation tradeOperation;

    @Override
    public void startTrade() {
        TradePair tradePair = tradeOperation.getTradePairInfo("BTCUSDT");
        System.out.println(tradePair.getTradeLimits().getMinQty());
    }
}
