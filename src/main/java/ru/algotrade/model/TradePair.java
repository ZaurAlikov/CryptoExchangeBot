package ru.algotrade.model;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

public class TradePair {

    private String symbol;
    private String baseAsset;
    private String quoteAsset;
    private BigDecimal askPrice;
    private BigDecimal askQty;
    private BigDecimal bidPrice;
    private BigDecimal bidQty;
    private BigDecimal marketPrice;
    private TradeLimits tradeLimits;
    private List<Candle> candles;

    public TradePair(String symbol, String baseAsset, String quoteAsset, BigDecimal askPrice, BigDecimal askQty, BigDecimal bidPrice, BigDecimal bidQty, BigDecimal marketPrice, TradeLimits tradeLimits) {
        this.symbol = symbol;
        this.baseAsset = baseAsset;
        this.quoteAsset = quoteAsset;
        this.askPrice = askPrice;
        this.askQty = askQty;
        this.bidPrice = bidPrice;
        this.bidQty = bidQty;
        this.marketPrice = marketPrice;
        this.tradeLimits = tradeLimits;
    }

    public TradePair(){
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getBaseAsset() {
        return baseAsset;
    }

    public void setBaseAsset(String baseAsset) {
        this.baseAsset = baseAsset;
    }

    public String getQuoteAsset() {
        return quoteAsset;
    }

    public void setQuoteAsset(String quoteAsset) {
        this.quoteAsset = quoteAsset;
    }

    public BigDecimal getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(BigDecimal askPrice) {
        this.askPrice = askPrice;
    }

    public BigDecimal getAskQty() {
        return askQty;
    }

    public void setAskQty(BigDecimal askQty) {
        this.askQty = askQty;
    }

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(BigDecimal bidPrice) {
        this.bidPrice = bidPrice;
    }

    public BigDecimal getBidQty() {
        return bidQty;
    }

    public void setBidQty(BigDecimal bidQty) {
        this.bidQty = bidQty;
    }

    public BigDecimal getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }

    public TradeLimits getTradeLimits() {
        return tradeLimits;
    }

    public void setTradeLimits(TradeLimits tradeLimits) {
        this.tradeLimits = tradeLimits;
    }

    public BigDecimal getSpread(){
        return askPrice.subtract(bidPrice);
    }

    public List<Candle> getCandles() {
        return candles;
    }

    public void setCandles(List<Candle> candles) {
        this.candles = candles;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
