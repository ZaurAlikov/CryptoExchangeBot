package ru.algotrade.util;

import ru.algotrade.model.Candle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static ru.algotrade.util.CalcUtils.*;

public class MathUtils {

    public static BigDecimal ema(List<Candle> values, BigDecimal curValue, Integer period, Integer numCandle) {
        BigDecimal ema;
        BigDecimal sma = sma(values, period);
        BigDecimal alpha = divide(toBigDec("2"), toBigDec(period + 1));
        ema = add(multiply(alpha, values.get(period).getClose()), multiply(subtract(toBigDec("1"), alpha), sma));
        for (int i = period + 1; i < (values.size() - 1) - numCandle; ++i) {
            ema = add(multiply(alpha, values.get(i).getClose()), multiply(subtract(toBigDec("1"), alpha), ema));
        }
        if (numCandle > 0) {
            ema = add(multiply(alpha, values.get(values.size() - (numCandle + 1)).getClose()), multiply(subtract(toBigDec("1"), alpha), ema));
            ema = ema.setScale(4, RoundingMode.HALF_UP);
            return ema;
        }
        ema = add(multiply(alpha, curValue), multiply(subtract(toBigDec("1"), alpha), ema));
        ema = ema.setScale(4, RoundingMode.HALF_UP);
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
