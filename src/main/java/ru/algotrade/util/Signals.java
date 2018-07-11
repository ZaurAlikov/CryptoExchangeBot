package ru.algotrade.util;

import java.math.BigDecimal;
import java.util.List;

import static ru.algotrade.util.CalcUtils.*;

public class Signals {

    public static boolean downUpCrossing(BigDecimal oldFastTickInd, List<BigDecimal> fastTickListInd,
                                         BigDecimal oldSlowTickInd, List<BigDecimal> slowTickListInd) {
        boolean signal = false;
        if (oldFastTickInd.compareTo(oldSlowTickInd) < 0 &&
                fastTickListInd.get(1).compareTo(slowTickListInd.get(1)) < 0 &&
                fastTickListInd.get(2).compareTo(slowTickListInd.get(2)) < 0 &&
                fastTickListInd.get(0).compareTo(slowTickListInd.get(0)) >= 0) {
            signal = true;
        }
        return signal;
    }

    public static boolean rsiSignal(List<BigDecimal> rsi, int rsiMin, int rsiMax){
        boolean signal = false;
        if (rsi.get(0).compareTo(toBigDec(rsiMax)) > 0 &&
                (rsi.get(1).compareTo(toBigDec(rsiMin)) < 0 || rsi.get(2).compareTo(toBigDec(rsiMin)) < 0 ||
                        rsi.get(3).compareTo(toBigDec(rsiMin)) < 0 || rsi.get(4).compareTo(toBigDec(rsiMin)) < 0) && (rsi.get(0).compareTo(rsi.get(1)) > 0 &&
                rsi.get(0).compareTo(rsi.get(2)) > 0 && rsi.get(1).compareTo(rsi.get(2)) > 0 && rsi.get(2).compareTo(rsi.get(3)) > 0)){
            signal = true;
        }
        return signal;
    }
}
