package ru.algotrade.util;

import ru.algotrade.model.Candle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static ru.algotrade.util.CalcUtils.*;

public class MathUtils {

    public static BigDecimal ema(List<Candle> values, BigDecimal curValue, Integer period) {
        BigDecimal ema;
        BigDecimal sma = sma(values, period);
        BigDecimal alpha = divide(toBigDec("2"), toBigDec(period + 1));
        ema = add(multiply(alpha, curValue), multiply(subtract(toBigDec("1"), alpha), sma));
        return ema;
    }

    public static BigDecimal sma(List<Candle> values, Integer period) {
        BigDecimal sma = BigDecimal.ZERO;
        for (int i = 0; i < period; ++i) {
            sma = add(sma, values.get(i).getClose());
        }
        sma = divide(sma, toBigDec(period), 4, RoundingMode.HALF_UP);
        return sma;
    }
}
