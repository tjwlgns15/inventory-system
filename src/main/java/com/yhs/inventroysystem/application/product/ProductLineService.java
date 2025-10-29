package com.yhs.inventroysystem.application.product;

import com.yhs.inventroysystem.application.product.ProductLineCommands.PLRegisterCommand;
import com.yhs.inventroysystem.application.product.ProductLineCommands.PLUpdateCommand;
import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.part.Part;
import com.yhs.inventroysystem.domain.product.ProductLine;
import com.yhs.inventroysystem.domain.product.ProductLineRepository;
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

    private final ProductLineRepository productLineRepository;

    @Transactional
    public ProductLine registerProductLine(PLRegisterCommand command) {
        validateDuplicateName(command.name());

        ProductLine productLine = new ProductLine(command.name());
        return productLineRepository.save(productLine);
    }

    public List<ProductLine> getAllProductLines() {
        return productLineRepository.findAll();
    }

    @Transactional
    public ProductLine updateProductLine(Long id, PLUpdateCommand command) {
        ProductLine productLine = findProductLineById(id);

        validateDuplicateNameForUpdate(id, command.name());
        productLine.updateName(command.name());
        return productLine;
    }

    @Transactional
    public void deleteProductLine(Long id) {
        productLineRepository.deleteById(id);
    }

    private ProductLine findProductLineById(Long id) {
        return productLineRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.productLine(id));
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
