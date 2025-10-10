package com.yhs.inventroysystem.domain.product;

import com.yhs.inventroysystem.domain.exception.InsufficientStockException;
import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    public Product(String productCode, String name, BigDecimal defaultUnitPrice, String description, Integer initialStock) {
        this.productCode = productCode;
        this.name = name;
        this.defaultUnitPrice = defaultUnitPrice;
        this.description = description;
        this.stockQuantity = initialStock;
    }

    public void addPartMapping(ProductPart mapping) {
        this.partMappings.add(mapping);
    }

    public void decreaseStock(Integer quantity) {
        if (this.stockQuantity < quantity) {
            throw new InsufficientStockException(this.name, quantity, this.stockQuantity);
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(Integer quantity) {
        this.stockQuantity += quantity;
    }

    public void updateInfo(String name, BigDecimal defaultUnitPrice, String description) {
        this.name = name;
        this.defaultUnitPrice = defaultUnitPrice;
        this.description = description;
    }

    public void clearPartMappings() {
        this.partMappings.clear();
    }

}
