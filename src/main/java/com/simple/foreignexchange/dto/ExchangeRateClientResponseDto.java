package com.simple.foreignexchange.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
public class ExchangeRateClientResponseDto implements Serializable {

    private Long lastUpdate;
    private String base;
    private Map<String, BigDecimal> rates;

}
