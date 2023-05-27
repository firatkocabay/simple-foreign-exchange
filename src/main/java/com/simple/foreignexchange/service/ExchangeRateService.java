package com.simple.foreignexchange.service;

import com.simple.foreignexchange.dto.ExchangeRateRequestDto;
import com.simple.foreignexchange.dto.ExchangeRateResponseDto;

public interface ExchangeRateService {

    ExchangeRateResponseDto getExchangeRate(ExchangeRateRequestDto exchangeRateRequestDto);

}
