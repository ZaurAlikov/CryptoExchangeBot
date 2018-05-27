package ru.algotrade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.algotrade.mapping.TradePairBinanceMapper;
import ru.algotrade.mapping.binance.TradePairBinanceMapperImpl;
import ru.algotrade.service.ExchangeService;
import ru.algotrade.service.TradeOperation;
import ru.algotrade.service.impl.BinanceTradeOperation;
import ru.algotrade.service.impl.ExchangeServiceImpl;

@Configuration
@PropertySource("classpath:settings.properties")
public class AppConfig {
    @Value("${api_key}")
    String apiKey;

    @Value("${secret_key}")
    String secretKey;

    @Bean
    ExchangeService exchangeService(){
        return new ExchangeServiceImpl();
    }

    @Bean
    TradeOperation tradeOperation(){
        return new BinanceTradeOperation(apiKey, secretKey);
    }

    @Bean
    TradePairBinanceMapper tradePairBinanceMapper(){
        return new TradePairBinanceMapperImpl();
    }

}