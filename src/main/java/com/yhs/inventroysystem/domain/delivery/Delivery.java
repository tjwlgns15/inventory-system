package com.yhs.inventroysystem.domain.delivery;

import com.yhs.inventroysystem.domain.exception.InvalidDeliveryStateException;
import com.yhs.inventroysystem.domain.task.Task;
import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import com.yhs.inventroysystem.domain.client.Client;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
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
    private BigDecimal totalAmount;

    // 거래 시점의 환율
    @Column(precision = 15, scale = 6)
    private BigDecimal exchangeRate;

    // 원화 환산 금액
    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmountKRW;

    private LocalDateTime deliveredAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task relatedTask;

    public Delivery(String deliveryNumber, Client client) {
        this.deliveryNumber = deliveryNumber;
        this.client = client;
        this.status = DeliveryStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.totalAmountKRW = BigDecimal.ZERO;
    }

    public void addItem(DeliveryItem item) {
        this.items.add(item);
        calculateTotalAmount();
    }

    public void complete() {
        if (this.status != DeliveryStatus.PENDING) {
            throw new InvalidDeliveryStateException(this.status, "완료");
        }
        this.status = DeliveryStatus.COMPLETED;
        this.deliveredAt = LocalDateTime.now();
    }

    private void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(DeliveryItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 환율이 설정되어 있으면 원화 환산 금액 계산
        if (this.exchangeRate != null) {
            this.totalAmountKRW = this.totalAmount.multiply(this.exchangeRate);
        }
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
        // 환율 설정 시 원화 환산 금액 재계산
        if (this.totalAmount != null) {
            this.totalAmountKRW = this.totalAmount.multiply(exchangeRate);
        }
    }

    public void setRelatedTask(Task task) {
        this.relatedTask = task;
    }
}
