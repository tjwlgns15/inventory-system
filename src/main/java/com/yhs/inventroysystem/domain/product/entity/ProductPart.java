package com.yhs.inventroysystem.domain.product.entity;

import com.yhs.inventroysystem.domain.part.entity.Part;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_part_mappings")
@Getter
@NoArgsConstructor
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
    private Integer requiredQuantity; // 제품 1개당 필요한 부품 수량

    public ProductPart(Product product, Part part, Integer requiredQuantity) {
        this.product = product;
        this.part = part;
        this.requiredQuantity = requiredQuantity;
    }

    public Integer calculateTotalRequired(Integer productQuantity) {
        return this.requiredQuantity * productQuantity;
    }
}
