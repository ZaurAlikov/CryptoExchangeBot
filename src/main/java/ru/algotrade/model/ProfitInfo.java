package ru.algotrade.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProfitInfo {

    private String sellCur;
    private String buyCur;
    private BigDecimal sellAmt;
    private BigDecimal buyAmt;
    private BigDecimal totalProfit;
    private BigDecimal mainCurProfit;
    private Fee fee;

    public ProfitInfo(){}

    public ProfitInfo(String sellCur, String buyCur, BigDecimal sellAmt, BigDecimal buyAmt, Fee fee) {
        this.sellCur = sellCur;
        this.buyCur = buyCur;
        this.sellAmt = sellAmt.setScale(8, RoundingMode.DOWN);
        this.buyAmt = buyAmt.setScale(8, RoundingMode.DOWN);
        this.fee = fee;
    }

    public String getSellCur() {
        return sellCur;
    }

    public void setSellCur(String sellCur) {
        this.sellCur = sellCur;
    }

    public String getBuyCur() {
        return buyCur;
    }

    public void setBuyCur(String buyCur) {
        this.buyCur = buyCur;
    }

    public BigDecimal getSellAmt() {
        return sellAmt;
    }

    public void setSellAmt(BigDecimal sellAmt) {
        this.sellAmt = sellAmt.setScale(8, RoundingMode.DOWN);
    }

    public BigDecimal getBuyAmt() {
        return buyAmt;
    }

    public void setBuyAmt(BigDecimal buyAmt) {
        this.buyAmt = buyAmt.setScale(8, RoundingMode.DOWN);
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public BigDecimal getMainCurProfit() {
        return mainCurProfit;
    }

    public void setMainCurProfit(BigDecimal mainCurProfit) {
        this.mainCurProfit = mainCurProfit;
    }

    public Fee getFee() {
        return fee;
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }

    @Override
    public String toString() {
        return "ProfitInfo{" +
                "paid: " + sellAmt +
                " " + sellCur +
                ", buy: " + buyAmt +
                " " + buyCur +
                ", commission: " + fee.getFee() +
                " " + fee.getSimbol() +
                '}';
    }
}
