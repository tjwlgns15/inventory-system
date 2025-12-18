package com.yhs.inventroysystem.domain.part.entity;

import com.yhs.inventroysystem.domain.exception.InsufficientStockException;
import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity @Getter
@Table(name = "parts")
@NoArgsConstructor
public class Part extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String partCode;

    @Column(nullable = false)
    private String name;

    private String specification;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private String unit; // 단위 (개, kg, m 등)

    private String imagePath;

    private String originalImageName;

    public Part(String partCode, String name, String specification, Integer initialStock, String unit) {
        this.partCode = partCode;
        this.name = name;
        this.specification = specification;
        this.stockQuantity = initialStock;
        this.unit = unit;
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

    public void updateStockQuantity(Integer stockQuantity) {
        ensureNotDeleted();
        this.stockQuantity = stockQuantity;
    }

    public void updateInfo(String name, String specification, Integer stockQuantity, String unit) {
        ensureNotDeleted();
        this.name = name;
        this.specification = specification;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
    }

    public void updateImage(String imagePath, String originalImageName) {
        ensureNotDeleted();
        this.imagePath = imagePath;
        this.originalImageName = originalImageName;
    }

    public void removeImage() {
        ensureNotDeleted();
        this.imagePath = null;
        this.originalImageName = null;
    }

    public void markAsDeleted() {
        ensureNotDeleted();
        this.partCode = this.partCode + "_DELETED_" + System.currentTimeMillis();
        this.deletedAt = LocalDateTime.now();
    }
}
