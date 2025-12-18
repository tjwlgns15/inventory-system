package com.yhs.inventroysystem.domain.product.service;

import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.product.entity.Product;
import com.yhs.inventroysystem.domain.product.entity.ProductCategory;
import com.yhs.inventroysystem.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDomainService {

    private final ProductRepository productRepository;


    @Transactional
    public Product createProduct(ProductCategory productCategory,
                                        String productCode,
                                        String name,
                                        BigDecimal defaultUnitPrice,
                                        String description,
                                        Integer initialStock) {

        validateProductCodeDuplication(productCode);
        validateProductNameDuplication(name);

        Product product = new Product(
                productCategory,
                productCode,
                name,
                defaultUnitPrice,
                description,
                initialStock
        );

        return productRepository.save(product);
    }


    public List<Product> findAllActive() {
        return productRepository.findAllActive();
    }
    public List<Product> findAllActive(Sort sort) {
        return productRepository.findAllActive(sort);
    }

    public Page<Product> findAllActive(Pageable pageable) {
        return productRepository.findAllActive(pageable);
    }

    public Page<Product> searchByKeyword(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable);
    }

    public List<Product> findAllActiveWithPartOrderByDisplayOrder() {
        return productRepository.findAllActiveWithPartOrderByDisplayOrder();
    }

    public Product findProductWithParts(Long productId) {
        return productRepository.findByIdWithPartsAndNotDeleted(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));
    }

    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));
    }

    public void validateProductNameDuplicationForUpdate(Long productId, String productName) {
        productRepository.findByNameAndNotDeleted(productName)
                .ifPresent(existingProduct -> {
                    if (!existingProduct.getId().equals(productId)) {
                        throw DuplicateResourceException.productName(productName);
                    }
                });
    }

    /*
        Private Method
     */
    private void validateProductCodeDuplication(String productCode) {
        if (productRepository.existsByProductCodeAndNotDeleted(productCode)) {
            throw DuplicateResourceException.productCode(productCode);
        }
    }

    private void validateProductNameDuplication(String productName) {
        if (productRepository.existsByNameAndNotDeleted(productName)) {
            throw DuplicateResourceException.productName(productName);
        }
    }
}