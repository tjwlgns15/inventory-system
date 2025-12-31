package com.yhs.inventroysystem.domain.shipment.entity;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "shipment_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false)
    private Integer sequence;

    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false, length = 300)
    private String productCode;

    @Column(nullable = false, length = 500)
    private String productDescription;

    @Column(length = 50)
    private String hsCode; // 관세 코드

    @Column(nullable = false, length = 20)
    private String unit; // 단위

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // ========== 중량 및 CBM 정보 ==========
    @Column(precision = 10, scale = 3)
    private BigDecimal netWeight; // 순중량 (Net Weight) - kg

    @Column(precision = 10, scale = 3)
    private BigDecimal grossWeight; // 총중량 (Gross Weight) - kg

    @Column(precision = 10, scale = 3)
    private BigDecimal cbm; // CBM (Cubic Meter)

    /**
     * ShipmentItem 생성
     */
    public static ShipmentItem create(Integer sequence, Long productId,
                                      String productCode, String productDescription,
                                      String hsCode, String unit,
                                      Integer quantity, BigDecimal unitPrice,
                                      BigDecimal netWeight, BigDecimal grossWeight, BigDecimal cbm) {
        ShipmentItem item = new ShipmentItem();
        item.sequence = sequence;
        item.productId = productId;
        item.productCode = productCode;
        item.productDescription = productDescription;
        item.hsCode = hsCode;
        item.unit = unit;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        item.amount = item.calculateAmount(quantity, unitPrice);
        item.netWeight = netWeight;
        item.grossWeight = grossWeight;
        item.cbm = cbm;

        return item;
    }

    /**
     * Shipment 할당 (연관관계 편의 메서드)
     */
    public void assignToShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    /**
     * 제품 정보 수정
     */
    public void update(Integer sequence, String productCode, String productDescription,
                       String hsCode, String unit,
                       Integer quantity, BigDecimal unitPrice,
                       BigDecimal netWeight, BigDecimal grossWeight, BigDecimal cbm) {
        this.sequence = sequence;
        this.productCode = productCode;
        this.productDescription = productDescription;
        this.hsCode = hsCode;
        this.unit = unit;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = calculateAmount(quantity, unitPrice);
        this.netWeight = netWeight;
        this.grossWeight = grossWeight;
        this.cbm = cbm;
    }

    /**
     * 수량 변경
     */
    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
        this.amount = calculateAmount(quantity, this.unitPrice);
    }

    /**
     * 단가 변경
     */
    public void updateUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        this.amount = calculateAmount(this.quantity, unitPrice);
    }

    /**
     * 중량 및 CBM 정보 업데이트
     */
    public void updateWeightAndCbm(BigDecimal netWeight, BigDecimal grossWeight, BigDecimal cbm) {
        this.netWeight = netWeight;
        this.grossWeight = grossWeight;
        this.cbm = cbm;
    }

    /**
     * 금액 계산
     */
    private BigDecimal calculateAmount(Integer quantity, BigDecimal unitPrice) {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 금액 재계산 (외부 호출용)
     */
    public void recalculateAmount() {
        this.amount = calculateAmount(this.quantity, this.unitPrice);
    }
}
