package com.yhs.inventroysystem.application.exchange;

import com.yhs.inventroysystem.domain.exchange.entity.Currency;
import com.yhs.inventroysystem.domain.exchange.entity.ExchangeRate;
import com.yhs.inventroysystem.domain.exchange.service.ExchangeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExchangeRateService {

    private final ExchangeDomainService exchangeDomainService;
    private final RestTemplate restTemplate;

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/KRW";

    /**
     * 특정 날짜의 환율 조회 (없으면 API에서 가져와서 저장)
     */
    @Transactional
    public ExchangeRate getExchangeRate(Currency currency, LocalDate date) {
        // KRW는 환율이 1
        if (currency == Currency.KRW) {
            return new ExchangeRate(Currency.KRW, BigDecimal.ONE, date);
        }

        // DB에서 조회
        return exchangeDomainService.findByCurrencyAndDate(currency, date)
                .orElseGet(() -> fetchAndSaveExchangeRate(currency, date));
    }

    /**
     * 외부 API에서 환율 가져와서 저장
     */
    @Transactional
    public ExchangeRate fetchAndSaveExchangeRate(Currency currency, LocalDate date) {
        try {
            // API 호출
            Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);

            if (response != null && response.containsKey("rates")) {
                Map<String, Double> rates = (Map<String, Double>) response.get("rates");

                // KRW 기준이므로 역수 계산
                // 예: API에서 1 KRW = 0.00075 USD라면, 1 USD = 1333.33 KRW
                Double rate = rates.get(currency.getCode());
                BigDecimal krwRate = BigDecimal.ONE.divide(
                        BigDecimal.valueOf(rate),
                        6,
                        BigDecimal.ROUND_HALF_UP
                );

                ExchangeRate exchangeRate = new ExchangeRate(currency, krwRate, date);
                return exchangeDomainService.saveExchangeRate(exchangeRate);
            }
        } catch (Exception e) {
            log.error("환율 조회 실패: {}", e.getMessage());
        }

        // 실패 시 기본값 반환 (고정 환율)
        return getDefaultExchangeRate(currency, date);
    }

    /**
     * API 실패 시 기본 환율 반환
     */
    private ExchangeRate getDefaultExchangeRate(Currency currency, LocalDate date) {
        BigDecimal defaultRate = switch (currency) {
            case USD -> BigDecimal.valueOf(1300.0);
            case JPY -> BigDecimal.valueOf(9.5);
            case EUR -> BigDecimal.valueOf(1400.0);
            case CNY -> BigDecimal.valueOf(180.0);
            case GBP -> BigDecimal.valueOf(1650.0);
            default -> BigDecimal.ONE;
        };

        ExchangeRate exchangeRate = new ExchangeRate(currency, defaultRate, date);
        return exchangeDomainService.saveExchangeRate(exchangeRate);
    }

}
