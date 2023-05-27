package com.simple.foreignexchange.service;

import com.simple.foreignexchange.dto.ConversionListRequestDto;
import com.simple.foreignexchange.dto.ConversionListResponseDto;
import com.simple.foreignexchange.dto.ConvertRequestDto;
import com.simple.foreignexchange.dto.ConvertResponseDto;

public interface ConversionService {

    ConvertResponseDto convertExchange(ConvertRequestDto convertRequestDto);

    ConversionListResponseDto getConversions(ConversionListRequestDto convertRequestDto);

}
