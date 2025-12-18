package com.yhs.inventroysystem.domain.quotation.entity;

import com.yhs.inventroysystem.domain.exchange.entity.Currency;
import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotaions")
@Getter
@NoArgsConstructor
public class Quotation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String quotationNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuotationType quotationType;

    @Column(nullable = false)
    private String companyName; // 거래처, 발행사

    private String representativeName; // 담당자

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<QuotationItem> items = new ArrayList<>();

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount; // 공급가액 (세전 금액)

    @Column(precision = 15, scale = 2)
    private BigDecimal taxAmount; // 부가세액

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAfterTaxAmount; // 총액 (세후 금액)

    @Column(nullable = false)
    private boolean isTax; // 부가가치세 포함 여부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;


    @Column(length = 500)
    private String note;

    private LocalDate orderedAt;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<QuotationDocument> documents = new ArrayList<>();

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% 부가세

    public Quotation(String quotationNumber, QuotationType quotationType,
                     String companyName, String representativeName,
                     boolean isTax, Currency currency,
                     String note, LocalDate orderedAt) {
        this.quotationNumber = quotationNumber;
        this.quotationType = quotationType;
        this.companyName = companyName;
        this.representativeName = representativeName;
        this.isTax = isTax;
        this.currency = currency;
        this.note = note;
        this.orderedAt = orderedAt;
        this.totalAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.totalAfterTaxAmount = BigDecimal.ZERO;
    }

    public void addItems(List<QuotationItem> items) {
        for (QuotationItem item : items) {
            addItem(item);
        }
    }
    public void addItem(QuotationItem item) {
        this.items.add(item);
        calculateAmounts();
    }
    public void removeItem(QuotationItem item) {
        this.items.remove(item);
        calculateAmounts();
    }
    public void clearItems() {
        this.items.clear();
        this.totalAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.totalAfterTaxAmount = BigDecimal.ZERO;
    }

    public void addDocument(QuotationDocument document) {
        this.documents.add(document);
    }
    public void removeDocument(QuotationDocument document) {
        this.documents.remove(document);
    }

    public void update(QuotationType quotationType, String companyName,
                       String representativeName, boolean isTax,
                       Currency currency, String note, LocalDate orderedAt) {
        this.quotationType = quotationType;
        this.companyName = companyName;
        this.representativeName = representativeName;
        this.isTax = isTax;
        this.currency = currency;
        this.note = note;
        this.orderedAt = orderedAt;

        // 부가세 포함 여부가 변경되면 금액 재계산
        calculateAmounts();
    }

    private void calculateAmounts() {
        // 공급가액 계산
        this.totalAmount = items.stream()
                .map(QuotationItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 부가세 포함 여부에 따라 세액 및 총액 계산
        if (this.isTax) {
            this.taxAmount = this.totalAmount
                    .multiply(TAX_RATE)
                    .setScale(0, RoundingMode.HALF_UP);

            this.totalAfterTaxAmount = this.totalAmount.add(this.taxAmount);
        } else {
            // 부가세 미포함
            this.taxAmount = BigDecimal.ZERO;
            this.totalAfterTaxAmount = this.totalAmount;
        }
    }

    /**
     * 외부에서 금액 재계산이 필요한 경우 호출
     * (예: QuotationItem 수정 후)
     */
    public void recalculateAmounts() {
        calculateAmounts();
    }
}