package com.simple.foreignexchange.service.impl;

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
import com.simple.foreignexchange.service.ConversionService;
import com.simple.foreignexchange.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionServiceImpl implements ConversionService {

    private final RestTemplate restTemplate;
    private final ExchangeConversionRepository exchangeConversionRepository;

    @Override
    @Transactional
    public ConvertResponseDto convertExchange(ConvertRequestDto convertRequestDto) {
        log.info("Started conversion flow on service.");
        ClientConvertResponseDto clientConvertResponseDto;

        try {
            clientConvertResponseDto = restTemplate.getForObject(Constants.BASE_CONVERT_URL, ClientConvertResponseDto.class,
                    convertRequestDto.getSourceCurrency(), convertRequestDto.getTargetCurrency(), convertRequestDto.getAmount(), Constants.API_KEY);
        } catch (Exception e) {
            throw new ThirdPartyServiceException(Constants.DEFAULT_THIRD_PARTY_SERVICE_ERROR_MESSAGE + e.getMessage());
        }

        if (Objects.isNull(clientConvertResponseDto))
            throw new ThirdPartyServiceException("Convert response object is null!");

        ExchangeConversion exchangeConversion = createExchangeConversion(clientConvertResponseDto);
        return convertToConvertResponseDto(exchangeConversion);

    }

    @Override
    @Transactional(readOnly = true)
    public ConversionListResponseDto getConversions(ConversionListRequestDto conversionRequest) {
        log.info("Start get conversion list from: {}", conversionRequest);
        ConversionListResponseDto conversionListResponseDto = new ConversionListResponseDto();
        List<ConvertResponseDto> conversions = new ArrayList<>();
        final var pageable = PageRequest.of(conversionRequest.getPage(), conversionRequest.getSize());

        if (Objects.nonNull(conversionRequest.getTransactionId()) && Objects.nonNull(conversionRequest.getTransactionDate())) {
            exchangeConversionRepository.getExchangeConversionByIdAndDate(conversionRequest.getTransactionId(),
                            conversionRequest.getTransactionDate().toInstant().truncatedTo(ChronoUnit.DAYS))
                    .ifPresent(exchangeConversion -> conversions.add(convertToConvertResponseDto(exchangeConversion)));
        } else if (Objects.nonNull(conversionRequest.getTransactionId())) {
            exchangeConversionRepository.findById(conversionRequest.getTransactionId())
                    .ifPresent(exchangeConversion -> conversions.add(convertToConvertResponseDto(exchangeConversion)));
        } else {
            exchangeConversionRepository.getExchangeConversionsByDate(conversionRequest.getTransactionDate()
                            .toInstant().truncatedTo(ChronoUnit.DAYS), pageable)
                    .forEach(exchangeConversion -> conversions.add(convertToConvertResponseDto(exchangeConversion)));
        }

        if (conversions.isEmpty())
            throw new ConversionNotFoundException("Conversion not found!");

        conversionListResponseDto.setConversions(conversions);
        return conversionListResponseDto;
    }

    private ExchangeConversion createExchangeConversion(ClientConvertResponseDto clientConvertResponseDto) {
        log.info("Called createExchangeConversion with: {}", clientConvertResponseDto);
        ExchangeConversion exchangeConversion = convertToEntity(clientConvertResponseDto);
        return exchangeConversionRepository.save(exchangeConversion);
    }

    private ExchangeConversion convertToEntity(ClientConvertResponseDto clientConvertResponseDto) {
        log.info("Called convertToEntity with: {}", clientConvertResponseDto);
        ExchangeConversion exchangeConversion = new ExchangeConversion();
        exchangeConversion.setBase(clientConvertResponseDto.getBase());
        exchangeConversion.setTo(clientConvertResponseDto.getTo());
        exchangeConversion.setAmount(clientConvertResponseDto.getAmount());
        exchangeConversion.setConverted(clientConvertResponseDto.getConverted());
        exchangeConversion.setRate(clientConvertResponseDto.getRate());
        exchangeConversion.setLastExchangeRateDate(TimeUtil.convertMissingMillisecondsToInstant(clientConvertResponseDto.getLastUpdate()));
        exchangeConversion.setTransactionDate(Instant.now());
        return exchangeConversion;
    }

    private ConvertResponseDto convertToConvertResponseDto(ExchangeConversion exchangeConversion) {
        log.info("Called convertToConvertResponseDto with: {}", exchangeConversion);
        ConvertResponseDto convertResponseDto = new ConvertResponseDto();
        convertResponseDto.setTransactionId(exchangeConversion.getId());
        convertResponseDto.setSourceCurrency(exchangeConversion.getBase());
        convertResponseDto.setTargetCurrency(exchangeConversion.getTo());
        convertResponseDto.setAmount(exchangeConversion.getAmount());
        convertResponseDto.setConvertedAmount(exchangeConversion.getConverted());
        convertResponseDto.setExchangeRate(exchangeConversion.getRate());
        convertResponseDto.setLastExchangeRateDate(TimeUtil.getFormattedInstantDate(exchangeConversion.getLastExchangeRateDate()));
        convertResponseDto.setTransactionDate(TimeUtil.getFormattedInstantDate(exchangeConversion.getTransactionDate()));
        return convertResponseDto;
    }

}
