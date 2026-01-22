package com.yhs.inventroysystem.application.part;


import com.yhs.inventroysystem.application.part.PartCommands.PartStockDecreaseCommand;
import com.yhs.inventroysystem.application.part.PartCommands.PartStockIncreaseCommand;
import com.yhs.inventroysystem.application.part.PartCommands.PartStockUpdateCommand;
import com.yhs.inventroysystem.domain.exception.PartInUseException;
import com.yhs.inventroysystem.domain.part.entity.Part;
import com.yhs.inventroysystem.domain.part.entity.PartStockTransaction;
import com.yhs.inventroysystem.domain.part.entity.TransactionType;
import com.yhs.inventroysystem.domain.part.service.PartDomainService;
import com.yhs.inventroysystem.domain.part.service.PartStockTransactionDomainService;
import com.yhs.inventroysystem.domain.product.service.ProductPartDomainService;
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

import java.io.InputStream;
import java.util.List;

import static com.yhs.inventroysystem.application.part.PartCommands.PartRegisterCommand;
import static com.yhs.inventroysystem.application.part.PartCommands.PartUpdateCommand;
import static com.yhs.inventroysystem.domain.part.entity.TransactionType.ADJUSTMENT;

@Service
@Transactional(readOnly = true)
public class PartService {

    private final PartDomainService partDomainService;
    private final ProductPartDomainService productPartDomainService;
    private final PartStockTransactionDomainService partStockTransactionDomainService;
    private final FileStorageService fileStorageService;

    public PartService(
            PartDomainService partDomainService,
            ProductPartDomainService productPartDomainService,
            PartStockTransactionDomainService partStockTransactionDomainService,
            FileStorageFactory fileStorageFactory) {
        this.partDomainService = partDomainService;
        this.productPartDomainService = productPartDomainService;
        this.partStockTransactionDomainService = partStockTransactionDomainService;
        this.fileStorageService = fileStorageFactory.getStorageService(FileStorageType.PART_IMAGE);
    }


    @Transactional
    public Part registerPart(PartRegisterCommand command) {
        partDomainService.validatePartCodeDuplication(command.partCode());

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

        Part savedPart = partDomainService.savePart(part);

        partStockTransactionDomainService.recordTransaction(
                savedPart,
                TransactionType.INITIAL,
                0,
                command.initialStock()
        );

        return savedPart;
    }

    public List<Part> findAllPart(String sortBy, String direction) {
        Sort sort = PageableUtils.createSort(sortBy, direction);
        return partDomainService.findAllActive(sort);
    }

    public Page<Part> searchParts(String keyword, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);
        return partDomainService.searchByKeyword(keyword, pageable);
    }

    public Page<Part> findAllPartPaged(int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);
        return partDomainService.findAllActive(pageable);
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
            partStockTransactionDomainService.recordTransaction(
                    part,
                    ADJUSTMENT,
                    beforeStock,
                    changeQuantity
            );
        }

        return partDomainService.savePart(part);
    }

    @Transactional
    public Part adjustPartStock(Long partId, PartStockUpdateCommand command) {
        Part part = findPartById(partId);

        Integer beforeStock = part.getStockQuantity();
        int afterStock = beforeStock + command.adjustmentQuantity();

        if (afterStock < 0) {
            throw new IllegalArgumentException("조정 후 재고가 0보다 작을 수 없습니다. (현재: " + beforeStock + ", 조정: " + command.adjustmentQuantity() + ")");
        }

        part.updateStockQuantity(afterStock);

        partStockTransactionDomainService.recordTransactionWithNote(
                part,
                ADJUSTMENT,
                beforeStock,
                command.adjustmentQuantity(),
                command.note()
        );

        return part;
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
    public void increaseStock(Long partId, PartStockIncreaseCommand command) {
        Part part = findPartById(partId);
        Integer beforeStock = part.getStockQuantity();

        Integer increaseStock = command.quantity();
        part.increaseStock(increaseStock);

        partStockTransactionDomainService.recordTransactionWithNote(part, TransactionType.INBOUND, beforeStock, increaseStock, command.note());
    }

    @Transactional
    public void decreaseStock(Long partId, PartStockDecreaseCommand command) {
        Part part = findPartById(partId);
        Integer beforeStock = part.getStockQuantity();

        Integer decreaseStock = command.quantity();
        part.decreaseStock(decreaseStock);

        partStockTransactionDomainService.recordTransactionWithNote(part, TransactionType.OUTBOUND, beforeStock, -decreaseStock,  command.note());

    }

    @Transactional
    public void deletePart(Long partId) {
        Part part = findPartById(partId);

        // 매핑된 product 유무 확인
        if (productPartDomainService.existsByPart(part)) {
            long usingProductCount = productPartDomainService.countProductsByPart(part);
            throw PartInUseException.usedInProducts(part.getPartCode(), (int) usingProductCount);
        }

        // 이미지 파일 삭제
        if (part.getImagePath() != null) {
            fileStorageService.delete(part.getImagePath());
        }

        part.markAsDeleted();
    }

    public List<PartStockTransaction> getPartStockTransactions(Long partId) {
        return partStockTransactionDomainService.findByPartId(partId);
    }

    public List<PartStockTransaction> getAllStockTransactions() {
        return partStockTransactionDomainService.findAll();
    }

    public Part findPartById(Long partId) {
        return partDomainService.findById(partId);
    }

    public InputStream getPartImageStream(Long partId) {
        Part part = findPartById(partId);

        if (part.getImagePath() == null) {
            throw new IllegalArgumentException("이미지가 존재하지 않습니다.");
        }

        return fileStorageService.loadAsStream(part.getImagePath());
    }
}
