package ru.algotrade.model;

import java.math.BigDecimal;

public class TradeLimits {

    private String symbol;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal tickSize;
    private BigDecimal minQty;
    private BigDecimal maxQty;
    private BigDecimal stepSize;
    private BigDecimal minNotional;

    public TradeLimits(String symbol, String minPrice, String maxPrice, String tickSize, String minQty, String maxQty, String stepSize, String minNotional) {
        this.symbol = symbol;
        this.minPrice = new BigDecimal(minPrice);
        this.maxPrice = new BigDecimal(maxPrice);
        this.tickSize = new BigDecimal(tickSize);
        this.minQty = new BigDecimal(minQty);
        this.maxQty = new BigDecimal(maxQty);
        this.stepSize = new BigDecimal(stepSize);
        this.minNotional = new BigDecimal(minNotional);
    }

    public TradeLimits(){

    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getTickSize() {
        return tickSize;
    }

    public void setTickSize(BigDecimal tickSize) {
        this.tickSize = tickSize;
    }

    public BigDecimal getMinQty() {
        return minQty;
    }

    public void setMinQty(BigDecimal minQty) {
        this.minQty = minQty;
    }

    public BigDecimal getMaxQty() {
        return maxQty;
    }

    public void setMaxQty(BigDecimal maxQty) {
        this.maxQty = maxQty;
    }

    public BigDecimal getStepSize() {
        return stepSize;
    }

    public void setStepSize(BigDecimal stepSize) {
        this.stepSize = stepSize;
    }

    public BigDecimal getMinNotional() {
        return minNotional;
    }

    public void setMinNotional(BigDecimal minNotional) {
        this.minNotional = minNotional;
    }
}
