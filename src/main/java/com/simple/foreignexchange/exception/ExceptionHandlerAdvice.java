package com.simple.foreignexchange.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException e,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        String error = e.getParameterName() + " parameter is missing.";
        final DefaultErrorMessage errorMessage = new DefaultErrorMessage();
        errorMessage.setErrorCode(HttpStatus.BAD_REQUEST.value());
        errorMessage.setErrorStatus(HttpStatus.BAD_REQUEST);
        errorMessage.setErrorMessage(error);
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        StringBuilder resultMessage = new StringBuilder();
        e.getBindingResult().getAllErrors().forEach(error -> resultMessage.append(error.getDefaultMessage()));
        return new ResponseEntity<>(new DefaultErrorMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
                resultMessage.toString()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<DefaultErrorMessage> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>(new DefaultErrorMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
                e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ThirdPartyServiceException.class)
    protected ResponseEntity<DefaultErrorMessage> handleThirdPartyServiceException(ThirdPartyServiceException e) {
        return new ResponseEntity<>(new DefaultErrorMessage(HttpStatus.EXPECTATION_FAILED.value(),
                HttpStatus.EXPECTATION_FAILED, e.getMessage()), HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(ForeignExchangeBadRequestException.class)
    protected ResponseEntity<DefaultErrorMessage> handleBadRequestException(ForeignExchangeBadRequestException e) {
        return new ResponseEntity<>(new DefaultErrorMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
                e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConversionNotFoundException.class)
    protected ResponseEntity<DefaultErrorMessage> handleConversionNotFoundException(ConversionNotFoundException e) {
        return new ResponseEntity<>(new DefaultErrorMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
                e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RatesNotFoundException.class)
    protected ResponseEntity<DefaultErrorMessage> handleRatesNotFoundException(RatesNotFoundException e) {
        return new ResponseEntity<>(new DefaultErrorMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
                e.getMessage()), HttpStatus.NOT_FOUND);
    }

}
