package uk.tw.energy.controller;

import org.springframework.http.HttpStatus;

public class PricePlanNotMatchedException extends RuntimeException {
    private final HttpStatus status;

    public PricePlanNotMatchedException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
