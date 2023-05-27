package com.simple.foreignexchange.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClientConvertResponseDto {

    private String base;
    private String to;
    private BigDecimal amount;
    private BigDecimal converted;
    private BigDecimal rate;
    private Long lastUpdate;

}
