package com.yhs.inventroysystem.domain.exchange.repository;

import com.yhs.inventroysystem.domain.exchange.entity.Currency;
import com.yhs.inventroysystem.domain.exchange.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate,Long> {

    @Query("SELECT e FROM ExchangeRate e " +
            "WHERE e.currency = :currency " +
            "AND e.rateDate = :date")
    Optional<ExchangeRate> findByCurrencyAndDate(
            @Param("currency") Currency currency,
            @Param("date") LocalDate date
    );

    @Query("SELECT e FROM ExchangeRate e " +
            "WHERE e.currency = :currency " +
            "AND e.rateDate <= :date " +
            "ORDER BY e.rateDate DESC " +
            "LIMIT 1")
    Optional<ExchangeRate> findLatestByCurrencyBeforeDate(
            @Param("currency") Currency currency,
            @Param("date") LocalDate date
    );
}
