package com.yhs.inventroysystem.application.part;


import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.PartInUseException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.part.*;
import com.yhs.inventroysystem.domain.product.ProductPartRepository;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.yhs.inventroysystem.application.part.PartCommands.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartService {

    private final PartRepository partRepository;
    private final ProductPartRepository productPartRepository;

    private final PartStockTransactionService partStockTransactionService;

    private final FileStorageService fileStorageService;

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

        // 이미지 파일이 있으면 저장
        MultipartFile imageFile = command.imageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            String storedFileName = fileStorageService.storeFile(imageFile);
            part.updateImage(storedFileName, imageFile.getOriginalFilename());
        }

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

        // 새 이미지가 있으면 기존 이미지 삭제 후 저장
        MultipartFile imageFile = command.imageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 이미지 삭제
            if (part.getImagePath() != null) {
                fileStorageService.deleteFile(part.getImagePath());
            }

            // 새 이미지 저장
            String storedFileName = fileStorageService.storeFile(imageFile);
            part.updateImage(storedFileName, imageFile.getOriginalFilename());
        }

        return partRepository.save(part);
    }

    @Transactional
    public void deletePartImage(Long partId) {
        Part part = findPartById(partId);

        if (part.getImagePath() != null) {
            fileStorageService.deleteFile(part.getImagePath());
            part.removeImage();
        }
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

        // 이미지 파일 삭제
        if (part.getImagePath() != null) {
            fileStorageService.deleteFile(part.getImagePath());
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

    private void validatePartNameDuplicationForUpdate(Long partId, String name) {
        partRepository.findByNameAndNotDeleted(name)
                .ifPresent(existingPart -> {
                    if (!existingPart.getId().equals(partId)) {
                        throw DuplicateResourceException.partName(name);
                    }
                });
    }
}
