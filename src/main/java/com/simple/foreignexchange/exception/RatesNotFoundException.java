package com.simple.foreignexchange.exception;

public class RatesNotFoundException extends RuntimeException {
    public RatesNotFoundException(String message) {
        super(message);
    }
}
