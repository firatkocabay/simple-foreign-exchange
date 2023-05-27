package com.simple.foreignexchange;

import java.math.BigDecimal;

public final class TestConstants {

    private TestConstants() {
    }

    public static final String RATES_URL_TEMPLATE = "/api/v1/rates?source=%s&target=%s";
    public static final String CONVERT_URL_TEMPLATE = "/api/v1/convert";
    public static final String CONVERSION_LIST_BY_ID_URL_TEMPLATE = "/api/v1/conversions?transactionId=%s&page=%d&size=%d";
    public static final String CONVERSION_LIST_BY_DATE_URL_TEMPLATE = "/api/v1/conversions?transactionDate=%s&page=%d&size=%d";
    public static final String CONVERSION_LIST_ALL_URL_TEMPLATE = "/api/v1/conversions?transactionId=%s&transactionDate=%s&page=%d&size=%d";
    public static final String CONVERSION_LIST_NO_ID_AND_DATE_URL_TEMPLATE = "/api/v1/conversions?page=%d&size=%d";
    public static final String EXAMPLE_SOURCE_CURRENCY = "EUR";
    public static final String EXAMPLE_TARGET_CURRENCY = "TRY";
    public static final BigDecimal EXAMPLE_AMOUNT = BigDecimal.TEN;
    public static final String INVALID_CURRENCY = "TEST";
    public static final String UNKNOWN_CURRENCY = "VVV";

}
