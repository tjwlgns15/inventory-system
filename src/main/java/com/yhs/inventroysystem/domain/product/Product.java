package com.yhs.inventroysystem.domain.product;

import com.yhs.inventroysystem.domain.exception.InsufficientStockException;
import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "product_category")
    private ProductCategory productCategory;

    @ManyToOne
    @JoinColumn(name = "product_line_id")
    private ProductLine productLine;

    @Column(nullable = false, unique = true)
    private String productCode;

    @Column(nullable = false)
    private String name;

    private BigDecimal defaultUnitPrice;

    private String description;

    @Column(nullable = false)
    private Integer stockQuantity;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductPart> partMappings = new ArrayList<>();

    public Product(ProductCategory productCategory, ProductLine productLine,
                   String productCode, String name, BigDecimal defaultUnitPrice,
                   String description, Integer initialStock) {
        validateProductCategory(productCategory);
        validateProductCode(productCode);
        validateName(name);

        this.productCategory = productCategory;
        this.productLine = productLine;
        this.productCode = productCode;
        this.name = name;
        this.defaultUnitPrice = defaultUnitPrice;
        this.description = description;
        this.stockQuantity = initialStock;
    }

    private void validateProductCategory(ProductCategory productCategory) {
        if (productCategory == null) {
            throw new IllegalArgumentException("제품 카테고리는 필수입니다.");
        }
    }

    private void validateProductCode(String productCode) {
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new IllegalArgumentException("제품 코드는 필수입니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("제품명은 필수입니다.");
        }
    }

    public void addPartMapping(ProductPart mapping) {
        ensureNotDeleted();
        this.partMappings.add(mapping);
    }

    public void decreaseStock(Integer quantity) {
        ensureNotDeleted();
        if (this.stockQuantity < quantity) {
            throw InsufficientStockException.insufficientStock(this.name, quantity, this.stockQuantity);
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(Integer quantity) {
        ensureNotDeleted();
        this.stockQuantity += quantity;
    }

    public void updateInfo(String name, BigDecimal defaultUnitPrice, String description) {
        ensureNotDeleted();
        this.name = name;
        this.defaultUnitPrice = defaultUnitPrice;
        this.description = description;
    }

    public void changeCategory(ProductCategory category) {
        ensureNotDeleted();
        validateProductCategory(category);
        this.productCategory = category;
    }

    public void assignProductLine(ProductLine productLine) {
        ensureNotDeleted();
        this.productLine = productLine;
    }

    public void removeProductLine() {
        ensureNotDeleted();
        this.productLine = null;
    }

    public void clearPartMappings() {
        ensureNotDeleted();
        this.partMappings.clear();
    }

    public void markAsDeleted() {
        ensureNotDeleted();
        this.productCode = this.productCode + "_DELETED_" + System.currentTimeMillis();
        this.deletedAt = LocalDateTime.now();
        this.partMappings.clear();
    }
}
