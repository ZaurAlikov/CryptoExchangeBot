package ru.algotrade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import ru.algotrade.mapping.TradePairBinanceMapper;
import ru.algotrade.mapping.TradePairBinanceMapperImpl;
import ru.algotrade.service.BalanceCache;
import ru.algotrade.service.ExchangeService;
import ru.algotrade.service.FakeBalance;
import ru.algotrade.service.TradeOperation;
import ru.algotrade.service.impl.binance.BalanceCacheImpl;
import ru.algotrade.service.impl.binance.BinanceTradeOperation;
import ru.algotrade.service.impl.ExchangeServiceImpl;
import ru.algotrade.service.impl.FakeBalanceImpl;

@Configuration
@PropertySource("classpath:settings.properties")
public class AppConfig {
    @Value("${api_key}")
    String apiKey;

    @Value("${secret_key}")
    String secretKey;

    @Bean
    FakeBalance fakeBalance(){
        return new FakeBalanceImpl();
    }

    @Bean
    @DependsOn("balanceCache")
    TradeOperation tradeOperation(){
        return new BinanceTradeOperation(apiKey, secretKey);
    }

    @Bean
    BalanceCache balanceCache(){
        return new BalanceCacheImpl(apiKey, secretKey);
    }

    @Bean
    TradePairBinanceMapper tradePairBinanceMapper(){
        return new TradePairBinanceMapperImpl();
    }

    @Bean
    @DependsOn("tradeOperation")
    ExchangeService exchangeService(){
        return new ExchangeServiceImpl();
    }
}
