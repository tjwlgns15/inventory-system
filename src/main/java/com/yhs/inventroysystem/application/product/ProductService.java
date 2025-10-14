package com.yhs.inventroysystem.application.product;

import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.part.PartStockTransactionService;
import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.InsufficientStockException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.part.Part;
import com.yhs.inventroysystem.domain.part.PartRepository;
import com.yhs.inventroysystem.domain.part.TransactionType;
import com.yhs.inventroysystem.domain.product.*;
import com.yhs.inventroysystem.domain.task.Priority;
import com.yhs.inventroysystem.domain.task.Task;
import com.yhs.inventroysystem.domain.task.TaskRepository;
import com.yhs.inventroysystem.domain.task.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.yhs.inventroysystem.application.product.ProductCommands.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final PartRepository partRepository;
    private final TaskRepository taskRepository;

    private final ProductStockTransactionService productStockTransactionService;
    private final PartStockTransactionService partStockTransactionService;

    @Transactional
    public Product registerProduct(ProductRegisterCommand command) {
        validateProductCodeDuplication(command.productCode());
        validateProductNameDuplication(command.name());

        Product product = new Product(
                command.productCode(),
                command.name(),
                command.defaultUnitPrice(),
                command.description(),
                command.initialStock()
        );

        // 부품 매핑 추가
        for (PartMappingInfo mappingInfo : command.partMappings()) {
            Part part = partRepository.findByIdAndNotDeleted(mappingInfo.partId())
                    .orElseThrow(() -> ResourceNotFoundException.part(mappingInfo.partId()));

            ProductPart mapping = new ProductPart(
                    product,
                    part,
                    mappingInfo.requiredQuantity()
            );
            product.addPartMapping(mapping);
        }

        Product savedProduct = productRepository.save(product);

        productStockTransactionService.recordTransaction(savedProduct, ProductTransactionType.INITIAL, 0, command.initialStock());

        return savedProduct;
    }

    public List<Product> findAllProduct() {
        return productRepository.findAllActive();
    }
    public List<Product> findAllProductWithParts() {
        return productRepository.findAllActiveWithPart();
    }

    public Product findProductWithParts(Long productId) {
        return productRepository.findByIdWithPartsAndNotDeleted(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));
    }

    public Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));
    }

    @Transactional
    public Product produceProduct(ProductProduceCommand command, CustomUserDetails currentUser) {
        // 1. 제품 조회 (부품 정보 포함)
        Product product = productRepository.findByIdWithPartsAndNotDeleted(command.productId())
                .orElseThrow(() -> ResourceNotFoundException.product(command.productId()));

        // 2. 필요한 부품 재고 검증
        for (ProductPart mapping : product.getPartMappings()) {
            Part part = mapping.getPart();
            Integer requiredQuantity = mapping.calculateTotalRequired(command.quantity());

            if (part.getStockQuantity() < requiredQuantity) {
                throw InsufficientStockException.insufficientStock(
                        part.getName(),
                        requiredQuantity,
                        part.getStockQuantity()
                );
            }
        }

        // 3. 부품 재고 차감
        for (ProductPart mapping : product.getPartMappings()) {
            Part part = mapping.getPart();
            Integer beforeStock = part.getStockQuantity();
            Integer requiredQuantity = mapping.calculateTotalRequired(command.quantity());
            part.decreaseStock(requiredQuantity);

            partStockTransactionService.recordTransaction(part, TransactionType.OUTBOUND, beforeStock, -requiredQuantity);
        }

        // 4. 제품 재고 증가
        Integer beforeStock = product.getStockQuantity();
        product.increaseStock(command.quantity());

        productStockTransactionService.recordTransaction(product, ProductTransactionType.PRODUCE, beforeStock, command.quantity());
        createTaskForProduct(product, command.quantity(), currentUser);

        return product;
    }

    @Transactional
    public Product updateProduct(Long productId, ProductUpdateCommand command) {
        Product product = productRepository.findByIdWithPartsAndNotDeleted(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));

        validateProductNameDuplicationForUpdate(productId, command.name());

        product.updateInfo(
                command.name(),
                command.defaultUnitPrice(),
                command.description()
        );

        // 기존 부품 매핑 삭제
        product.clearPartMappings();

        // 새로운 부품 매핑 추가
        for (PartMappingInfo mappingInfo : command.partMappings()) {
            Part part = partRepository.findByIdAndNotDeleted(mappingInfo.partId())
                    .orElseThrow(() -> ResourceNotFoundException.part(mappingInfo.partId()));

            ProductPart mapping = new ProductPart(
                    product,
                    part,
                    mappingInfo.requiredQuantity()
            );
            product.addPartMapping(mapping);
        }

        return product;
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProductById(productId);
        product.markAsDeleted();
    }

    public List<ProductStockTransaction> getProductStockTransactions(Long productId) {
        return productStockTransactionService.findByProductId(productId);
    }

    public List<ProductStockTransaction> getAllStockTransactions() {
        return productStockTransactionService.findAll();
    }

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

    private void validateProductCodeDuplicationForUpdate(Long productId, String productCode) {
        productRepository.findByProductCodeAndNotDeleted(productCode)
                .ifPresent(existingProduct -> {
                    if (!existingProduct.getId().equals(productId)) {
                        throw DuplicateResourceException.productCode(productCode);
                    }
                });
    }
    private void validateProductNameDuplicationForUpdate(Long productId, String productName) {
        productRepository.findByNameAndNotDeleted(productName)
                .ifPresent(existingProduct -> {
                    if (!existingProduct.getId().equals(productId)) {
                        throw DuplicateResourceException.productName(productName);
                    }
                });
    }

    private void createTaskForProduct(Product product, Integer quantity, CustomUserDetails currentUser) {
        String title = generateTaskTitle(product, quantity);
        String description = generateTaskDescription(product, quantity);

        Task task = new Task(
                title,
                description,
                currentUser.getName(),
                LocalDate.now(),
                LocalDate.now(),
                TaskStatus.COMPLETED,
                Priority.MEDIUM
        );
        taskRepository.save(task);
    }

    private String generateTaskTitle(Product product, Integer quantity) {
        return String.format("[생산] %s - %s",
                product.getName(),
                quantity);
    }
    private String generateTaskDescription(Product product, Integer quantity) {
        return "생산 정보:\n" +
                String.format("- %s: %d개 생산, 재고: %d\n",
                        product.getName(),
                        quantity,
                        product.getStockQuantity());
    }
}