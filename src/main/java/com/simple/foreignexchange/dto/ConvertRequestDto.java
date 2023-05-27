package com.simple.foreignexchange.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConvertRequestDto {

    @NotBlank(message = "Source currency name must be filled!")
    @Size(min = 3, max = 3, message = "Source currency name length must be 3!")
    private String sourceCurrency;

    @NotBlank(message = "Target currency name must be filled!")
    @Size(min = 3, max = 3, message = "Target currency name length must be 3!")
    private String targetCurrency;

    @NotNull(message = "Amount must be filled!")
    @Min(value = 0, message = "Cannot be calculated for amount less than zero!")
    private BigDecimal amount;

}
