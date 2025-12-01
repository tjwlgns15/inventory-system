package com.yhs.inventroysystem.application.part;


import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.PartInUseException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.part.Part;
import com.yhs.inventroysystem.domain.part.PartRepository;
import com.yhs.inventroysystem.domain.part.PartStockTransaction;
import com.yhs.inventroysystem.domain.part.TransactionType;
import com.yhs.inventroysystem.domain.product.ProductPartRepository;
import com.yhs.inventroysystem.infrastructure.file.FileStorageFactory;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageType;
import com.yhs.inventroysystem.infrastructure.file.FileUploadResult;
import com.yhs.inventroysystem.infrastructure.pagenation.PageableUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.yhs.inventroysystem.application.part.PartCommands.PartRegisterCommand;
import static com.yhs.inventroysystem.application.part.PartCommands.PartUpdateCommand;

@Service
@Transactional(readOnly = true)
public class PartService {

    private final PartRepository partRepository;
    private final ProductPartRepository productPartRepository;
    private final PartStockTransactionService partStockTransactionService;
    private final FileStorageService fileStorageService;

    public PartService(
            PartRepository partRepository,
            ProductPartRepository productPartRepository,
            PartStockTransactionService partStockTransactionService,
            FileStorageFactory fileStorageFactory) {
        this.partRepository = partRepository;
        this.productPartRepository = productPartRepository;
        this.partStockTransactionService = partStockTransactionService;
        this.fileStorageService = fileStorageFactory.getStorageService(FileStorageType.PART_IMAGE);
    }


    @Transactional
    public Part registerPart(PartRegisterCommand command) {
        validatePartCodeDuplication(command.partCode());

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
            FileUploadResult result = fileStorageService.store(
                    imageFile,
                    FileStorageType.PART_IMAGE.getDirectory());
            part.updateImage(result.getFilePath(), result.getOriginalFileName());
        }

        Part savedPart = partRepository.save(part);

        partStockTransactionService.recordTransaction(
                savedPart,
                TransactionType.INITIAL,
                0,
                command.initialStock()
        );

        return savedPart;
    }

    public List<Part> findAllPart(String sortBy, String direction) {
        Sort sort = PageableUtils.createSort(sortBy, direction);
        return partRepository.findAllActive(sort);
    }

    public Page<Part> searchParts(String keyword, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);
        return partRepository.searchByKeyword(keyword, pageable);
    }

    public Page<Part> findAllPartPaged(int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);
        return partRepository.findAllActive(pageable);
    }

    @Transactional
    public Part updatePart(Long partId, PartUpdateCommand command) {
        Part part = findPartById(partId);
        Integer beforeStock = part.getStockQuantity();

        part.updateInfo(
                command.name(),
                command.specification(),
                command.stockQuantity(),
                command.unit()
        );

        // 새 이미지가 있으면 기존 이미지 삭제 후 저장
        MultipartFile imageFile = command.imageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 이미지 삭제
            if (part.getImagePath() != null) {
                fileStorageService.delete(part.getImagePath());
            }

            // 새 이미지 저장
            FileUploadResult result = fileStorageService.store(
                    imageFile,
                    FileStorageType.PART_IMAGE.getDirectory()
            );
            part.updateImage(result.getFilePath(), result.getOriginalFileName());
        }

        Integer afterStock = part.getStockQuantity();
        if (!beforeStock.equals(afterStock)) {
            int changeQuantity = afterStock - beforeStock;
            partStockTransactionService.recordTransaction(
                    part,
                    TransactionType.ADJUSTMENT,
                    beforeStock,
                    changeQuantity
            );
        }

        return partRepository.save(part);
    }

    @Transactional
    public void deletePartImage(Long partId) {
        Part part = findPartById(partId);

        if (part.getImagePath() != null) {
            fileStorageService.delete(part.getImagePath());
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
            fileStorageService.delete(part.getImagePath());
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


    /*
        Private Method
     */

    private void validatePartCodeDuplication(String partCode) {
        if (partRepository.existsByPartCodeAndNotDeleted(partCode)) {
            throw DuplicateResourceException.partCode(partCode);
        }
    }
}
