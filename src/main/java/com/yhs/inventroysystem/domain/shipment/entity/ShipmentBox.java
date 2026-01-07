package com.yhs.inventroysystem.domain.shipment.entity;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 자주 사용하는 박스 사이즈 템플릿
 * - Shipment 작성 시 선택 가능
 * - 선택하지 않고 직접 입력도 가능
 */
@Entity
@Table(name = "shipment_box_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentBox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal width;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal length;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal height;

    @Column(nullable = false)
    private Boolean isActive = true;

    // ========== 정적 팩토리 메서드 ==========

    public static ShipmentBox create(String title, BigDecimal width, BigDecimal length,
                                     BigDecimal height) {
        ShipmentBox box = new ShipmentBox();
        box.title = title;
        box.width = width;
        box.length = length;
        box.height = height;
        box.isActive = true;

        return box;
    }

    // ========== 비즈니스 메서드 ==========

    public void update(String title, BigDecimal width, BigDecimal length,
                       BigDecimal height) {
        this.title = title;
        this.width = width;
        this.length = length;
        this.height = height;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public String getDimensionString() {
        return String.format("%s x %s x %s Cm",
                width.stripTrailingZeros().toPlainString(),
                length.stripTrailingZeros().toPlainString(),
                height.stripTrailingZeros().toPlainString()
        );
    }

    public BigDecimal calculateCbm() {
        return width.multiply(length)
                .multiply(height)
                .divide(BigDecimal.valueOf(1_000_000), 6, RoundingMode.HALF_UP);
    }
}