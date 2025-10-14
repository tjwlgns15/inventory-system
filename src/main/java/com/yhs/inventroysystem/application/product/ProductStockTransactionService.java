package com.yhs.inventroysystem.application.product;

import com.yhs.inventroysystem.domain.product.Product;
import com.yhs.inventroysystem.domain.product.ProductStockTransaction;
import com.yhs.inventroysystem.domain.product.ProductStockTransactionRepository;
import com.yhs.inventroysystem.domain.product.ProductTransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductStockTransactionService {

    private final ProductStockTransactionRepository productStockTransactionRepository;


    @Transactional
    public void recordTransaction(Product product, ProductTransactionType type, int beforeStock, int changeQuantity) {
        int afterStock = product.getStockQuantity();

        ProductStockTransaction transaction = ProductStockTransaction.create(
                product,
                type,
                beforeStock,
                changeQuantity,
                afterStock
        );

        productStockTransactionRepository.save(transaction);
    }

    public List<ProductStockTransaction> findByProductId(Long partId) {
        return productStockTransactionRepository.findByProductIdOrderByCreatedAtDesc(partId);
    }

    public List<ProductStockTransaction> findAll() {
        return productStockTransactionRepository.findAllByOrderByCreatedAtDesc();
    }
}
