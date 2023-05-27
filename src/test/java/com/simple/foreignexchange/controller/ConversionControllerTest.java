package com.simple.foreignexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.simple.foreignexchange.TestConstants;
import com.simple.foreignexchange.dto.ConversionListRequestDto;
import com.simple.foreignexchange.dto.ConversionListResponseDto;
import com.simple.foreignexchange.dto.ConvertRequestDto;
import com.simple.foreignexchange.dto.ConvertResponseDto;
import com.simple.foreignexchange.exception.DefaultErrorMessage;
import com.simple.foreignexchange.service.ConversionService;
import com.simple.foreignexchange.util.TimeUtil;
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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConversionControllerTest {

    @MockBean
    private ConversionService conversionService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidRequest_whenConvert_thenShouldReturnConvertedAmountSuccess() throws Exception {
        // given
        final BigDecimal actualCalculatedAmount = BigDecimal.valueOf(100);
        final ObjectMapper objectMapper = new ObjectMapper();
        final ConvertResponseDto convertResponseDto = new ConvertResponseDto();
        convertResponseDto.setTransactionId(1L);
        convertResponseDto.setSourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        convertResponseDto.setTargetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        convertResponseDto.setAmount(TestConstants.EXAMPLE_AMOUNT);
        convertResponseDto.setExchangeRate(TestConstants.EXAMPLE_AMOUNT);
        convertResponseDto.setConvertedAmount(actualCalculatedAmount);

        final ConvertRequestDto convertRequestDto = new ConvertRequestDto();
        convertRequestDto.setSourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        convertRequestDto.setTargetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        convertRequestDto.setAmount(TestConstants.EXAMPLE_AMOUNT);

        // when
        doReturn(convertResponseDto).when(conversionService).convertExchange(convertRequestDto);

        // then
        MvcResult mvcResult = mockMvc.perform(post(TestConstants.CONVERT_URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(convertRequestDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        ConvertResponseDto responseDto = new Gson().fromJson(responseBody, ConvertResponseDto.class);

        assertNotNull(responseDto);
        assertEquals(TestConstants.EXAMPLE_AMOUNT, responseDto.getAmount());
        assertEquals(actualCalculatedAmount, responseDto.getConvertedAmount());
        assertEquals(TestConstants.EXAMPLE_SOURCE_CURRENCY, responseDto.getSourceCurrency());
        assertEquals(TestConstants.EXAMPLE_TARGET_CURRENCY, responseDto.getTargetCurrency());
    }

    @Test
    void givenInvalidRequest_whenConvert_thenShouldReturnException() throws Exception {
        // given
        final ConvertRequestDto convertRequestDto = new ConvertRequestDto();
        final ObjectMapper objectMapper = new ObjectMapper();
        convertRequestDto.setSourceCurrency(null);
        convertRequestDto.setTargetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        convertRequestDto.setAmount(TestConstants.EXAMPLE_AMOUNT);

        // when
        MvcResult mvcResult = mockMvc.perform(post(TestConstants.CONVERT_URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(convertRequestDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        String responseBody = mvcResult.getResponse().getContentAsString();
        DefaultErrorMessage exceptionResponse = new Gson().fromJson(responseBody, DefaultErrorMessage.class);

        assertNotNull(exceptionResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), exceptionResponse.getErrorCode());
        assertEquals("Source currency name must be filled!", exceptionResponse.getErrorMessage());
    }

    @Test
    void givenValidRequest_whenListConversionsById_thenShouldReturnListSuccess() throws Exception {
        // given
        final BigDecimal actualCalculatedAmount = BigDecimal.valueOf(100);
        final String transactionDateStr = TimeUtil.getFormattedInstantDate(new Date().toInstant());
        final Long transactionId = 101L;

        final ConvertResponseDto convertResponseDto = new ConvertResponseDto();
        convertResponseDto.setTransactionId(transactionId);
        convertResponseDto.setSourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        convertResponseDto.setTargetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        convertResponseDto.setAmount(TestConstants.EXAMPLE_AMOUNT);
        convertResponseDto.setExchangeRate(TestConstants.EXAMPLE_AMOUNT);
        convertResponseDto.setConvertedAmount(actualCalculatedAmount);
        convertResponseDto.setTransactionDate(transactionDateStr);

        final List<ConvertResponseDto> conversions = new ArrayList<>();
        conversions.add(convertResponseDto);

        final ConversionListResponseDto conversionListResponseDto = new ConversionListResponseDto();
        conversionListResponseDto.setConversions(conversions);

        final ConversionListRequestDto conversionListRequestDto = ConversionListRequestDto.builder()
                .transactionId(transactionId).page(0).size(5).build();

        final String urlTemplate = String.format(TestConstants.CONVERSION_LIST_BY_ID_URL_TEMPLATE,
                conversionListRequestDto.getTransactionId(),
                conversionListRequestDto.getPage(),
                conversionListRequestDto.getSize());

        // when
        doReturn(conversionListResponseDto).when(conversionService).getConversions(conversionListRequestDto);

        // then
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        ConversionListResponseDto responseDto = new Gson().fromJson(responseBody, ConversionListResponseDto.class);

        assertNotNull(responseDto);
        assertEquals(TestConstants.EXAMPLE_AMOUNT, responseDto.getConversions().get(0).getAmount());
        assertEquals(actualCalculatedAmount, responseDto.getConversions().get(0).getConvertedAmount());
        assertEquals(TestConstants.EXAMPLE_SOURCE_CURRENCY, responseDto.getConversions().get(0).getSourceCurrency());
        assertEquals(TestConstants.EXAMPLE_TARGET_CURRENCY, responseDto.getConversions().get(0).getTargetCurrency());
        assertEquals(transactionDateStr, responseDto.getConversions().get(0).getTransactionDate());
        assertEquals(transactionId, responseDto.getConversions().get(0).getTransactionId());
    }

    @Test
    void givenValidRequest_whenListConversionsByDate_thenShouldReturnListSuccess() throws Exception {
        // given
        final BigDecimal actualCalculatedAmount = BigDecimal.valueOf(100);
        final String currentDateStr = "2023-05-27";
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        final Date firstTransactionDate = formatter.parse(currentDateStr);
        final Date secondDate = new Date();
        final String transactionDateStr = TimeUtil.getFormattedInstantDate(firstTransactionDate.toInstant());
        final String secondTransactionDateStr = TimeUtil.getFormattedInstantDate(secondDate.toInstant());
        final Long transactionId = 101L;
        final Long secondTransactionId = 505L;

        final ConvertResponseDto convertResponseDto = new ConvertResponseDto();
        convertResponseDto.setTransactionId(transactionId);
        convertResponseDto.setSourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        convertResponseDto.setTargetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        convertResponseDto.setAmount(TestConstants.EXAMPLE_AMOUNT);
        convertResponseDto.setExchangeRate(TestConstants.EXAMPLE_AMOUNT);
        convertResponseDto.setConvertedAmount(actualCalculatedAmount);
        convertResponseDto.setTransactionDate(transactionDateStr);

        final ConvertResponseDto secondConvertResponseDto = new ConvertResponseDto();
        secondConvertResponseDto.setTransactionId(secondTransactionId);
        secondConvertResponseDto.setSourceCurrency("USD");
        secondConvertResponseDto.setTargetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        secondConvertResponseDto.setAmount(TestConstants.EXAMPLE_AMOUNT.multiply(BigDecimal.TEN));
        secondConvertResponseDto.setExchangeRate(TestConstants.EXAMPLE_AMOUNT.multiply(BigDecimal.valueOf(1.3)));
        secondConvertResponseDto.setConvertedAmount(BigDecimal.valueOf(1300));
        secondConvertResponseDto.setTransactionDate(TimeUtil.getFormattedInstantDate(secondDate.toInstant()));

        final List<ConvertResponseDto> conversions = new ArrayList<>();
        conversions.add(convertResponseDto);
        conversions.add(secondConvertResponseDto);

        final ConversionListResponseDto conversionListResponseDto = new ConversionListResponseDto();
        conversionListResponseDto.setConversions(conversions);

        final String urlTemplate = String.format(TestConstants.CONVERSION_LIST_BY_DATE_URL_TEMPLATE, currentDateStr, 0, 5);

        // when
        doReturn(conversionListResponseDto).when(conversionService).getConversions(Mockito.any());

        // then
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        ConversionListResponseDto responseDto = new Gson().fromJson(responseBody, ConversionListResponseDto.class);

        assertNotNull(responseDto);
        assertEquals(TestConstants.EXAMPLE_AMOUNT, responseDto.getConversions().get(0).getAmount());
        assertEquals(actualCalculatedAmount, responseDto.getConversions().get(0).getConvertedAmount());
        assertEquals(TestConstants.EXAMPLE_SOURCE_CURRENCY, responseDto.getConversions().get(0).getSourceCurrency());
        assertEquals(TestConstants.EXAMPLE_TARGET_CURRENCY, responseDto.getConversions().get(0).getTargetCurrency());
        assertEquals(transactionDateStr, responseDto.getConversions().get(0).getTransactionDate());
        assertEquals(transactionId, responseDto.getConversions().get(0).getTransactionId());
        assertEquals(BigDecimal.valueOf(100), responseDto.getConversions().get(1).getAmount());
        assertEquals(BigDecimal.valueOf(1300), responseDto.getConversions().get(1).getConvertedAmount());
        assertEquals("USD", responseDto.getConversions().get(1).getSourceCurrency());
        assertEquals(TestConstants.EXAMPLE_TARGET_CURRENCY, responseDto.getConversions().get(1).getTargetCurrency());
        assertEquals(secondTransactionDateStr, responseDto.getConversions().get(1).getTransactionDate());
        assertEquals(505L, responseDto.getConversions().get(1).getTransactionId());
    }

    @Test
    void givenValidRequest_whenListConversionsByIdAndDate_thenShouldReturnListSuccess() throws Exception {
        // given
        final BigDecimal actualCalculatedAmount = BigDecimal.valueOf(100);
        final String transactionDateStr = TimeUtil.getFormattedInstantDate(new Date().toInstant());
        final Long transactionId = 101L;
        final String currentDateStr = "2023-05-27";
        final ConvertResponseDto convertResponseDto = new ConvertResponseDto();
        convertResponseDto.setTransactionId(transactionId);
        convertResponseDto.setSourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        convertResponseDto.setTargetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        convertResponseDto.setAmount(TestConstants.EXAMPLE_AMOUNT);
        convertResponseDto.setExchangeRate(TestConstants.EXAMPLE_AMOUNT);
        convertResponseDto.setConvertedAmount(actualCalculatedAmount);
        convertResponseDto.setTransactionDate(transactionDateStr);

        final List<ConvertResponseDto> conversions = new ArrayList<>();
        conversions.add(convertResponseDto);

        final ConversionListResponseDto conversionListResponseDto = new ConversionListResponseDto();
        conversionListResponseDto.setConversions(conversions);

        final String urlTemplate = String.format(TestConstants.CONVERSION_LIST_ALL_URL_TEMPLATE, transactionId, currentDateStr, 0, 10);

        // when
        doReturn(conversionListResponseDto).when(conversionService).getConversions(Mockito.any());

        // then
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        ConversionListResponseDto responseDto = new Gson().fromJson(responseBody, ConversionListResponseDto.class);

        assertNotNull(responseDto);
        assertEquals(TestConstants.EXAMPLE_AMOUNT, responseDto.getConversions().get(0).getAmount());
        assertEquals(actualCalculatedAmount, responseDto.getConversions().get(0).getConvertedAmount());
        assertEquals(TestConstants.EXAMPLE_SOURCE_CURRENCY, responseDto.getConversions().get(0).getSourceCurrency());
        assertEquals(TestConstants.EXAMPLE_TARGET_CURRENCY, responseDto.getConversions().get(0).getTargetCurrency());
        assertEquals(transactionDateStr, responseDto.getConversions().get(0).getTransactionDate());
        assertEquals(transactionId, responseDto.getConversions().get(0).getTransactionId());
    }

    @Test
    void givenInvalidRequest_whenListConversionsInvalidPage_thenShouldReturnException() throws Exception {
        // given
        final String urlTemplate = String.format(TestConstants.CONVERSION_LIST_ALL_URL_TEMPLATE, 101L, "2023-05-27", -1, 10);

        // when
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        DefaultErrorMessage exceptionResponse = new Gson().fromJson(responseBody, DefaultErrorMessage.class);

        // then
        assertNotNull(exceptionResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), exceptionResponse.getErrorCode());
        assertEquals("Page must be 0 or greater!", exceptionResponse.getErrorMessage());
    }

    @Test
    void givenInvalidRequest_whenListConversionsInvalidSize_thenShouldReturnException() throws Exception {
        // given
        final String urlTemplate = String.format(TestConstants.CONVERSION_LIST_ALL_URL_TEMPLATE, 101L, "2023-05-27", 0, 0);

        // when
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        DefaultErrorMessage exceptionResponse = new Gson().fromJson(responseBody, DefaultErrorMessage.class);

        // then
        assertNotNull(exceptionResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), exceptionResponse.getErrorCode());
        assertEquals("Size must be 1 or greater!", exceptionResponse.getErrorMessage());
    }

    @Test
    void givenInvalidRequest_whenListConversionsNullIdAndDate_thenShouldReturnException() throws Exception {
        // given
        final String urlTemplate = String.format(TestConstants.CONVERSION_LIST_NO_ID_AND_DATE_URL_TEMPLATE, 0, 10);

        // when
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        DefaultErrorMessage exceptionResponse = new Gson().fromJson(responseBody, DefaultErrorMessage.class);

        // then
        assertNotNull(exceptionResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), exceptionResponse.getErrorCode());
        assertEquals("transactionId and transactionDate null or empty, least one of the these inputs required!", exceptionResponse.getErrorMessage());
    }

}