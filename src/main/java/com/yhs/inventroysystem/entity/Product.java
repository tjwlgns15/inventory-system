package com.yhs.inventroysystem.entity;

import com.yhs.inventroysystem.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.util.List;
import java.util.ArrayList;

@Entity @Getter
@AllArgsConstructor @NoArgsConstructor
@SQLDelete(sql = "UPDATE product SET deleted_at = NOW() WHERE id = ?")
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int stock; // 완재품

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductPart> productParts = new ArrayList<>();


    // 팩토리 메서드
    public static Product create(String name, int initialQty) {
        validateName(name);
        return new Product(null, name, initialQty, new ArrayList<>());
    }

    // 비즈니스 메서드
    public void updateName(String name) {
        validateName(name);
        ensureNotDeleted();

        this.name = name;
    }

    public void addProductPart(Part part, int count) {
        if (part == null) {
            throw new IllegalArgumentException("부품은 필수입니다");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다");
        }
        ensureNotDeleted();

        // 중복 부품 확인
        if (hasProductPart(part)) {
            throw new IllegalArgumentException("이미 등록된 부품입니다");
        }

        ProductPart productPart = ProductPart.create(this, part, count);
        this.productParts.add(productPart);
    }

    public void removeProductPart(Part part) {
        if (part == null) {
            throw new IllegalArgumentException("부품은 필수입니다");
        }
        ensureNotDeleted();

        this.productParts.removeIf(pp -> pp.getPart().equals(part));
    }

    public void updateProductPartCount(Part part, int newCount) {
        if (part == null) {
            throw new IllegalArgumentException("부품은 필수입니다");
        }
        if (newCount <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다");
        }
        ensureNotDeleted();

        ProductPart productPart = findProductPart(part);
        if (productPart == null) {
            throw new IllegalArgumentException("등록되지 않은 부품입니다");
        }

        productPart.updateCount(newCount);
    }

    public boolean canProduce(int quantity) {
        if (quantity <= 0) {
            return false;
        }

        return productParts.stream()
                .allMatch(pp -> pp.getPart().hasSufficientStock(pp.getCount() * quantity));
    }

    public void produceProduct(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("생산 수량은 1 이상이어야 합니다");
        }
        ensureNotDeleted();

        if (!canProduce(quantity)) {
            throw new IllegalArgumentException("부품 재고가 부족하여 생산할 수 없습니다");
        }

        // 부품 재고 차감
        for (ProductPart pp : productParts) {
            pp.getPart().decreaseStock(pp.getCount() * quantity);
        }

        // 제품 재고 증가
        this.stock += quantity;
    }

    public void removeProduct(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("제거 수량은 1 이상이어야 합니다");
        }
        if (this.stock < quantity) {
            throw new IllegalArgumentException("제품 재고가 부족합니다");
        }
        ensureNotDeleted();

        this.stock -= quantity;
    }

    public void adjustStock(int newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다");
        }
        ensureNotDeleted();

        this.stock = newStock;
    }

    public boolean hasStock() {
        return stock > 0;
    }

    public boolean hasProductPart(Part part) {
        return productParts.stream()
                .anyMatch(pp -> pp.getPart().equals(part));
    }

    private ProductPart findProductPart(Part part) {
        return productParts.stream()
                .filter(pp -> pp.getPart().equals(part))
                .findFirst()
                .orElse(null);
    }

    // 검증 메서드
    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("제품명은 필수입니다");
        }
    }

}
