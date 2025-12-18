package com.yhs.inventroysystem.application.product;

import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.domain.exception.InsufficientStockException;
import com.yhs.inventroysystem.domain.part.entity.Part;
import com.yhs.inventroysystem.domain.part.entity.TransactionType;
import com.yhs.inventroysystem.domain.part.service.PartDomainService;
import com.yhs.inventroysystem.domain.part.service.PartStockTransactionDomainService;
import com.yhs.inventroysystem.domain.product.entity.*;
import com.yhs.inventroysystem.domain.product.service.ProductDomainService;
import com.yhs.inventroysystem.domain.product.service.ProductLineDomainService;
import com.yhs.inventroysystem.domain.product.service.ProductStockTransactionDomainService;
import com.yhs.inventroysystem.domain.task.entity.Priority;
import com.yhs.inventroysystem.domain.task.entity.Task;
import com.yhs.inventroysystem.domain.task.entity.TaskCategory;
import com.yhs.inventroysystem.domain.task.entity.TaskStatus;
import com.yhs.inventroysystem.domain.task.service.TaskCategoryDomainService;
import com.yhs.inventroysystem.domain.task.service.TaskDomainService;
import com.yhs.inventroysystem.infrastructure.pagenation.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.yhs.inventroysystem.application.product.ProductCommands.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductDomainService productDomainService;
    private final ProductLineDomainService productLineDomainService;
    private final PartDomainService partDomainService;
    private final TaskDomainService taskDomainService;
    private final TaskCategoryDomainService taskCategoryDomainService;

    private final ProductStockTransactionDomainService productStockTransactionDomainService;
    private final PartStockTransactionDomainService partStockTransactionDomainService;


    @Transactional
    public Product registerProduct(ProductRegisterCommand command) {

        Product product = productDomainService.createProduct(
                command.productCategory(),
                command.productCode(),
                command.name(),
                command.defaultUnitPrice(),
                command.description(),
                command.initialStock()
        );

        if (command.productLineId() != null) {
            ProductLine productLine = productLineDomainService.findById(command.productLineId());
            product.assignProductLine(productLine);
        }

        if (command.partMappings() != null && !command.partMappings().isEmpty()) {
            addPartMappings(product, command.partMappings());
        }

        productStockTransactionDomainService.recordTransaction(
                product,
                ProductTransactionType.INITIAL,
                0,
                command.initialStock()
        );

        return product;
    }


    public List<Product> findAllProduct(String sortBy, String direction) {
        Sort sort = PageableUtils.createSort(sortBy, direction);
        return productDomainService.findAllActive(sort);
    }

    public Page<Product> searchProducts(String keyword, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);
        return productDomainService.searchByKeyword(keyword, pageable);
    }

    public Page<Product> findAllProductPaged(int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);
        return productDomainService.findAllActive(pageable);
    }

    public List<Product> findAllProductWithParts() {
        return productDomainService.findAllActiveWithPartOrderByDisplayOrder();
    }

    public Product findProductWithParts(Long productId) {
        return productDomainService.findProductWithParts(productId);
    }

    public Product findProductById(Long productId) {
        return productDomainService.findById(productId);
    }

    @Transactional
    public Product produceProduct(ProductProduceCommand command, CustomUserDetails currentUser) {
        // 1. 제품 조회 (부품 정보 포함)
        Product product = productDomainService.findProductWithParts(command.productId());

        // 2. 부품이 있는 경우에만 부품 재고 검증 및 차감
        if (!product.getPartMappings().isEmpty()) {
            // 필요한 부품 재고 검증
            validatePartStock(command, product);
            // 부품 재고 차감
            decreasePartStock(command, product);
        }

        // 3. 제품 재고 증가
        Integer beforeStock = product.getStockQuantity();
        product.increaseStock(command.quantity());

        productStockTransactionDomainService.recordTransaction(product, ProductTransactionType.PRODUCE, beforeStock, command.quantity());

        createTaskForProduct(product, command.quantity(), currentUser);

        return product;
    }

    @Transactional
    public Product updateProduct(Long productId, ProductUpdateCommand command) {
        Product product = productDomainService.findProductWithParts(productId);

        productDomainService.validateProductNameDuplicationForUpdate(productId, command.name());

        product.updateInfo(
                command.name(),
                command.defaultUnitPrice(),
                command.description()
        );

        if (command.productCategory() != null) {
            product.changeCategory(command.productCategory());
        }

        if (command.productLineId() != null) {
            ProductLine productLine = productLineDomainService.findById(command.productLineId());
            product.assignProductLine(productLine);
        } else {
            // productLineId가 null이면 제품 라인 제거
            product.removeProductLine();
        }

        // 기존 부품 매핑 삭제
        product.clearPartMappings();

        // 새로운 부품 매핑 추가
        if (command.partMappings() != null && !command.partMappings().isEmpty()) {
            addPartMappings(product, command.partMappings());
        }

        return product;
    }

    @Transactional
    public Product adjustProductStock(Long productId, ProductStockUpdateCommand command) {
        Product product = productDomainService.findProductWithParts(productId);

        Integer beforeStock = product.getStockQuantity();
        int afterStock = beforeStock + command.adjustmentQuantity();

        // 재고가 음수가 되는 것 방지
        if (afterStock < 0) {
            throw new IllegalArgumentException("조정 후 재고가 0보다 작을 수 없습니다. (현재: " + beforeStock + ", 조정: " + command.adjustmentQuantity() + ")");
        }

        product.updateStockQuantity(afterStock);

        // 트랜잭션 기록 (사유 포함)
        productStockTransactionDomainService.recordTransactionWithNote(
                product,
                ProductTransactionType.ADJUSTMENT,
                beforeStock,
                command.adjustmentQuantity(),
                command.note()
        );

        return product;
    }

    @Transactional
    public Product updateDisplayOrder(Long productId, DisplayOrderUpdateCommand command) {
        Product product = productDomainService.findById(productId);
        product.changeDisplayOrder(command.displayOrder());
        return product;
    }

    @Transactional
    public void updateDisplayOrders(List<ProductOrderUpdate> updates) {
        updates.forEach(update -> {
            Product product = productDomainService.findById(update.productId());
            product.changeDisplayOrder(update.displayOrder());
        });
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productDomainService.findById(productId);
        product.markAsDeleted();
    }

    @Transactional
    public Product toggleProductFeatured(Long productId) {
        Product product = productDomainService.findById(productId);
        product.toggleFeatured();
        return product;
    }

    @Transactional
    public Product toggleProductFeatured2(Long productId) {
        Product product = productDomainService.findById(productId);
        product.toggleFeatured2();
        return product;
    }


    public List<ProductStockTransaction> getProductStockTransactions(Long productId) {
        return productStockTransactionDomainService.findByProductId(productId);
    }

    public List<ProductStockTransaction> getAllStockTransactions() {
        return productStockTransactionDomainService.findAll();
    }

    /**
     * 제품의 최대 생산 가능 수량 계산
     */
    public Integer calculateMaxProducibleQuantity(Long productId) {
        Product product = productDomainService.findProductWithParts(productId);

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
        Product product = productDomainService.findProductWithParts(productId);

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


    /*
        Private Method
     */
    private void createTaskForProduct(Product product, Integer quantity, CustomUserDetails currentUser) {
        String title = generateTaskTitle(product, quantity);
        String description = generateTaskDescription(product, quantity);

        TaskCategory productionCategory = taskCategoryDomainService.findByName("제품 생산");

        Task task = new Task(
                title,
                description,
                currentUser.getName(),
                LocalDate.now(),
                LocalDate.now(),
                TaskStatus.COMPLETED,
                Priority.MEDIUM
        );

        task.addCategory(productionCategory);
        taskDomainService.saveTask(task);
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


    private void addPartMappings(Product product, List<PartMappingInfo> mappings) {
        for (PartMappingInfo mapping : mappings) {
            Part part = partDomainService.findById(mapping.partId());
            ProductPart productPart = new ProductPart(
                    product,
                    part,
                    mapping.requiredQuantity()
            );

            product.addPartMapping(productPart);
        }
    }

    private static void validatePartStock(ProductProduceCommand command, Product product) {
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
    }

    private void decreasePartStock(ProductProduceCommand command, Product product) {
        for (ProductPart mapping : product.getPartMappings()) {
            Part part = mapping.getPart();
            Integer beforeStock = part.getStockQuantity();
            Integer requiredQuantity = mapping.calculateTotalRequired(command.quantity());
            part.decreaseStock(requiredQuantity);

            partStockTransactionDomainService.recordTransaction(
                    part,
                    TransactionType.OUTBOUND,
                    beforeStock,
                    -requiredQuantity
            );
        }
    }
}