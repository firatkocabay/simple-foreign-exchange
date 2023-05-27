package com.simple.foreignexchange.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class ConversionListResponseDto {

    private List<ConvertResponseDto> conversions;

}
