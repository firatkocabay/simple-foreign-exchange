package com.simple.foreignexchange.service;

import com.simple.foreignexchange.TestConstants;
import com.simple.foreignexchange.dto.Constants;
import com.simple.foreignexchange.dto.ExchangeRateClientResponseDto;
import com.simple.foreignexchange.dto.ExchangeRateRequestDto;
import com.simple.foreignexchange.dto.ExchangeRateResponseDto;
import com.simple.foreignexchange.exception.RatesNotFoundException;
import com.simple.foreignexchange.exception.ThirdPartyServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class ExchangeRateServiceTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void givenMockingResponse_whenGetExchangeRateIsCalled_shouldReturnMockedObject() {
        // given
        final ExchangeRateRequestDto exchangeRateRequestDto = ExchangeRateRequestDto.builder()
                .sourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY)
                .targetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY).build();
        final ExchangeRateClientResponseDto clientResponseDto = new ExchangeRateClientResponseDto();
        clientResponseDto.setBase(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        final Map<String, BigDecimal> rates = new HashMap<>();
        rates.put(TestConstants.EXAMPLE_TARGET_CURRENCY, TestConstants.EXAMPLE_AMOUNT);
        clientResponseDto.setRates(rates);
        clientResponseDto.setLastUpdate(Long.parseLong(String.valueOf(new Date().getTime()).substring(0, 11)));

        // when
        doReturn(clientResponseDto).when(restTemplate).getForObject(Constants.BASE_RATES_URL,
                ExchangeRateClientResponseDto.class,
                TestConstants.EXAMPLE_SOURCE_CURRENCY, Constants.API_KEY);

        ExchangeRateResponseDto exchangeRate = exchangeRateService.getExchangeRate(exchangeRateRequestDto);

        // then
        assertNotNull(clientResponseDto.getLastUpdate());
        assertEquals(TestConstants.EXAMPLE_SOURCE_CURRENCY, exchangeRate.getSourceCurrency());
        assertEquals(TestConstants.EXAMPLE_TARGET_CURRENCY, exchangeRate.getTargetCurrency());
        assertEquals(TestConstants.EXAMPLE_AMOUNT, exchangeRate.getRateAmount());
    }

    @Test
    void givenMockingIncompatibleResponse_whenGetExchangeRateIsCalled_shouldThrowException() {
        // given
        final ExchangeRateRequestDto exchangeRateRequestDto = ExchangeRateRequestDto.builder()
                .sourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY)
                .targetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY).build();
        final Long currentMilliseconds = Long.parseLong(String.valueOf(new Date().getTime()).substring(0, 11));
        final ExchangeRateClientResponseDto clientResponseDto = new ExchangeRateClientResponseDto();
        clientResponseDto.setBase(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        final Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("USD", TestConstants.EXAMPLE_AMOUNT);
        clientResponseDto.setRates(rates);
        clientResponseDto.setLastUpdate(currentMilliseconds);

        // when
        doReturn(clientResponseDto).when(restTemplate).getForObject(Constants.BASE_RATES_URL,
                ExchangeRateClientResponseDto.class,
                TestConstants.EXAMPLE_SOURCE_CURRENCY, Constants.API_KEY);

        // then
        RatesNotFoundException thrown = Assertions.assertThrows(RatesNotFoundException.class, () -> {
            exchangeRateService.getExchangeRate(exchangeRateRequestDto);
        });

        Assertions.assertEquals("Target currency rate not found!", thrown.getMessage());
    }

    @Test
    void givenMockingNullResponse_whenGetExchangeRateIsCalled_shouldThrowException() {
        // given
        final ExchangeRateRequestDto exchangeRateRequestDto = ExchangeRateRequestDto.builder()
                .sourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY)
                .targetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY).build();
        // when
        doReturn(null).when(restTemplate).getForObject(Constants.BASE_RATES_URL,
                ExchangeRateClientResponseDto.class,
                TestConstants.EXAMPLE_SOURCE_CURRENCY, Constants.API_KEY);

        // then
        ThirdPartyServiceException thrown = Assertions.assertThrows(ThirdPartyServiceException.class, () -> {
            exchangeRateService.getExchangeRate(exchangeRateRequestDto);
        });

        Assertions.assertEquals("Rates response object is null!", thrown.getMessage());
    }

    @Test
    void givenInvalidRequest_whenGetExchangeRateIsCalled_shouldThrowException() {
        // given
        final ExchangeRateRequestDto exchangeRateRequestDto = ExchangeRateRequestDto.builder()
                .sourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY)
                .targetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY).build();
        // when
        doThrow(IllegalArgumentException.class).when(restTemplate).getForObject(Constants.BASE_RATES_URL,
                ExchangeRateClientResponseDto.class,
                TestConstants.EXAMPLE_SOURCE_CURRENCY, Constants.API_KEY);

        // then
        ThirdPartyServiceException thrown = Assertions.assertThrows(ThirdPartyServiceException.class, () -> {
            exchangeRateService.getExchangeRate(exchangeRateRequestDto);
        });

        assertTrue(thrown.getMessage().contains(Constants.DEFAULT_THIRD_PARTY_SERVICE_ERROR_MESSAGE));
    }

}