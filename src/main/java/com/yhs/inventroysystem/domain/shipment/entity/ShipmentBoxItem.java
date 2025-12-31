package com.yhs.inventroysystem.domain.shipment.entity;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Shipment에 실제로 들어가는 박스 정보
 * - 템플릿에서 선택하거나 직접 입력
 * - 선택 후에도 사이즈 수정 가능
 */
@Entity
@Table(name = "shipment_box_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentBoxItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false)
    private Integer sequence;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal width;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal length;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal height;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(nullable = false)
    private Integer quantity;

    // 어떤 템플릿에서 가져왔는지 추적 (null이면 직접 입력)
    @Column(name = "box_template_id")
    private Long boxTemplateId;

    // ========== 정적 팩토리 메서드 ==========

    /**
     * 직접 입력으로 생성
     */
    public static ShipmentBoxItem createDirect(Integer sequence, String title,
                                               BigDecimal width, BigDecimal length,
                                               BigDecimal height, BigDecimal weight,
                                               Integer quantity) {
        ShipmentBoxItem boxItem = new ShipmentBoxItem();
        boxItem.sequence = sequence;
        boxItem.title = title;
        boxItem.width = width;
        boxItem.length = length;
        boxItem.height = height;
        boxItem.weight = weight;
        boxItem.quantity = quantity;
        boxItem.boxTemplateId = null;  // 직접 입력

        return boxItem;
    }

    /**
     * 템플릿에서 선택하여 생성 (사이즈 그대로 복사)
     */
    public static ShipmentBoxItem createFromTemplate(Integer sequence, ShipmentBox template,
                                                     Integer quantity) {
        ShipmentBoxItem boxItem = new ShipmentBoxItem();
        boxItem.sequence = sequence;
        boxItem.title = template.getTitle();
        boxItem.width = template.getWidth();
        boxItem.length = template.getLength();
        boxItem.height = template.getHeight();
        boxItem.weight = template.getWeight();
        boxItem.quantity = quantity;
        boxItem.boxTemplateId = template.getId();  // 템플릿 ID 기록

        return boxItem;
    }

    /**
     * 템플릿에서 선택 후 사이즈 커스터마이징
     */
    public static ShipmentBoxItem createFromTemplateWithCustomSize(
            Integer sequence, ShipmentBox template,
            BigDecimal customWidth, BigDecimal customLength,
            BigDecimal customHeight, BigDecimal customWeight,
            Integer quantity) {
        ShipmentBoxItem boxItem = new ShipmentBoxItem();
        boxItem.sequence = sequence;
        boxItem.title = template.getTitle();
        boxItem.width = customWidth;
        boxItem.length = customLength;
        boxItem.height = customHeight;
        boxItem.weight = customWeight;
        boxItem.quantity = quantity;
        boxItem.boxTemplateId = template.getId();  // 템플릿 기반임을 표시

        return boxItem;
    }

    // ========== 비즈니스 메서드 ==========

    public void assignToShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public void update(Integer sequence, String title,
                       BigDecimal width, BigDecimal length,
                       BigDecimal height, BigDecimal weight,
                       Integer quantity) {
        this.sequence = sequence;
        this.title = title;
        this.width = width;
        this.length = length;
        this.height = height;
        this.weight = weight;
        this.quantity = quantity;
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

    public BigDecimal calculateTotalCbm() {
        return calculateCbm().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * 템플릿에서 가져온 박스인지 확인
     */
    public boolean isFromTemplate() {
        return boxTemplateId != null;
    }

}
