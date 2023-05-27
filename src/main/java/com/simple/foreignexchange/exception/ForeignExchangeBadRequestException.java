package com.simple.foreignexchange.exception;

public class ForeignExchangeBadRequestException extends RuntimeException {
    public ForeignExchangeBadRequestException(String message) {
        super(message);
    }
}
