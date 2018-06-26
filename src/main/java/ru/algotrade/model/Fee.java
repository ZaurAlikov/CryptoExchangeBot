package ru.algotrade.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Fee {

    private String simbol;

    private BigDecimal fee;

    public Fee(String simbol, BigDecimal fee) {
        this.simbol = simbol;
        this.fee = fee.setScale(8, RoundingMode.DOWN);;
    }

    public String getSimbol() {
        return simbol;
    }

    public void setSimbol(String simbol) {
        this.simbol = simbol;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee.setScale(8, RoundingMode.DOWN);;
    }

    @Override
    public String toString() {
        return "Fee{Symbol: "+simbol+", Fee: "+fee+"}";
    }
}
