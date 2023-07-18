package com.simple.foreignexchange.service;

import com.simple.foreignexchange.TestConstants;
import com.simple.foreignexchange.dto.ClientConvertResponseDto;
import com.simple.foreignexchange.dto.Constants;
import com.simple.foreignexchange.dto.ConversionListRequestDto;
import com.simple.foreignexchange.dto.ConversionListResponseDto;
import com.simple.foreignexchange.dto.ConvertRequestDto;
import com.simple.foreignexchange.dto.ConvertResponseDto;
import com.simple.foreignexchange.exception.ConversionNotFoundException;
import com.simple.foreignexchange.exception.ThirdPartyServiceException;
import com.simple.foreignexchange.model.ExchangeConversion;
import com.simple.foreignexchange.repository.ExchangeConversionRepository;
import com.simple.foreignexchange.util.TimeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class ConversionServiceTest {

    @Value("${apiKey}")
    private String apiKey;

    @Autowired
    private ConversionService conversionService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ExchangeConversionRepository exchangeConversionRepository;


    @Test
    void givenMockingResponse_whenGetExchangeRateIsCalled_shouldReturnMockedObject() {
        // given
        final BigDecimal convertedAmount = BigDecimal.valueOf(100);
        final Instant currentTime = Instant.now();
        final Long currentMilliseconds = Long.parseLong(String.valueOf(new Date().getTime()).substring(0, 11));
        final ConvertRequestDto convertRequestDto = new ConvertRequestDto();
        convertRequestDto.setSourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        convertRequestDto.setTargetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        convertRequestDto.setAmount(TestConstants.EXAMPLE_AMOUNT);

        final ClientConvertResponseDto clientResponseDto = new ClientConvertResponseDto();
        clientResponseDto.setBase(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        clientResponseDto.setTo(TestConstants.EXAMPLE_TARGET_CURRENCY);
        clientResponseDto.setAmount(TestConstants.EXAMPLE_AMOUNT);
        clientResponseDto.setRate(TestConstants.EXAMPLE_AMOUNT);
        clientResponseDto.setLastUpdate(currentMilliseconds);
        clientResponseDto.setConverted(convertedAmount);

        final ExchangeConversion exchangeConversion = new ExchangeConversion();
        exchangeConversion.setBase(clientResponseDto.getBase());
        exchangeConversion.setTo(clientResponseDto.getTo());
        exchangeConversion.setAmount(clientResponseDto.getAmount());
        exchangeConversion.setRate(clientResponseDto.getRate());
        exchangeConversion.setConverted(clientResponseDto.getConverted());
        exchangeConversion.setTransactionDate(currentTime);
        exchangeConversion.setLastExchangeRateDate(currentTime);

        // when
        doReturn(clientResponseDto).when(restTemplate).getForObject(Constants.BASE_CONVERT_URL,
                ClientConvertResponseDto.class, convertRequestDto.getSourceCurrency(),
                convertRequestDto.getTargetCurrency(), convertRequestDto.getAmount(), apiKey);
        doReturn(exchangeConversion).when(exchangeConversionRepository).save(Mockito.any());

        ConvertResponseDto convertResponseDto = conversionService.convertExchange(convertRequestDto);

        // then
        assertEquals(TestConstants.EXAMPLE_SOURCE_CURRENCY, convertResponseDto.getSourceCurrency());
        assertEquals(TestConstants.EXAMPLE_TARGET_CURRENCY, convertResponseDto.getTargetCurrency());
        assertEquals(TestConstants.EXAMPLE_AMOUNT, convertResponseDto.getAmount());
        assertEquals(convertedAmount, convertResponseDto.getConvertedAmount());
        assertEquals(TimeUtil.getFormattedInstantDate(currentTime), convertResponseDto.getTransactionDate());
        assertEquals(TimeUtil.getFormattedInstantDate(currentTime), convertResponseDto.getLastExchangeRateDate());
    }

    @Test
    void givenMockingIncompatibleResponse_whenConvertCurrencyIsCalled_shouldThrowException() {
        // given
        final ConvertRequestDto convertRequestDto = new ConvertRequestDto();
        convertRequestDto.setSourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        convertRequestDto.setTargetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        convertRequestDto.setAmount(TestConstants.EXAMPLE_AMOUNT);

        // when
        doReturn(null).when(restTemplate).getForObject(Constants.BASE_CONVERT_URL,
                ClientConvertResponseDto.class, convertRequestDto.getSourceCurrency(),
                convertRequestDto.getTargetCurrency(), convertRequestDto.getAmount(), apiKey);

        // then
        ThirdPartyServiceException thrown = Assertions.assertThrows(ThirdPartyServiceException.class, () -> {
            conversionService.convertExchange(convertRequestDto);
        });

        Assertions.assertEquals("Convert response object is null!", thrown.getMessage());
    }

    @Test
    void givenInvalidRequest_whenConvertCurrencyIsCalled_shouldThrowException() {
        // given
        final ConvertRequestDto convertRequestDto = new ConvertRequestDto();
        convertRequestDto.setSourceCurrency(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        convertRequestDto.setTargetCurrency(TestConstants.EXAMPLE_TARGET_CURRENCY);
        convertRequestDto.setAmount(TestConstants.EXAMPLE_AMOUNT);

        // when
        doThrow(RuntimeException.class).when(restTemplate).getForObject(Constants.BASE_CONVERT_URL,
                ClientConvertResponseDto.class, convertRequestDto.getSourceCurrency(),
                convertRequestDto.getTargetCurrency(), convertRequestDto.getAmount(), apiKey);

        // then
        ThirdPartyServiceException thrown = Assertions.assertThrows(ThirdPartyServiceException.class, () -> {
            conversionService.convertExchange(convertRequestDto);
        });

        assertTrue(thrown.getMessage().contains(Constants.DEFAULT_THIRD_PARTY_SERVICE_ERROR_MESSAGE));
    }

    @Test
    void givenMockConversionList_whenGetConversionsIsCalled_shouldReturnMockedListObject() {
        // given
        final BigDecimal convertedAmount = BigDecimal.valueOf(100);
        final Long transactionId = 101L;
        final Date currentDate = new Date();
        final Instant currentInstant = Instant.now();
        final String transactionDateStr = TimeUtil.getFormattedInstantDate(currentInstant);
        final ConversionListRequestDto conversionListRequestDto = ConversionListRequestDto.builder()
                .transactionId(transactionId).transactionDate(currentDate).page(0).size(10).build();

        final ExchangeConversion exchangeConversion = new ExchangeConversion();
        exchangeConversion.setId(transactionId);
        exchangeConversion.setTransactionDate(currentInstant);
        exchangeConversion.setBase(TestConstants.EXAMPLE_SOURCE_CURRENCY);
        exchangeConversion.setTo(TestConstants.EXAMPLE_TARGET_CURRENCY);
        exchangeConversion.setAmount(TestConstants.EXAMPLE_AMOUNT);
        exchangeConversion.setRate(BigDecimal.TEN);
        exchangeConversion.setConverted(convertedAmount);

        final ConvertResponseDto convertResponseDto = new ConvertResponseDto();
        convertResponseDto.setSourceCurrency(exchangeConversion.getBase());
        convertResponseDto.setTargetCurrency(exchangeConversion.getTo());
        convertResponseDto.setAmount(exchangeConversion.getAmount());
        convertResponseDto.setConvertedAmount(exchangeConversion.getConverted());
        convertResponseDto.setTransactionId(exchangeConversion.getId());
        convertResponseDto.setLastExchangeRateDate(TimeUtil.getFormattedInstantDate(exchangeConversion.getLastExchangeRateDate()));
        convertResponseDto.setTransactionDate(transactionDateStr);

        final List<ConvertResponseDto> conversions = new ArrayList<>();
        conversions.add(convertResponseDto);

        final ConversionListResponseDto conversionListResponseDto = new ConversionListResponseDto();
        conversionListResponseDto.setConversions(conversions);

        // when
        doReturn(Optional.of(exchangeConversion)).when(exchangeConversionRepository)
                .getExchangeConversionByIdAndDate(transactionId, currentInstant.truncatedTo(ChronoUnit.DAYS));

        ConversionListResponseDto conversionList = conversionService.getConversions(conversionListRequestDto);

        // then
        assertEquals(TestConstants.EXAMPLE_SOURCE_CURRENCY, conversionList.getConversions().get(0).getSourceCurrency());
        assertEquals(TestConstants.EXAMPLE_TARGET_CURRENCY, conversionList.getConversions().get(0).getTargetCurrency());
        assertEquals(TestConstants.EXAMPLE_AMOUNT, conversionList.getConversions().get(0).getAmount());
        assertEquals(convertedAmount, conversionList.getConversions().get(0).getConvertedAmount());
        assertEquals(transactionDateStr, conversionList.getConversions().get(0).getTransactionDate());
    }

    @Test
    void givenNotSavedTransactionId_whenGetConversionsIsCalled_shouldThrowException() {
        // given
        final Long transactionId = 101L;
        final ConversionListRequestDto conversionListRequestDto = ConversionListRequestDto.builder()
                .transactionId(transactionId).page(0).size(10).build();

        // when
        doReturn(Optional.empty()).when(exchangeConversionRepository).findById(transactionId);

        // then
        ConversionNotFoundException thrown = Assertions.assertThrows(ConversionNotFoundException.class, () -> {
            conversionService.getConversions(conversionListRequestDto);
        });

        assertEquals("Conversion not found!", thrown.getMessage());
    }

    @Test
    void givenNotSavedTransactionDate_whenGetConversionsIsCalled_shouldThrowException() {
        // given
        final Date currentDate = new Date();
        final ConversionListRequestDto conversionListRequestDto = ConversionListRequestDto.builder()
                .transactionDate(currentDate).page(0).size(10).build();
        final var pageable = PageRequest.of(conversionListRequestDto.getPage(), conversionListRequestDto.getSize());

        // when
        doReturn(Collections.emptyList())
                .when(exchangeConversionRepository)
                .getExchangeConversionsByDate(currentDate.toInstant().truncatedTo(ChronoUnit.DAYS), pageable);

        // then
        ConversionNotFoundException thrown = Assertions.assertThrows(ConversionNotFoundException.class, () -> {
            conversionService.getConversions(conversionListRequestDto);
        });

        assertEquals("Conversion not found!", thrown.getMessage());
    }

}