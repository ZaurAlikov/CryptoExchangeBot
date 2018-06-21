package ru.algotrade.model;

import java.math.BigDecimal;

public class Fee {

    private String simbol;

    private BigDecimal fee;

    public Fee(String simbol, BigDecimal fee) {
        this.simbol = simbol;
        this.fee = fee;
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
        this.fee = fee;
    }

    @Override
    public String toString() {
        return "Fee{Symbol: "+simbol+", Fee: "+fee+"}";
    }
}
