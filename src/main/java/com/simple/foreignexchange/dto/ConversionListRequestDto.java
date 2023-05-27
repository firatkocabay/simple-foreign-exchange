package com.simple.foreignexchange.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;


@Data
@Builder
public class ConversionListRequestDto {

    private Long transactionId;
    private Date transactionDate;
    private int page;
    private int size;

}
