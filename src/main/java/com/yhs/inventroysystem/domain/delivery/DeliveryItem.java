package com.yhs.inventroysystem.domain.delivery;

import com.yhs.inventroysystem.domain.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "delivery_items")
@Getter
@NoArgsConstructor
public class DeliveryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseUnitPrice; // 기준 단가

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal actualUnitPrice; // 실제 적용 단가 (할인/ 서비스 등 반영)

    @Column(precision = 15, scale = 2)
    private BigDecimal discountAmount; // 할인 금액

    @Column(length = 200)
    private String priceNote; // 금액 변경에 대한 메모 (신규 할인, 대량 구매 등)

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private Boolean isFreeItem;

    public DeliveryItem(Delivery delivery, Product product, Integer quantity,
                        BigDecimal baseUnitPrice, BigDecimal actualUnitPrice,
                        String priceNote) {
        this.delivery = delivery;
        this.product = product;
        this.quantity = quantity;
        this.baseUnitPrice = baseUnitPrice;
        this.actualUnitPrice = actualUnitPrice;
        this.priceNote = priceNote;
        this.isFreeItem = false;
        calculateAmounts();
    }

    // 기준가와 실제가가 동일한 경우 (할인 없음)
    public DeliveryItem(Delivery delivery, Product product, Integer quantity, BigDecimal unitPrice) {
        this(delivery, product, quantity, unitPrice, unitPrice, null);
    }

    // 무상 제공 항목 생성
    public static DeliveryItem createFreeItem(Delivery delivery, Product product,
                                              Integer quantity, String note) {
        DeliveryItem item = new DeliveryItem();
        item.delivery = delivery;
        item.product = product;
        item.quantity = quantity;
        item.baseUnitPrice = BigDecimal.ZERO;
        item.actualUnitPrice = BigDecimal.ZERO;
        item.discountAmount = BigDecimal.ZERO;
        item.totalPrice = BigDecimal.ZERO;
        item.priceNote = note;
        item.isFreeItem = true;
        return item;
    }

    // 가격 조정 (할인/할증) - 생성 후에도 조정 가능하도록
    public void adjustPrice(BigDecimal newActualPrice, String note) {
        this.actualUnitPrice = newActualPrice;
        this.priceNote = note;
        calculateAmounts();
    }

    private void calculateAmounts() {
        this.discountAmount = this.baseUnitPrice.subtract(this.actualUnitPrice);
        this.totalPrice = this.actualUnitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    public boolean isDiscounted() {
        return this.discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isSurcharge() {
        return this.discountAmount.compareTo(BigDecimal.ZERO) < 0;
    }

    public BigDecimal getDiscountRate() {
        if (baseUnitPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return discountAmount
                .divide(baseUnitPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
