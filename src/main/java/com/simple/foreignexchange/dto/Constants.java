package com.simple.foreignexchange.dto;

public final class Constants {

    private Constants() {
    }

    public static final String API_V1_MAPPING = "/api/v1";
    public static final String BASE_RATES_URL = "https://anyapi.io/api/v1/exchange/rates?base={base}&apiKey={apiKey}";
    public static final String BASE_CONVERT_URL = "https://anyapi.io/api/v1/exchange/convert?base={base}&to={to}&amount={amount}&apiKey={apiKey}";
    public static final String DEFAULT_THIRD_PARTY_SERVICE_ERROR_MESSAGE = "Exception occurred when call third party service. Exception message: ";
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";

}
