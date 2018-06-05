package ru.algotrade.util;

public class Utils {

    public static boolean isBaseCurrency(String pair, String coin){
        return pair.startsWith(coin);
    }

    public static String getRequiredCurrency(String pair, String availableCurrency ){
        return pair.replace(availableCurrency, "");
    }
}
