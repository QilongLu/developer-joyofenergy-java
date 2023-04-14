package uk.tw.energy.adapter.SmartMeter.controller.exception;

public class PricePlanNotMatchedException extends RuntimeException {
    public PricePlanNotMatchedException(String smartMeterId) {
        super("No price plan matched with " + smartMeterId);
    }
}
