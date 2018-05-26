package ru.algotrade.model;

import java.math.BigDecimal;

public class TradeLimits {

    private String symbol;
    private String minPrice;
    private String maxPrice;
    private String tickSize;
    private String minQty;
    private String maxQty;
    private String stepSize;
    private String minNotional;

    public TradeLimits(String symbol, String minPrice, String maxPrice, String tickSize, String minQty, String maxQty, String stepSize, String minNotional) {
        this.symbol = symbol;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.tickSize = tickSize;
        this.minQty = minQty;
        this.maxQty = maxQty;
        this.stepSize = stepSize;
        this.minNotional = minNotional;
    }

    public TradeLimits(){

    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(String minPrice) {
        this.minPrice = minPrice;
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getTickSize() {
        return tickSize;
    }

    public void setTickSize(String tickSize) {
        this.tickSize = tickSize;
    }

    public String getMinQty() {
        return minQty;
    }

    public void setMinQty(String minQty) {
        this.minQty = minQty;
    }

    public String getMaxQty() {
        return maxQty;
    }

    public void setMaxQty(String maxQty) {
        this.maxQty = maxQty;
    }

    public String getStepSize() {
        return stepSize;
    }

    public void setStepSize(String stepSize) {
        this.stepSize = stepSize;
    }

    public String getMinNotional() {
        return minNotional;
    }

    public void setMinNotional(String minNotional) {
        this.minNotional = minNotional;
    }

    public BigDecimal getMinPriceBigDec(){
        return new BigDecimal(minPrice);
    }
}
