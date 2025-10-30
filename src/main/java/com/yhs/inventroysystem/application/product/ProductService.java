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
    private final ProductLineRepository productLineRepository;

    private final ProductStockTransactionService productStockTransactionService;
    private final PartStockTransactionService partStockTransactionService;

    @Transactional
    public Product registerProduct(ProductRegisterCommand command) {
        validateProductCodeDuplication(command.productCode());
        validateProductNameDuplication(command.name());

        ProductLine productLine = productLineRepository.findById(command.productLineId())
                .orElseThrow(() -> ResourceNotFoundException.productLine(command.productLineId()));

        Product product = new Product(
                command.productCategory(),
                productLine,
                command.productCode(),
                command.name(),
                command.defaultUnitPrice(),
                command.description(),
                command.initialStock()
        );

        // 부품 매핑 추가
        if (command.partMappings() != null && !command.partMappings().isEmpty()) {
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

        // 2. 부품이 있는 경우에만 부품 재고 검증 및 차감
        if (!product.getPartMappings().isEmpty()) {
            // 필요한 부품 재고 검증
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

            // 부품 재고 차감
            for (ProductPart mapping : product.getPartMappings()) {
                Part part = mapping.getPart();
                Integer beforeStock = part.getStockQuantity();
                Integer requiredQuantity = mapping.calculateTotalRequired(command.quantity());
                part.decreaseStock(requiredQuantity);

                partStockTransactionService.recordTransaction(
                        part,
                        TransactionType.OUTBOUND,
                        beforeStock,
                        -requiredQuantity
                );
            }
        }

        // 3. 제품 재고 증가 (부품 유무와 관계없이 항상 실행)
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

        if (command.productCategory() != null) {
            product.changeCategory(command.productCategory());
        }

        if (command.productLineId() != null) {
            ProductLine productLine = productLineRepository.findById(command.productLineId())
                    .orElseThrow(() -> ResourceNotFoundException.productLine(command.productLineId()));
            product.assignProductLine(productLine);
        } else {
            // productLineId가 null이면 제품 라인 제거
            product.removeProductLine();
        }

        // 기존 부품 매핑 삭제
        product.clearPartMappings();

        // 새로운 부품 매핑 추가
        if (command.partMappings() != null && !command.partMappings().isEmpty()) {
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
        }

        return product;
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProductById(productId);
        product.markAsDeleted();
    }

    @Transactional
    public Product toggleProductFeatured(Long productId) {
        Product product = findProductById(productId);
        product.toggleFeatured();
        return product;
    }

    public List<ProductStockTransaction> getProductStockTransactions(Long productId) {
        return productStockTransactionService.findByProductId(productId);
    }

    public List<ProductStockTransaction> getAllStockTransactions() {
        return productStockTransactionService.findAll();
    }

    /**
     * 제품의 최대 생산 가능 수량 계산
     */
    public Integer calculateMaxProducibleQuantity(Long productId) {
        Product product = productRepository.findByIdWithPartsAndNotDeleted(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));

        // 부품이 없으면 제한 없음 (매우 큰 수 반환)
        if (product.getPartMappings().isEmpty()) {
            return Integer.MAX_VALUE;
        }

        Integer maxQuantity = Integer.MAX_VALUE;

        // 각 부품별로 생산 가능한 최대 수량 계산
        for (ProductPart mapping : product.getPartMappings()) {
            Part part = mapping.getPart();
            Integer availableStock = part.getStockQuantity();
            Integer requiredPerProduct = mapping.getRequiredQuantity();

            // 해당 부품으로 생산 가능한 최대 수량
            Integer producibleByThisPart = availableStock / requiredPerProduct;

            // 가장 적게 생산 가능한 수량이 최대 생산 가능 수량
            maxQuantity = Math.min(maxQuantity, producibleByThisPart);
        }

        return maxQuantity;
    }

    /**
     * 특정 수량 생산 시 부족한 부품 정보 계산
     */
    public List<InsufficientPartDetail> calculateInsufficientParts(Long productId, Integer requestedQuantity) {
        Product product = productRepository.findByIdWithPartsAndNotDeleted(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));

        if (product.getPartMappings().isEmpty()) {
            return List.of();
        }

        List<InsufficientPartDetail> insufficientParts = new java.util.ArrayList<>();

        for (ProductPart mapping : product.getPartMappings()) {
            Part part = mapping.getPart();
            Integer requiredTotal = mapping.calculateTotalRequired(requestedQuantity);
            Integer availableStock = part.getStockQuantity();

            if (availableStock < requiredTotal) {
                insufficientParts.add(new InsufficientPartDetail(
                        part.getId(),
                        part.getName(),
                        part.getPartCode(),
                        mapping.getRequiredQuantity(),
                        requiredTotal,
                        availableStock,
                        requiredTotal - availableStock
                ));
            }
        }

        return insufficientParts;
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