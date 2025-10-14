package com.yhs.inventroysystem.domain.part;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity @Getter
@Table(name = "part_stock_transactions")
@NoArgsConstructor
public class PartStockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private int beforeStock;

    @Column(nullable = false)
    private int changeQuantity;

    @Column(nullable = false)
    private int afterStock;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // 팩토리 메서드
    public static PartStockTransaction create(Part part, TransactionType type, int beforeStock, int changeQuantity, int afterStock) {
        validatePart(part);
        validateType(type);
        validateStockValues(beforeStock, changeQuantity, afterStock);

        return new PartStockTransaction(part, type, beforeStock, changeQuantity, afterStock, LocalDateTime.now());
    }

    public PartStockTransaction(Part part, TransactionType type, int beforeStock, int changeQuantity, int afterStock, LocalDateTime createdAt) {
        this.part = part;
        this.type = type;
        this.beforeStock = beforeStock;
        this.changeQuantity = changeQuantity;
        this.afterStock = afterStock;
        this.createdAt = createdAt;
    }

    // 검증 메서드
    private static void validatePart(Part part) {
        if (part == null) {
            throw new IllegalArgumentException("부품은 필수입니다");
        }
    }

    private static void validateType(TransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException("거래 유형은 필수입니다");
        }
    }

    private static void validateStockValues(int beforeStock, int changeQuantity, int afterStock) {
        if (beforeStock < 0) {
            throw new IllegalArgumentException("이전 재고는 0 이상이어야 합니다");
        }
        if (afterStock < 0) {
            throw new IllegalArgumentException("이후 재고는 0 이상이어야 합니다");
        }
        if (beforeStock + changeQuantity != afterStock) {
            throw new IllegalArgumentException("재고 계산이 올바르지 않습니다");
        }
    }
}


