package com.yhs.inventroysystem.domain.exchange;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"currency", "rate_date"}))
@Getter
@NoArgsConstructor
public class ExchangeRate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Currency currency;

    @Column(nullable = false, precision = 15, scale = 6)
    private BigDecimal rate;

    @Column(nullable = false)
    private LocalDate rateDate;

    public ExchangeRate(Currency currency, BigDecimal rate, LocalDate rateDate) {
        this.currency = currency;
        this.rate = rate;
        this.rateDate = rateDate;
    }

    // KRW 기준 환율이므로 원화 환산 계산
    public BigDecimal convertToKRW(BigDecimal amount) {
        return amount.multiply(this.rate);
    }
}
