package com.simple.foreignexchange;

import com.simple.foreignexchange.controller.ConversionController;
import com.simple.foreignexchange.controller.ExchangeRateController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ForeignExchangeApplicationTests {

    @Autowired
    private ForeignExchangeApplication foreignExchangeApplication;

    @Autowired
    private ConversionController conversionController;

    @Autowired
    private ExchangeRateController exchangeRateController;

    @Test
    void contextLoads() {
        ForeignExchangeApplication.main(new String[]{});
        assertNotNull(foreignExchangeApplication);
        assertNotNull(conversionController);
        assertNotNull(exchangeRateController);
    }

}
