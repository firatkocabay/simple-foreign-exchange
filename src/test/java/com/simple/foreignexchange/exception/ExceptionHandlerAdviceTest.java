package com.simple.foreignexchange.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.simple.foreignexchange.TestConstants;
import com.simple.foreignexchange.dto.Constants;
import com.simple.foreignexchange.dto.ConvertRequestDto;
import com.simple.foreignexchange.dto.ExchangeRateResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExceptionHandlerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void beforeAll() throws Exception {
        Thread.sleep(1000);
    }

    @Test
    void givenUnknownTargetCurrency_whenCallConvert_thenShouldThrowException() throws Exception {
        // given
        final ConvertRequestDto convertRequestDto = new ConvertRequestDto();
        final ObjectMapper objectMapper = new ObjectMapper();
        convertRequestDto.setSourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        convertRequestDto.setTargetCurrency(TestConstants.UNKNOWN_CURRENCY);
        convertRequestDto.setAmount(TestConstants.EXAMPLE_AMOUNT);

        // when
        MvcResult mvcResult = mockMvc.perform(post(TestConstants.CONVERT_URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(convertRequestDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isExpectationFailed())
                .andReturn();

        // then
        String responseBody = mvcResult.getResponse().getContentAsString();
        DefaultErrorMessage exceptionResponse = new Gson().fromJson(responseBody, DefaultErrorMessage.class);

        assertNotNull(exceptionResponse);
        assertEquals(HttpStatus.EXPECTATION_FAILED.value(), exceptionResponse.getErrorCode());
        assertTrue(exceptionResponse.getErrorMessage().contains(Constants.DEFAULT_THIRD_PARTY_SERVICE_ERROR_MESSAGE));
    }

    @Test
    void givenUnknownTransactionInfo_whenCallConversions_thenShouldThrowException() throws Exception {
        // given
        final String urlTemplate = String.format(TestConstants.CONVERSION_LIST_BY_ID_URL_TEMPLATE, 999L, 0, 10);

        // when
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isNotFound())
                .andReturn();

        // then
        String responseBody = mvcResult.getResponse().getContentAsString();
        DefaultErrorMessage exceptionResponse = new Gson().fromJson(responseBody, DefaultErrorMessage.class);

        assertNotNull(exceptionResponse);
        assertEquals(HttpStatus.NOT_FOUND.value(), exceptionResponse.getErrorCode());
        assertEquals("Conversion not found!", exceptionResponse.getErrorMessage());
    }

    @Test
    void givenUnknownTargetCurrency_whenGetExchangeRate_thenShouldThrowException() throws Exception {
        // given
        final ExchangeRateResponseDto exchangeRateResponseDto = new ExchangeRateResponseDto();
        exchangeRateResponseDto.setSourceCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        exchangeRateResponseDto.setTargetCurrency(TestConstants.UNKNOWN_CURRENCY);
        exchangeRateResponseDto.setRateAmount(TestConstants.EXAMPLE_AMOUNT);
        final String urlTemplate = String.format(TestConstants.RATES_URL_TEMPLATE,
                exchangeRateResponseDto.getSourceCurrency(), exchangeRateResponseDto.getTargetCurrency());

        // when
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        DefaultErrorMessage exceptionResponse = new Gson().fromJson(responseBody, DefaultErrorMessage.class);

        // then
        assertNotNull(exceptionResponse);
        assertEquals(HttpStatus.NOT_FOUND.value(), exceptionResponse.getErrorCode());
        assertEquals("Target currency rate not found!", exceptionResponse.getErrorMessage());
    }

}