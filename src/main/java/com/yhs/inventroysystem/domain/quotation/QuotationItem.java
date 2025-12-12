package com.yhs.inventroysystem.domain.quotation;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "quotataion_items")
@Getter
@NoArgsConstructor
public class QuotationItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Quotation quotation;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;


    public QuotationItem(Quotation quotation, String productName,
                         Integer quantity, BigDecimal unitPrice) {
        this.quotation = quotation;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateAmounts();
    }
    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateAmounts();
    }

    public void updateUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateAmounts();
    }

    private void calculateAmounts() {
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
}
