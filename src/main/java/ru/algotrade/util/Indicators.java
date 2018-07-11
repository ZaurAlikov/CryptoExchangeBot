package ru.algotrade.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.algotrade.model.Candle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static ru.algotrade.util.CalcUtils.*;

public class Indicators {

    private static Logger logger = LoggerFactory.getLogger(Indicators.class);

    public static BigDecimal ema(List<Candle> candles, BigDecimal curValue, Integer period, Integer numCandle) {
        BigDecimal ema;
        BigDecimal sma = sma(candles, period);
        BigDecimal alpha = divide(toBigDec("2"), toBigDec(period + 1));
        ema = add(multiply(alpha, candles.get(period).getClose()), multiply(subtract(toBigDec("1"), alpha), sma));
        for (int i = period + 1; i < (candles.size() - 1) - numCandle; ++i) {
            ema = add(multiply(alpha, candles.get(i).getClose()), multiply(subtract(toBigDec("1"), alpha), ema));
        }
        if (numCandle > 0) {
            ema = add(multiply(alpha, candles.get(candles.size() - (numCandle + 1)).getClose()), multiply(subtract(toBigDec("1"), alpha), ema));
            ema = ema.setScale(4, RoundingMode.HALF_UP);
            return ema;
        }
        ema = add(multiply(alpha, curValue), multiply(subtract(toBigDec("1"), alpha), ema));
        ema = ema.setScale(4, RoundingMode.HALF_UP);
        return ema;
    }

    public static List<BigDecimal> ema(List<Candle> candles, BigDecimal curValue, Integer period, Integer numCandle, Integer count) {
        List<BigDecimal> emaList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            emaList.add(ema(candles, curValue, period, i));
        }
        return emaList;
    }

    public static BigDecimal sma(List<Candle> candles, Integer period) {
        BigDecimal sma = BigDecimal.ZERO;
        for (int i = 0; i < period; ++i) {
            sma = add(sma, candles.get(i).getClose());
        }
        sma = divide(sma, toBigDec(period), 4, RoundingMode.HALF_UP);
        return sma;
    }

    public static BigDecimal rsi(List<Candle> candles, BigDecimal curValue, Integer period, Integer numCandle) {
        BigDecimal rsi;
        int lastBar = candles.size() - 1;
        if (candles.size() < (period + 1)) {
            String msg = "Quote history length " + candles.size() + " is insufficient to calculate the indicator.";
            logger.debug(msg);
            return BigDecimal.ZERO;
        }
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;
        for (int bar = 1; bar <= period; bar++) {
            BigDecimal change;
            if (bar == lastBar) {
                change = subtract(curValue, candles.get(bar - 1).getClose());
            } else {
                change = subtract(candles.get(bar).getClose(), candles.get(bar - 1).getClose());
            }
            if (change.compareTo(toBigDec("0")) >= 0) {
                avgGain = add(avgGain, change);
            } else {
                avgLoss = add(avgLoss, change);
            }
        }
        avgLoss = toBigDec(Math.abs(avgLoss.doubleValue()));
        avgGain = divide(avgGain, toBigDec(period));
        avgLoss = divide(avgLoss, toBigDec(period));
        for (int bar = period + 1; bar <= lastBar; bar++) {
            BigDecimal change;
            if (bar == lastBar) {
                change = subtract(curValue, candles.get(bar - 1).getClose());
            } else {
                change = subtract(candles.get(bar).getClose(), candles.get(bar - 1).getClose());
            }
            if (change.compareTo(toBigDec("0")) >= 0) {
                avgGain = divide(add(multiply(avgGain, toBigDec(period - 1)), change), toBigDec(period));
                avgLoss = divide(add(multiply(avgLoss, toBigDec(period - 1)), toBigDec("0")), toBigDec(period));
            } else {
                Double absChange = Math.abs(change.doubleValue());
                avgLoss = divide(add(multiply(avgLoss, toBigDec(period - 1)), toBigDec(absChange)), toBigDec(period));
                avgGain = divide(add(multiply(avgGain, toBigDec(period - 1)), toBigDec("0")), toBigDec(period));
            }
            if (bar == (lastBar - numCandle)) {
                break;
            }
        }
        BigDecimal rs = divide(avgGain, avgLoss);
        rsi = subtract(toBigDec("100"), divide(toBigDec("100"), add(toBigDec("1"), rs))).setScale(4, RoundingMode.HALF_UP);
        return rsi;
    }

    public static List<BigDecimal> rsi(List<Candle> candles, BigDecimal curValue, Integer period, Integer numCandle, Integer count) {
        List<BigDecimal> rsiList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rsiList.add(rsi(candles, curValue, period, i));
        }
        return rsiList;
    }
}
