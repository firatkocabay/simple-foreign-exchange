package com.simple.foreignexchange.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRateResponseDto {

    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal rateAmount;

}
