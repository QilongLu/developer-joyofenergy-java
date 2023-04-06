package uk.tw.energy.controller;

public class PricePlanNotMatchedException extends RuntimeException {

    public PricePlanNotMatchedException(String smartMeterId) {
        super("No price plan matched with " + smartMeterId);
    }
}
