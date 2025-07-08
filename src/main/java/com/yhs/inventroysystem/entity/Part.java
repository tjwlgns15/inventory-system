package com.yhs.inventroysystem.entity;

import com.yhs.inventroysystem.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;

@Entity @Getter
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE part SET deleted_at = NOW() WHERE id = ?")
public class Part extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 고유 ID (PK)
    private Long id;

    private String partCode; // 부품 번호 (예: P1001 등)

    private String name; // 부품 이름

    private int stock; // 현재 재고 수량

    private int initialQty; // 초기 등록 수량

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PartCategory category;

    public static Part create(String partCode, String name, int initialQty, PartCategory category) {
        validatePartCode(partCode);
        validateName(name);
        validateQuantity(initialQty);
        validateCategory(category);

        return new Part(null, partCode, name, initialQty, initialQty, category);
    }

    // 비즈니스 메서드
    public void updateName(String name) {
        validateName(name);

        this.name = name;
    }

    public void updateCategory(PartCategory category) {
        validateCategory(category);

        this.category = category;
    }

    public void increaseStock(int quantity) {
        validateQuantity(quantity);
        ensureNotDeleted();

        this.stock += quantity;
    }

    public void decreaseStock(int quantity) {
        validateQuantity(quantity);
        ensureNotDeleted();

        if (this.stock < quantity) {
            throw new IllegalArgumentException(
                    String.format("재고가 부족합니다. 현재 재고: %d, 요청 수량: %d", this.stock, quantity)
            );
        }

        this.stock -= quantity;
    }

    public void adjustStock(int newStock) {
        validateQuantity(newStock);
        ensureNotDeleted();

        this.stock = newStock;
    }

    public boolean hasStock() {
        return stock > 0;
    }

    public boolean hasSufficientStock(int requiredQuantity) {
        return stock >= requiredQuantity;
    }

    // 검증 메서드
    private static void validatePartCode(String partCode) {
        if (partCode == null || partCode.trim().isEmpty()) {
            throw new IllegalArgumentException("부품 코드는 필수입니다");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("부품명은 필수입니다");
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("수량은 0 이상이어야 합니다");
        }
    }

    private static void validateCategory(PartCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다");
        }
    }

}
