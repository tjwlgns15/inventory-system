package com.yhs.inventroysystem.entity;

import com.yhs.inventroysystem.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductPart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(nullable = false)
    private int count;

    // 팩토리 메서드
    public static ProductPart create(Product product, Part part, int count) {
        validateProduct(product);
        validatePart(part);
        validateCount(count);

        return new ProductPart(null, product, part, count);
    }

    // 비즈니스 메서드
    public void updateCount(int newCount) {
        validateCount(newCount);

        this.count = newCount;
    }

    public int getTotalRequiredQuantity(int productQuantity) {
        return count * productQuantity;
    }

    // 검증 메서드
    private static void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("제품은 필수입니다");
        }
    }

    private static void validatePart(Part part) {
        if (part == null) {
            throw new IllegalArgumentException("부품은 필수입니다");
        }
    }

    private static void validateCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다");
        }
    }
}
