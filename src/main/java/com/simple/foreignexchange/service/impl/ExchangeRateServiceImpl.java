package com.simple.foreignexchange.service.impl;

import com.simple.foreignexchange.dto.Constants;
import com.simple.foreignexchange.dto.ExchangeRateClientResponseDto;
import com.simple.foreignexchange.dto.ExchangeRateRequestDto;
import com.simple.foreignexchange.dto.ExchangeRateResponseDto;
import com.simple.foreignexchange.exception.RatesNotFoundException;
import com.simple.foreignexchange.exception.ThirdPartyServiceException;
import com.simple.foreignexchange.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    @Value("${apiKey}")
    private String apiKey;

    private final RestTemplate restTemplate;

    @Override
    public ExchangeRateResponseDto getExchangeRate(ExchangeRateRequestDto exchangeRateRequestDto) {
        log.info("Started exchange rate flow on service with: {}", exchangeRateRequestDto);
        ExchangeRateResponseDto exchangeRateResponseDto = new ExchangeRateResponseDto();
        exchangeRateResponseDto.setTargetCurrency(exchangeRateRequestDto.getTargetCurrency());

        ExchangeRateClientResponseDto clientResponseDto;
        try {
            clientResponseDto = restTemplate.getForObject(Constants.BASE_RATES_URL,
                    ExchangeRateClientResponseDto.class, exchangeRateRequestDto.getSourceCurrency(), apiKey);
        } catch (Exception e) {
            throw new ThirdPartyServiceException(Constants.DEFAULT_THIRD_PARTY_SERVICE_ERROR_MESSAGE + e.getMessage());
        }

        if (Objects.isNull(clientResponseDto))
            throw new ThirdPartyServiceException("Rates response object is null!");

        convertExchangeResponseFromClientResponse(clientResponseDto, exchangeRateResponseDto, exchangeRateRequestDto.getTargetCurrency());

        return exchangeRateResponseDto;
    }

    private void convertExchangeResponseFromClientResponse(ExchangeRateClientResponseDto clientResponseDto,
                                                           ExchangeRateResponseDto exchangeRateResponseDto,
                                                           String expectedRateCurrency) {
        log.info("Called convertExchangeResponseFromClientResponse expectedRateCurrency: {}", expectedRateCurrency);
        exchangeRateResponseDto.setSourceCurrency(clientResponseDto.getBase());
        clientResponseDto.getRates().forEach((key, value) -> {
            if (key.equalsIgnoreCase(expectedRateCurrency)) {
                exchangeRateResponseDto.setRateAmount(value);
            }
        });

        if (Objects.isNull(exchangeRateResponseDto.getRateAmount()))
            throw new RatesNotFoundException("Target currency rate not found!");
    }

}
