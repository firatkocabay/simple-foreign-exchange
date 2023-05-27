package com.simple.foreignexchange.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExchangeRateRequestDto {

    private String sourceCurrency;
    private String targetCurrency;

}
