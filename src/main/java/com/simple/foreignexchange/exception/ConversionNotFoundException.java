package com.simple.foreignexchange.exception;

public class ConversionNotFoundException extends RuntimeException {
    public ConversionNotFoundException(String message) {
        super(message);
    }
}
