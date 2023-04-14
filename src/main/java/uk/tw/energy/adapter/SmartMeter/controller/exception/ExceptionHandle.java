package uk.tw.energy.adapter.SmartMeter.controller.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class ExceptionHandle {
    @ExceptionHandler(PricePlanNotMatchedException.class)
    public ResponseEntity<String> PricePlanNotMatchedExceptionHandler(PricePlanNotMatchedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(ReadingsNotFoundException.class)
    public ResponseEntity<String> ReadingsNotFoundExceptionHandler(ReadingsNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> IllegalArgumentExceptionHandler(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> MethodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format entered.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> ConstraintViolationExceptionHandler(ConstraintViolationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Date not present");
    }
}
