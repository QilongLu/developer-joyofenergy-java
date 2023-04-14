package uk.tw.energy.adapter.SmartMeter.controller.exception;

public class ReadingsNotFoundException extends RuntimeException {
    public ReadingsNotFoundException() {
        super("No Readings Found.");
    }
}
