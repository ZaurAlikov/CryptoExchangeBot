package ru.algotrade;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.algotrade.config.AppConfig;
import ru.algotrade.service.ExchangeService;
import ru.algotrade.service.impl.ExchangeServiceImpl;

public class App {
    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        ExchangeService exchangeService = ((ExchangeServiceImpl)context.getBean("exchangeService"));
        exchangeService.startTrade();
    }
}
