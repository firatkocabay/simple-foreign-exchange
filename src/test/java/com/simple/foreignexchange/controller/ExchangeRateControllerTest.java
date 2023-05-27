package com.simple.foreignexchange.controller;


import com.google.gson.Gson;
import com.simple.foreignexchange.TestConstants;
import com.simple.foreignexchange.dto.ExchangeRateResponseDto;
import com.simple.foreignexchange.exception.DefaultErrorMessage;
import com.simple.foreignexchange.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExchangeRateControllerTest {

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidParams_whenGetRates_thenShouldReturnAmountSuccessful() throws Exception {
        // given
        final ExchangeRateResponseDto exchangeRateResponseDto = new ExchangeRateResponseDto();
        exchangeRateResponseDto.setSourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        exchangeRateResponseDto.setTargetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        exchangeRateResponseDto.setRateAmount(TestConstants.EXAMPLE_AMOUNT);
        final String urlTemplate = String.format(TestConstants.RATES_URL_TEMPLATE,
                exchangeRateResponseDto.getSourceCurrency(), exchangeRateResponseDto.getTargetCurrency());

        // when
        doReturn(exchangeRateResponseDto).when(exchangeRateService).getExchangeRate(Mockito.any());

        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        ExchangeRateResponseDto responseDto = new Gson().fromJson(responseBody, ExchangeRateResponseDto.class);

        // then
        assertNotNull(responseDto);
        assertEquals(TestConstants.EXAMPLE_AMOUNT, responseDto.getRateAmount());
        assertEquals(TestConstants.EXAMPLE_SOURCE_CURRENCY, responseDto.getSourceCurrency());
        assertEquals(TestConstants.EXAMPLE_TARGET_CURRENCY, responseDto.getTargetCurrency());
    }

    @Test
    void givenInvalidParams_whenGetRates_thenShouldReturnAmountFail() throws Exception {
        // given
        final ExchangeRateResponseDto exchangeRateResponseDto = new ExchangeRateResponseDto();
        exchangeRateResponseDto.setSourceCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        exchangeRateResponseDto.setTargetCurrency(TestConstants.INVALID_CURRENCY);
        exchangeRateResponseDto.setRateAmount(TestConstants.EXAMPLE_AMOUNT);
        final String urlTemplate = String.format(TestConstants.RATES_URL_TEMPLATE,
                exchangeRateResponseDto.getSourceCurrency(), exchangeRateResponseDto.getTargetCurrency());

        // when
        doReturn(exchangeRateResponseDto).when(exchangeRateService).getExchangeRate(Mockito.any());

        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        DefaultErrorMessage exceptionResponse = new Gson().fromJson(responseBody, DefaultErrorMessage.class);

        // then
        assertNotNull(exceptionResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), exceptionResponse.getErrorCode());
        assertEquals("getSingleExchangeRate.target: Target currency name length must be 3!", exceptionResponse.getErrorMessage());
    }

    @Test
    void givenMissingParams_whenCallGetRates_thenShouldFail() throws Exception {
        // given
        final String urlTemplate = String.format("/api/v1/rates?source=%s", TestConstants.EXAMPLE_SOURCE_CURRENCY);

        // when
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        DefaultErrorMessage exceptionResponse = new Gson().fromJson(responseBody, DefaultErrorMessage.class);

        // then
        assertNotNull(exceptionResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), exceptionResponse.getErrorCode());
        assertEquals("target parameter is missing.", exceptionResponse.getErrorMessage());
    }

}
