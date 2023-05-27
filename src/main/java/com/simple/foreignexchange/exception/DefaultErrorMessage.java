package com.simple.foreignexchange.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultErrorMessage {

    private int errorCode;
    private HttpStatus errorStatus;
    private String errorMessage;

}
