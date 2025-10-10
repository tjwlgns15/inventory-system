package com.yhs.inventroysystem.domain.price;

import com.yhs.inventroysystem.domain.exchange.Currency;
import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import com.yhs.inventroysystem.domain.client.Client;
import com.yhs.inventroysystem.domain.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "client_product_prices",
        uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "product_id"}))
@Getter
@NoArgsConstructor
public class ClientProductPrice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    public ClientProductPrice(Client client, Product product, BigDecimal unitPrice) {
        this.client = client;
        this.product = product;
        this.unitPrice = unitPrice;
    }

    public void updatePrice(BigDecimal newPrice) {
        this.unitPrice = newPrice;
    }

    public BigDecimal calculateTotalPrice(Integer quantity) {
        return this.unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Currency getCurrency() {
        return this.client.getCurrency();
    }

    public String getCurrencySymbol() {
        return this.client.getCurrency().getSymbol();
    }

    public String getCurrencyName() {
        return this.client.getCurrency().getName();
    }
}
