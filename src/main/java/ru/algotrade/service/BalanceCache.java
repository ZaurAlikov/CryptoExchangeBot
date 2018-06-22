package ru.algotrade.service;

import com.binance.api.client.domain.account.AssetBalance;

import java.util.Map;

public interface BalanceCache {
    Map<String, AssetBalance> getAccountBalanceCache();
}
