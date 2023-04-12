package uk.tw.energy.controller.exception;

public class ReadingsNotFoundException extends RuntimeException {
    public ReadingsNotFoundException() {
        super("No Readings Found.");
    }
}
