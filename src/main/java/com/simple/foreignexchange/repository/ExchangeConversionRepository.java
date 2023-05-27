package com.simple.foreignexchange.repository;

import com.simple.foreignexchange.model.ExchangeConversion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeConversionRepository extends JpaRepository<ExchangeConversion, Long> {

    @Query("select e from ExchangeConversion e " +
            " where CAST(e.transactionDate AS date) = CAST(:createdDate AS date) " +
            " order by e.transactionDate desc")
    List<ExchangeConversion> getExchangeConversionsByDate(Instant createdDate, Pageable pageable);

    @Query("select e from ExchangeConversion e " +
            " where e.id = :id and CAST(e.transactionDate AS date) = CAST(:createdDate AS date) " +
            " order by e.transactionDate desc")
    Optional<ExchangeConversion> getExchangeConversionByIdAndDate(Long id, Instant createdDate);

}
