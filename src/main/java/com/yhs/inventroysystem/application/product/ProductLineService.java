package com.yhs.inventroysystem.application.product;

import com.yhs.inventroysystem.application.product.ProductLineCommands.PLRegisterCommand;
import com.yhs.inventroysystem.application.product.ProductLineCommands.PLUpdateCommand;
import com.yhs.inventroysystem.domain.product.entity.ProductLine;
import com.yhs.inventroysystem.domain.product.service.ProductLineDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductLineService {

    private final ProductLineDomainService productLineDomainService;


    @Transactional
    public ProductLine registerProductLine(PLRegisterCommand command) {
        return productLineDomainService.saveProductLine(command.name());
    }

    public List<ProductLine> getAllProductLines() {
        return productLineDomainService.findAllProductLines();
    }

    @Transactional
    public ProductLine updateProductLine(Long id, PLUpdateCommand command) {
        return productLineDomainService.updateProductLine(id, command.name());
    }

    @Transactional
    public void deleteProductLine(Long id) {
        productLineDomainService.deleteProductLine(id);
    }
}
