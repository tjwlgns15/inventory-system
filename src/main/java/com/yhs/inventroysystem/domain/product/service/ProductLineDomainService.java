package com.yhs.inventroysystem.domain.product.service;

import com.yhs.inventroysystem.application.product.ProductLineCommands.PLUpdateCommand;
import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.product.entity.ProductLine;
import com.yhs.inventroysystem.domain.product.repository.ProductLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductLineDomainService {

    private final ProductLineRepository productLineRepository;

    @Transactional
    public ProductLine saveProductLine(String name) {
        validateDuplicateName(name);

        ProductLine productLine = new ProductLine(name);
        return productLineRepository.save(productLine);
    }

    public ProductLine findById(Long id) {
        return productLineRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.productLine(id));
    }

    public List<ProductLine> findAllProductLines() {
        return productLineRepository.findAll();
    }

    @Transactional
    public ProductLine updateProductLine(Long id, String updateName) {
        ProductLine productLine = productLineRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.productLine(id));

        validateDuplicateNameForUpdate(id, updateName);
        productLine.updateName(updateName);
        return productLine;
    }

    @Transactional
    public void deleteProductLine(Long id) {
        productLineRepository.deleteById(id);
    }

    private void validateDuplicateName(String name) {
        if (productLineRepository.existsByName(name)) {
            throw DuplicateResourceException.productLine(name);
        }
    }

    private void validateDuplicateNameForUpdate(Long id, String name) {
        if (productLineRepository.existsByNameAndIdNot(name, id)) {
            throw DuplicateResourceException.productLine(name);
        }
    }
}
