package com.simple.foreignexchange.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConvertResponseDto {

    private Long transactionId;
    private String transactionDate;
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal amount;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private String lastExchangeRateDate;

}
