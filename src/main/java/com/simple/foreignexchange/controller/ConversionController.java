package com.simple.foreignexchange.controller;

import com.simple.foreignexchange.dto.Constants;
import com.simple.foreignexchange.dto.ConversionListRequestDto;
import com.simple.foreignexchange.dto.ConversionListResponseDto;
import com.simple.foreignexchange.dto.ConvertRequestDto;
import com.simple.foreignexchange.dto.ConvertResponseDto;
import com.simple.foreignexchange.exception.ForeignExchangeBadRequestException;
import com.simple.foreignexchange.service.ConversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Objects;


@RestController
@RequestMapping(value = Constants.API_V1_MAPPING)
@RequiredArgsConstructor
@Slf4j
@Validated
public class ConversionController {

    private final ConversionService conversionService;

    @PostMapping("/convert")
    public ResponseEntity<ConvertResponseDto> convertCurrency(@Valid @RequestBody ConvertRequestDto convertRequestDto) {
        log.info("Start conversion with convertRequestDto: {}", convertRequestDto);
        ConvertResponseDto convertResponseDto = conversionService.convertExchange(convertRequestDto);
        return new ResponseEntity<>(convertResponseDto, HttpStatus.OK);
    }

    @GetMapping("/conversions")
    public ResponseEntity<ConversionListResponseDto> getConversionsByRequest(
            @RequestParam(value = "transactionId", required = false) Long transactionId,
            @RequestParam(value = "transactionDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date transactionDate,
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER, required = false) int page,
            @RequestParam(value = "size", defaultValue = Constants.DEFAULT_PAGE_SIZE, required = false) int size) {
        log.info("Start getConversions flow.");

        checkRequestParamsValid(transactionId, transactionDate, page, size);
        log.info("Request parameters passed validations.");

        final ConversionListRequestDto conversionListRequestDto = ConversionListRequestDto.builder()
                .transactionId(transactionId)
                .transactionDate(transactionDate)
                .page(page)
                .size(size)
                .build();

        ConversionListResponseDto conversionListResponseDto = conversionService.getConversions(conversionListRequestDto);
        return new ResponseEntity<>(conversionListResponseDto, HttpStatus.OK);
    }

    private void checkRequestParamsValid(Long transactionId, Date transactionDate, int page, int size) {
        if (Objects.isNull(transactionId) && Objects.isNull(transactionDate)) {
            throw new ForeignExchangeBadRequestException("transactionId and transactionDate null or empty, least one of the these inputs required!");
        }
        if (page < 0) {
            throw new ForeignExchangeBadRequestException("Page must be 0 or greater!");
        }
        if (size < 1) {
            throw new ForeignExchangeBadRequestException("Size must be 1 or greater!");
        }
    }

}
