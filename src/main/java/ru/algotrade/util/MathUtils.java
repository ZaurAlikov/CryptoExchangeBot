package ru.algotrade.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static ru.algotrade.util.CalcUtils.*;

public class MathUtils {
    private static int i = 0;

    public static BigDecimal ema(BigDecimal[] values, BigDecimal curValue, Integer period) {
        BigDecimal ema = BigDecimal.ZERO;
        BigDecimal alpha;
        BigDecimal smaSum = BigDecimal.ZERO;

        i = ++i;

        if (values != null && (i-1) <= (values.length - period)) {
            alpha = divide(toBigDec("2"), toBigDec(period + 1));
            ema = add(multiply(alpha, curValue), multiply(subtract(toBigDec("1"), alpha), ema(values, values[i-1], period)));
        }


//        if (value != null && value.length > 1) {
//            for (int i = 0; i < value.length; ++i) {
//                smaSum = add(smaSum, divide(value[i], toBigDec(value.length), 4, RoundingMode.HALF_UP));
//            }
//        } else return null;


        return ema;
    }
}
