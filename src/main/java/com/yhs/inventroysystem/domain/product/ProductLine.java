package com.yhs.inventroysystem.domain.product;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import com.yhs.inventroysystem.presentation.product.ProductLineDtos;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_lines")
@Getter
@NoArgsConstructor
public class ProductLine extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    public ProductLine(String name) {
        this.name = name;
    }

    public void updateName(String name) {
        this.name = name;
    }
}
