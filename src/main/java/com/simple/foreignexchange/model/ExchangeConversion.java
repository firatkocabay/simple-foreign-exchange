package com.simple.foreignexchange.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@Table(name = "exchange_conversion")
public class ExchangeConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base")
    private String base;

    @Column(name = "destination")
    private String to;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "converted")
    private BigDecimal converted;

    @Column(name = "rate")
    private BigDecimal rate;

    @Column(name = "last_exchange_rate_date")
    private Instant lastExchangeRateDate;

    @Column(name = "transaction_date")
    private Instant transactionDate;

}
