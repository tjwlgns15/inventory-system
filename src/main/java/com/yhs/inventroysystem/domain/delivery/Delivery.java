package com.yhs.inventroysystem.domain.delivery;

import com.yhs.inventroysystem.domain.exception.InvalidDeliveryStateException;
import com.yhs.inventroysystem.domain.task.Task;
import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import com.yhs.inventroysystem.domain.client.Client;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deliveries")
@Getter
@NoArgsConstructor
public class Delivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deliveryNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(precision = 15, scale = 2)
    private BigDecimal subtotalAmount; // 할인 전 금액 (DeliveryItem 합계)

    @Column(precision = 15, scale = 2)
    private BigDecimal totalDiscountAmount; // 전체 할인액

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount; // 최종 금액

    @Column(length = 200)
    private String discountNote; // 전체 할인 사유

    // 거래 시점의 환율
    @Column(precision = 15, scale = 6)
    private BigDecimal exchangeRate;

    // 원화 환산 금액
    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmountKRW;

    // 수주받은 날짜
    @Column(nullable = false)
    private LocalDate orderedAt;

    // 출하 요청 날짜
    private LocalDate requestedAt;

    // 실제 출하된 날짜
    private LocalDateTime deliveredAt;

    @Column(length = 500)
    private String memo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_task_id")
    private Task orderTask;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_task_id")
    private Task shipmentTask;

    public Delivery(String deliveryNumber, Client client, LocalDate orderedAt, LocalDate requestedAt) {
        this.deliveryNumber = deliveryNumber;
        this.client = client;
        this.orderedAt = orderedAt;
        this.requestedAt = requestedAt;
        this.status = DeliveryStatus.PENDING;
        this.subtotalAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.totalAmountKRW = BigDecimal.ZERO;
    }

    /**
     * 일괄 등록용 생성자 (status, deliveredAt 포함)
     * 과거 데이터 마이그레이션 등에 사용
     */
    public Delivery(String deliveryNumber, Client client, LocalDate orderedAt, LocalDate requestedAt,
                    DeliveryStatus status, LocalDateTime deliveredAt) {
        this.deliveryNumber = deliveryNumber;
        this.client = client;
        this.orderedAt = orderedAt;
        this.requestedAt = requestedAt;
        this.status = status != null ? status : DeliveryStatus.PENDING;
        this.deliveredAt = deliveredAt;
        this.subtotalAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.totalAmountKRW = BigDecimal.ZERO;
    }

    public void addItem(DeliveryItem item) {
        this.items.add(item);
        calculateTotalAmount();
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void complete() {
        if (this.status != DeliveryStatus.PENDING) {
            throw new InvalidDeliveryStateException(this.status, "완료");
        }
        this.status = DeliveryStatus.COMPLETED;
        this.deliveredAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status != DeliveryStatus.PENDING) {
            throw new InvalidDeliveryStateException(this.status, "취소");
        }
        this.status = DeliveryStatus.CANCELLED;
    }

    // 전체 할인 적용 (금액)
    public void applyDiscount(BigDecimal discountAmount, String note) {
        validatePendingStatus("할인 적용");
        this.totalDiscountAmount = discountAmount;
        this.discountNote = note;
        calculateTotalAmount();
    }

    // 전체 할인 적용 (비율)
    public void applyDiscountRate(BigDecimal discountRate, String note) {
        validatePendingStatus("할인 적용");
        this.totalDiscountAmount = this.subtotalAmount
                .multiply(discountRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        this.discountNote = note;
        calculateTotalAmount();
    }

    // 할인 제거
    public void clearDiscount() {
        this.totalDiscountAmount = null;
        this.discountNote = null;
        calculateTotalAmount();
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
        calculateKRWAmount();
    }

    public void setOrderTask(Task task) {
        this.orderTask = task;
    }

    public void setShipmentTask(Task task) {
        this.shipmentTask = task;
    }

    public void clearShipmentTask() {
        this.shipmentTask = null;
    }


    private void calculateTotalAmount() {
        // 1. 각 항목의 금액 합계
        this.subtotalAmount = items.stream()
                .map(DeliveryItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. 전체 할인 적용
        BigDecimal discount = this.totalDiscountAmount != null
                ? this.totalDiscountAmount
                : BigDecimal.ZERO;

        this.totalAmount = this.subtotalAmount.subtract(discount); // 전체 할인액 차감

        // 3. 원화 환산 금액 재계산
        calculateKRWAmount();
    }

    private void calculateKRWAmount() {
        if (this.exchangeRate != null && this.totalAmount != null) {
            this.totalAmountKRW = this.totalAmount
                    .multiply(this.exchangeRate)
                    .setScale(0, RoundingMode.HALF_UP);
        }
    }

    private void validatePendingStatus(String action) {
        if (this.status != DeliveryStatus.PENDING) {
            throw new InvalidDeliveryStateException(this.status, action);
        }
    }

    public boolean hasDiscount() {
        return this.totalDiscountAmount != null
                && this.totalDiscountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getTotalDiscountRate() {
        if (!hasDiscount() || subtotalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalDiscountAmount
                .divide(subtotalAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}