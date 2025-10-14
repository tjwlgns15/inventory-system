package com.yhs.inventroysystem.application.part;


import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.PartInUseException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.part.*;
import com.yhs.inventroysystem.domain.product.ProductPartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yhs.inventroysystem.application.part.PartCommands.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartService {

    private final PartRepository partRepository;
    private final ProductPartRepository productPartRepository;

    private final PartStockTransactionService partStockTransactionService;

    @Transactional
    public Part registerPart(PartRegisterCommand command) {
        validatePartCodeDuplication(command.partCode());
        validatePartNameDuplication(command.name());

        Part part = new Part(
                command.partCode(),
                command.name(),
                command.specification(),
                command.initialStock(),
                command.unit()
        );

        Part savedPart = partRepository.save(part);

        partStockTransactionService.recordTransaction(savedPart, TransactionType.INITIAL, 0, command.initialStock());

        return savedPart;
    }

    public List<Part> findAllPart() {
        return partRepository.findAllActive();
    }

    @Transactional
    public Part updatePart(Long partId, PartUpdateCommand command) {
        Part part = findPartById(partId);

        validatePartNameDuplicationForUpdate(partId, command.name());

        part.updateInfo(
                command.name(),
                command.specification(),
                command.unit()
        );

        return partRepository.save(part);
    }

    @Transactional
    public void increaseStock(Long partId, Integer quantity) {
        Part part = findPartById(partId);
        Integer beforeStock = part.getStockQuantity();

        part.increaseStock(quantity);

        partStockTransactionService.recordTransaction(part, TransactionType.INBOUND, beforeStock, quantity);
    }

    @Transactional
    public void decreaseStock(Long partId, Integer quantity) {
        Part part = findPartById(partId);
        Integer beforeStock = part.getStockQuantity();

        part.decreaseStock(quantity);

        partStockTransactionService.recordTransaction(part, TransactionType.OUTBOUND, beforeStock, -quantity);

    }

    @Transactional
    public void deletePart(Long partId) {
        Part part = findPartById(partId);

        // 매핑된 product 유무 확인
        if (productPartRepository.existsByPart(part)) {
            long usingProductCount = productPartRepository.countProductsByPart(part);
            throw PartInUseException.usedInProducts(part.getPartCode(), (int) usingProductCount);
        }

        part.markAsDeleted();
    }

    public List<PartStockTransaction> getPartStockTransactions(Long partId) {
        return partStockTransactionService.findByPartId(partId);
    }

    public List<PartStockTransaction> getAllStockTransactions() {
        return partStockTransactionService.findAll();
    }

    public Part findPartById(Long partId) {
        return partRepository.findByIdAndNotDeleted(partId)
                .orElseThrow(() -> ResourceNotFoundException.part(partId));
    }

    private void validatePartCodeDuplication(String partCode) {
        if (partRepository.existsByPartCodeAndNotDeleted(partCode)) {
            throw DuplicateResourceException.partCode(partCode);
        }
    }

    private void validatePartNameDuplication(String name) {
        if (partRepository.existsByNameAndNotDeleted(name)) {
            throw DuplicateResourceException.partName(name);
        }
    }

    private void validatePartCodeDuplicationForUpdate(Long partId, String partCode) {
        partRepository.findByPartCodeAndNotDeleted(partCode)
                .ifPresent(existingPart -> {
                    if (!existingPart.getId().equals(partId)) {
                        throw DuplicateResourceException.partCode(partCode);
                    }
                });
    }

    private void validatePartNameDuplicationForUpdate(Long partId, String name) {
        partRepository.findByNameAndNotDeleted(name)
                .ifPresent(existingPart -> {
                    if (!existingPart.getId().equals(partId)) {
                        throw DuplicateResourceException.partName(name);
                    }
                });
    }
}
