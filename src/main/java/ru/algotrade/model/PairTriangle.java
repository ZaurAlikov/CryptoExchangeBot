package ru.algotrade.model;

public class PairTriangle {

    public static int NUM_PAIR = 0;

    private String firstPair;
    private String secondPair;
    private String thirdPair;

    public PairTriangle(String firstPair, String secondPair, String thirdPair) {
        this.firstPair = firstPair;
        this.secondPair = secondPair;
        this.thirdPair = thirdPair;
    }

    public String getFirstPair() {
        return firstPair;
    }

    public void setFirstPair(String firstPair) {
        this.firstPair = firstPair;
    }

    public String getSecondPair() {
        return secondPair;
    }

    public void setSecondPair(String secondPair) {
        this.secondPair = secondPair;
    }

    public String getThirdPair() {
        return thirdPair;
    }

    public void setThirdPair(String thirdPair) {
        this.thirdPair = thirdPair;
    }

    @Override
    public String toString() {
        return "PairTriangle{" + firstPair + ' ' + secondPair + ' ' + thirdPair + '}';
    }
}
