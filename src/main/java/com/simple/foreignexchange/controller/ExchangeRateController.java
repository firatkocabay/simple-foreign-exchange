package com.simple.foreignexchange.controller;

import com.simple.foreignexchange.dto.Constants;
import com.simple.foreignexchange.dto.ExchangeRateRequestDto;
import com.simple.foreignexchange.dto.ExchangeRateResponseDto;
import com.simple.foreignexchange.service.ExchangeRateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = Constants.API_V1_MAPPING)
@RequiredArgsConstructor
@Slf4j
@Validated
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping("/rates")
    public ResponseEntity<ExchangeRateResponseDto> getSingleExchangeRate(
            @Valid @NotBlank @Size(min = 3, max = 3, message = "Source currency name length must be 3!") @RequestParam("source") String source,
            @Valid @NotBlank @Size(min = 3, max = 3, message = "Target currency name length must be 3!") @RequestParam("target") String target) {
        log.info("Start conversion with base: {}, to: {}", source, target);
        final ExchangeRateRequestDto exchangeRateRequestDto = ExchangeRateRequestDto.builder()
                .sourceCurrency(source)
                .targetCurrency(target)
                .build();
        ExchangeRateResponseDto exchangeRateResponseDto = exchangeRateService.getExchangeRate(exchangeRateRequestDto);
        return new ResponseEntity<>(exchangeRateResponseDto, HttpStatus.OK);
    }

}
