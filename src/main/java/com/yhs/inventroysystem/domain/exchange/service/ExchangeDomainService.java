package com.yhs.inventroysystem.domain.exchange.service;

import com.yhs.inventroysystem.domain.exchange.entity.Currency;
import com.yhs.inventroysystem.domain.exchange.entity.ExchangeRate;
import com.yhs.inventroysystem.domain.exchange.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExchangeDomainService {

    private final ExchangeRateRepository exchangeRateRepository;


    @Transactional
    public ExchangeRate saveExchangeRate(ExchangeRate exchangeRate) {
        return exchangeRateRepository.save(exchangeRate);
    }

    public Optional<ExchangeRate> findByCurrencyAndDate(Currency currency, LocalDate date) {
        return exchangeRateRepository.findByCurrencyAndDate(currency, date);
    }

    public Optional<ExchangeRate> findByCurrencyAndDate(Currency currency) {
        return exchangeRateRepository.findByCurrencyAndDate(currency, LocalDate.now());
    }

    public Optional<ExchangeRate> findLatestByCurrencyBeforeDate(Currency currency, LocalDate date) {
        return exchangeRateRepository.findLatestByCurrencyBeforeDate(currency, date);
    }

}
