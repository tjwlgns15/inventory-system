package com.yhs.inventroysystem.application.bulk;

import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.bulk.command.*;
import com.yhs.inventroysystem.application.bulk.command.ProductBulkRegisterCommand.BulkProductData;
import com.yhs.inventroysystem.application.bulk.parser.*;
import com.yhs.inventroysystem.application.exchange.ExchangeRateService;
import com.yhs.inventroysystem.application.part.PartStockTransactionService;
import com.yhs.inventroysystem.application.product.ProductStockTransactionService;
import com.yhs.inventroysystem.domain.client.Client;
import com.yhs.inventroysystem.domain.client.ClientRepository;
import com.yhs.inventroysystem.domain.client.Country;
import com.yhs.inventroysystem.domain.client.CountryRepository;
import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.domain.delivery.DeliveryItem;
import com.yhs.inventroysystem.domain.delivery.DeliveryRepository;
import com.yhs.inventroysystem.domain.delivery.DeliveryStatus;
import com.yhs.inventroysystem.domain.exchange.Currency;
import com.yhs.inventroysystem.domain.exchange.ExchangeRate;
import com.yhs.inventroysystem.domain.part.Part;
import com.yhs.inventroysystem.domain.part.PartRepository;
import com.yhs.inventroysystem.domain.part.TransactionType;
import com.yhs.inventroysystem.domain.price.ClientProductPrice;
import com.yhs.inventroysystem.domain.price.ClientProductPriceRepository;
import com.yhs.inventroysystem.domain.product.*;
import com.yhs.inventroysystem.domain.task.Priority;
import com.yhs.inventroysystem.domain.task.Task;
import com.yhs.inventroysystem.domain.task.TaskRepository;
import com.yhs.inventroysystem.domain.task.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BulkRegisterService {

    private final PartRepository partRepository;
    private final PartStockTransactionService partStockTransactionService;
    private final PartBulkFileParser partBulkFileParser;

    private final ProductRepository productRepository;
    private final ProductStockTransactionService productStockTransactionService;
    private final ProductBulkFileParser productBulkFileParser;

    private final ProductPartRepository productPartRepository;
    private final ProductPartMappingFileParser productPartMappingFileParser;

    private final ClientRepository clientRepository;
    private final CountryRepository countryRepository;
    private final ClientBulkFileParser clientBulkFileParser;

    private final ClientProductPriceRepository priceRepository;
    private final PriceBulkFileParser priceBulkFileParser;

    private final DeliveryRepository deliveryRepository;
    private final DeliveryBulkFileParser deliveryBulkFileParser;
    private final ExchangeRateService exchangeRateService;
    private final TaskRepository taskRepository;

    private final DeliveryItemBulkFileParser deliveryItemBulkFileParser;

    /**
     * Part
     */
    @Transactional
    public PartBulkRegisterCommand.Result bulkRegisterParts(MultipartFile file) {
        List<PartBulkRegisterCommand.BulkPartData> bulkParts;

        try {
            bulkParts = partBulkFileParser.parseFile(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("파일 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }

        int totalCount = bulkParts.size();
        int successCount = 0;
        List<PartBulkRegisterCommand.FailureDetail> failures = new ArrayList<>();

        for (int i = 0; i < bulkParts.size(); i++) {
            PartBulkRegisterCommand.BulkPartData bulkData = bulkParts.get(i);
            int rowNumber = i + 2; // Excel/CSV의 실제 행 번호 (헤더 + 0-based index)

            try {
                validateBulkPartData(bulkData);

                // 중복 체크
                if (partRepository.existsByPartCodeAndNotDeleted(bulkData.partCode())) {
                    failures.add(PartBulkRegisterCommand.FailureDetail.builder()
                            .rowNumber(rowNumber)
                            .partCode(bulkData.partCode())
                            .name(bulkData.name())
                            .errorMessage("이미 존재하는 부품 코드입니다")
                            .build());
                    continue;
                }

                // Part 생성 및 저장
                Part part = new Part(
                        bulkData.partCode(),
                        bulkData.name(),
                        bulkData.specification(),
                        bulkData.initialStock(),
                        bulkData.unit()
                );

                Part savedPart = partRepository.save(part);

                // 초기 재고 트랜잭션 기록
                partStockTransactionService.recordTransaction(
                        savedPart,
                        TransactionType.INITIAL,
                        0,
                        bulkData.initialStock()
                );

                successCount++;

            } catch (Exception e) {
                failures.add(PartBulkRegisterCommand.FailureDetail.builder()
                        .rowNumber(rowNumber)
                        .partCode(bulkData.partCode())
                        .name(bulkData.name())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        int failureCount = totalCount - successCount;

        return new PartBulkRegisterCommand.Result(
                totalCount,
                successCount,
                failureCount,
                failures
        );
    }

    private void validateBulkPartData(PartBulkRegisterCommand.BulkPartData data) {
        if (data.partCode() == null || data.partCode().trim().isEmpty()) {
            throw new IllegalArgumentException("부품 코드는 필수입니다");
        }
        if (data.name() == null || data.name().trim().isEmpty()) {
            throw new IllegalArgumentException("부품명은 필수입니다");
        }
        if (data.unit() == null || data.unit().trim().isEmpty()) {
            throw new IllegalArgumentException("단위는 필수입니다");
        }
        if (data.initialStock() == null || data.initialStock() < 0) {
            throw new IllegalArgumentException("초기 재고는 0 이상이어야 합니다");
        }
    }


    /**
     * Product
     */
    @Transactional
    public ProductBulkRegisterCommand.Result bulkRegisterProducts(MultipartFile file) {
        List<BulkProductData> bulkProducts;

        try {
            bulkProducts = productBulkFileParser.parseFile(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("파일 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }

        int totalCount = bulkProducts.size();
        int successCount = 0;
        List<ProductBulkRegisterCommand.FailureDetail> failures = new ArrayList<>();

        for (int i = 0; i < bulkProducts.size(); i++) {
            BulkProductData bulkData = bulkProducts.get(i);
            int rowNumber = i + 2; // Excel/CSV의 실제 행 번호 (헤더 + 0-based index)

            try {
                validateBulkProductData(bulkData);

                // 중복 체크
                if (productRepository.existsByProductCodeAndNotDeleted(bulkData.productCode())) {
                    failures.add(ProductBulkRegisterCommand.FailureDetail.builder()
                            .rowNumber(rowNumber)
                            .productCode(bulkData.productCode())
                            .name(bulkData.name())
                            .errorMessage("이미 존재하는 제품 코드입니다")
                            .build());
                    continue;
                }

                // Product 생성 및 저장 (부품 매핑 없이)
                // todo: 임시로 HARDWARE, null
                Product product = new Product(
                        ProductCategory.HARDWARE,
                        null,
                        bulkData.productCode(),
                        bulkData.name(),
                        bulkData.defaultUnitPrice(),
                        bulkData.description(),
                        bulkData.stockQuantity()
                );

                Product savedProduct = productRepository.save(product);

                // 초기 재고 트랜잭션 기록
                productStockTransactionService.recordTransaction(
                        savedProduct,
                        ProductTransactionType.INITIAL,
                        0,
                        bulkData.stockQuantity()
                );

                successCount++;

            } catch (Exception e) {
                failures.add(ProductBulkRegisterCommand.FailureDetail.builder()
                        .rowNumber(rowNumber)
                        .productCode(bulkData.productCode())
                        .name(bulkData.name())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        int failureCount = totalCount - successCount;

        return new ProductBulkRegisterCommand.Result(
                totalCount,
                successCount,
                failureCount,
                failures
        );
    }

    private void validateBulkProductData(BulkProductData data) {
        if (data.productCode() == null || data.productCode().trim().isEmpty()) {
            throw new IllegalArgumentException("제품 코드는 필수입니다");
        }
        if (data.name() == null || data.name().trim().isEmpty()) {
            throw new IllegalArgumentException("제품명은 필수입니다");
        }
        if (data.stockQuantity() == null || data.stockQuantity() < 0) {
            throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다");
        }
    }

    /**
     * Part-Product mapping
     */
    @Transactional
    public ProductPartMappingBulkCommand.Result bulkRegisterProductPartMappings(MultipartFile file) {
        List<ProductPartMappingBulkCommand.BulkMappingData> bulkMappings;

        try {
            bulkMappings = productPartMappingFileParser.parseFile(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("파일 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }

        int totalCount = bulkMappings.size();
        int successCount = 0;
        List<ProductPartMappingBulkCommand.FailureDetail> failures = new ArrayList<>();

        for (int i = 0; i < bulkMappings.size(); i++) {
            ProductPartMappingBulkCommand.BulkMappingData bulkData = bulkMappings.get(i);
            int rowNumber = i + 2; // Excel/CSV의 실제 행 번호 (헤더 + 0-based index)

            try {
                validateBulkMappingData(bulkData);

                // Product 조회
                Product product = productRepository.findByProductCodeAndNotDeleted(bulkData.productCode())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않는 제품 코드입니다: " + bulkData.productCode()));

                // Part 조회
                Part part = partRepository.findByPartCodeAndNotDeleted(bulkData.partCode())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않는 부품 코드입니다: " + bulkData.partCode()));

                // 중복 매핑 체크 (같은 제품에 같은 부품이 이미 매핑되어 있는지)
                boolean isDuplicate = product.getPartMappings().stream()
                        .anyMatch(mapping -> mapping.getPart().getId().equals(part.getId()));

                if (isDuplicate) {
                    failures.add(ProductPartMappingBulkCommand.FailureDetail.builder()
                            .rowNumber(rowNumber)
                            .productCode(bulkData.productCode())
                            .partCode(bulkData.partCode())
                            .errorMessage("이미 매핑되어 있는 부품입니다")
                            .build());
                    continue;
                }

                // ProductPart 매핑 생성 및 저장
                ProductPart mapping = new ProductPart(
                        product,
                        part,
                        bulkData.requiredQuantity()
                );

                product.addPartMapping(mapping);
                productPartRepository.save(mapping);

                successCount++;

            } catch (Exception e) {
                failures.add(ProductPartMappingBulkCommand.FailureDetail.builder()
                        .rowNumber(rowNumber)
                        .productCode(bulkData.productCode())
                        .partCode(bulkData.partCode())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        int failureCount = totalCount - successCount;

        return new ProductPartMappingBulkCommand.Result(
                totalCount,
                successCount,
                failureCount,
                failures
        );
    }

    private void validateBulkMappingData(ProductPartMappingBulkCommand.BulkMappingData data) {
        if (data.productCode() == null || data.productCode().trim().isEmpty()) {
            throw new IllegalArgumentException("제품 코드는 필수입니다");
        }
        if (data.partCode() == null || data.partCode().trim().isEmpty()) {
            throw new IllegalArgumentException("부품 코드는 필수입니다");
        }
        if (data.requiredQuantity() == null || data.requiredQuantity() <= 0) {
            throw new IllegalArgumentException("필요 수량은 1 이상이어야 합니다");
        }
    }

    /**
     * Client
     */
    @Transactional
    public ClientBulkRegisterCommand.Result bulkRegisterClients(MultipartFile file) {
        List<ClientBulkRegisterCommand.BulkClientData> bulkClients;

        try {
            bulkClients = clientBulkFileParser.parseFile(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("파일 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }

        int totalCount = bulkClients.size();
        int successCount = 0;
        List<ClientBulkRegisterCommand.FailureDetail> failures = new ArrayList<>();

        // 1차: 상위 거래처(Parent) 먼저 등록
        for (int i = 0; i < bulkClients.size(); i++) {
            ClientBulkRegisterCommand.BulkClientData bulkData = bulkClients.get(i);
            int rowNumber = i + 2;

            // parentClientCode가 없으면 상위 거래처
            if (bulkData.parentClientCode() == null || bulkData.parentClientCode().trim().isEmpty()) {
                try {
                    registerParentClientFromBulk(bulkData);
                    successCount++;
                } catch (Exception e) {
                    failures.add(ClientBulkRegisterCommand.FailureDetail.builder()
                            .rowNumber(rowNumber)
                            .clientCode(bulkData.clientCode())
                            .name(bulkData.name())
                            .errorMessage(e.getMessage())
                            .build());
                }
            }
        }

        // 2차: 하위 거래처(Child) 등록
        for (int i = 0; i < bulkClients.size(); i++) {
            ClientBulkRegisterCommand.BulkClientData bulkData = bulkClients.get(i);
            int rowNumber = i + 2;

            // parentClientCode가 있으면 하위 거래처
            if (bulkData.parentClientCode() != null && !bulkData.parentClientCode().trim().isEmpty()) {
                try {
                    registerChildClientFromBulk(bulkData);
                    successCount++;
                } catch (Exception e) {
                    failures.add(ClientBulkRegisterCommand.FailureDetail.builder()
                            .rowNumber(rowNumber)
                            .clientCode(bulkData.clientCode())
                            .name(bulkData.name())
                            .errorMessage(e.getMessage())
                            .build());
                }
            }
        }

        int failureCount = totalCount - successCount;

        return new ClientBulkRegisterCommand.Result(
                totalCount,
                successCount,
                failureCount,
                failures
        );
    }

    private void registerParentClientFromBulk(ClientBulkRegisterCommand.BulkClientData data) {
        validateBulkClientData(data);

        // 중복 체크
        if (clientRepository.existsByClientCodeAndNotDeleted(data.clientCode())) {
            throw new IllegalArgumentException("이미 존재하는 거래처 코드입니다");
        }

        // Country 조회
        Country country = countryRepository.findByCode(data.countryCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 국가 코드입니다: " + data.countryCode()));

        // Currency 변환
        Currency currency = parseCurrency(data.currency());

        // Parent Client 생성 및 저장
        Client client = new Client(
                data.clientCode(),
                country,
                data.name(),
                data.address(),
                data.contactNumber(),
                data.email(),
                currency
        );

        clientRepository.save(client);
    }

    private void registerChildClientFromBulk(ClientBulkRegisterCommand.BulkClientData data) {
        validateBulkClientData(data);

        // 중복 체크
        if (clientRepository.existsByClientCodeAndNotDeleted(data.clientCode())) {
            throw new IllegalArgumentException("이미 존재하는 거래처 코드입니다");
        }

        // Parent Client 조회
        Client parentClient = clientRepository.findByClientCodeAndNotDeleted(data.parentClientCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 상위 거래처 코드입니다: " + data.parentClientCode()));

        // Country 조회
        Country country = countryRepository.findByCode(data.countryCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 국가 코드입니다: " + data.countryCode()));

        Currency currency = Currency.valueOf(data.currency().toUpperCase());

        // Child Client 생성 및 저장
        Client childClient = new Client(
                data.clientCode(),
                parentClient,
                country,
                data.name(),
                data.address(),
                data.contactNumber(),
                data.email(),
                currency
        );

        parentClient.addChildClient(childClient);
        clientRepository.save(childClient);
    }

    private void validateBulkClientData(ClientBulkRegisterCommand.BulkClientData data) {
        if (data.clientCode() == null || data.clientCode().trim().isEmpty()) {
            throw new IllegalArgumentException("거래처 코드는 필수입니다");
        }
        if (data.name() == null || data.name().trim().isEmpty()) {
            throw new IllegalArgumentException("거래처명은 필수입니다");
        }
        if (data.countryCode() == null || data.countryCode().trim().isEmpty()) {
            throw new IllegalArgumentException("국가 코드는 필수입니다");
        }
        if (data.currency() == null || data.currency().trim().isEmpty()) {
            throw new IllegalArgumentException("통화는 필수입니다");
        }
    }

    private Currency parseCurrency(String currencyStr) {
        try {
            return Currency.valueOf(currencyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("올바르지 않은 통화 코드입니다: " + currencyStr);
        }
    }

    /**
     * Product-Client price
     */
    @Transactional
    public PriceBulkRegisterCommand.Result bulkRegisterPrices(MultipartFile file) {
        List<PriceBulkRegisterCommand.BulkPriceData> bulkPrices;

        try {
            bulkPrices = priceBulkFileParser.parseFile(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("파일 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }

        int totalCount = bulkPrices.size();
        int successCount = 0;
        List<PriceBulkRegisterCommand.FailureDetail> failures = new ArrayList<>();

        for (int i = 0; i < bulkPrices.size(); i++) {
            PriceBulkRegisterCommand.BulkPriceData bulkData = bulkPrices.get(i);
            int rowNumber = i + 2; // Excel/CSV의 실제 행 번호 (헤더 + 0-based index)

            try {
                validateBulkPriceData(bulkData);

                // Client 조회
                Client client = clientRepository.findByClientCodeAndNotDeleted(bulkData.clientCode())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않는 거래처 코드입니다: " + bulkData.clientCode()));

                // Product 조회
                Product product = productRepository.findByProductCodeAndNotDeleted(bulkData.productCode())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않는 제품 코드입니다: " + bulkData.productCode()));

                // 중복 체크
                if (priceRepository.existsByClientIdAndProductId(client.getId(), product.getId())) {
                    failures.add(PriceBulkRegisterCommand.FailureDetail.builder()
                            .rowNumber(rowNumber)
                            .clientCode(bulkData.clientCode())
                            .productCode(bulkData.productCode())
                            .errorMessage("이미 등록된 거래처-제품 가격 정보입니다")
                            .build());
                    continue;
                }

                // ClientProductPrice 생성 및 저장
                ClientProductPrice price = new ClientProductPrice(
                        client,
                        product,
                        bulkData.unitPrice()
                );

                priceRepository.save(price);
                successCount++;

            } catch (Exception e) {
                failures.add(PriceBulkRegisterCommand.FailureDetail.builder()
                        .rowNumber(rowNumber)
                        .clientCode(bulkData.clientCode())
                        .productCode(bulkData.productCode())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        int failureCount = totalCount - successCount;

        return new PriceBulkRegisterCommand.Result(
                totalCount,
                successCount,
                failureCount,
                failures
        );
    }

    private void validateBulkPriceData(PriceBulkRegisterCommand.BulkPriceData data) {
        if (data.clientCode() == null || data.clientCode().trim().isEmpty()) {
            throw new IllegalArgumentException("거래처 코드는 필수입니다");
        }
        if (data.productCode() == null || data.productCode().trim().isEmpty()) {
            throw new IllegalArgumentException("제품 코드는 필수입니다");
        }
        if (data.unitPrice() == null || data.unitPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("단가는 0 이상이어야 합니다");
        }
    }

    /**
     * Delivery
     */
    @Transactional
    public DeliveryBulkRegisterCommand.Result bulkRegisterDeliveries(MultipartFile file, String userName) {
        List<DeliveryBulkRegisterCommand.BulkDeliveryData> bulkDeliveries;

        try {
            bulkDeliveries = deliveryBulkFileParser.parseFile(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("파일 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }

        int totalCount = bulkDeliveries.size();
        int successCount = 0;
        List<DeliveryBulkRegisterCommand.FailureDetail> failures = new ArrayList<>();

        for (int i = 0; i < bulkDeliveries.size(); i++) {
            DeliveryBulkRegisterCommand.BulkDeliveryData bulkData = bulkDeliveries.get(i);
            int rowNumber = i + 2; // Excel/CSV의 실제 행 번호 (헤더 + 0-based index)

            try {
                validateBulkDeliveryData(bulkData);

                // Client 조회
                Client client = clientRepository.findByClientCodeAndNotDeleted(bulkData.clientCode())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않는 거래처 코드입니다: " + bulkData.clientCode()));

                // Delivery 번호 중복 체크 (옵션)
                if (deliveryRepository.existsByDeliveryNumber(bulkData.deliveryNumber())) {
                    failures.add(DeliveryBulkRegisterCommand.FailureDetail.builder()
                            .rowNumber(rowNumber)
                            .deliveryNumber(bulkData.deliveryNumber())
                            .clientCode(bulkData.clientCode())
                            .errorMessage("이미 존재하는 납품 번호입니다")
                            .build());
                    continue;
                }

                // Delivery 생성 (status 포함)
                Delivery delivery = new Delivery(
                        bulkData.deliveryNumber(),
                        client,
                        bulkData.orderedAt(),
                        bulkData.requestedAt(),
                        bulkData.status() != null ? bulkData.status() : DeliveryStatus.PENDING,
                        bulkData.deliveredAt()
                );

                // 메모 설정
                if (bulkData.memo() != null && !bulkData.memo().trim().isEmpty()) {
                    delivery.updateMemo(bulkData.memo());
                }

                // 할인 적용
                if (bulkData.totalDiscountAmount() != null &&
                        bulkData.totalDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                    delivery.applyDiscount(bulkData.totalDiscountAmount(), bulkData.discountNote());
                }

                // 환율 조회 및 설정
                ExchangeRate exchangeRate = exchangeRateService.getLatestExchangeRate(client.getCurrency());
                delivery.setExchangeRate(exchangeRate.getRate());

                // Delivery 저장
                Delivery savedDelivery = deliveryRepository.save(delivery);

                successCount++;

            } catch (Exception e) {
                failures.add(DeliveryBulkRegisterCommand.FailureDetail.builder()
                        .rowNumber(rowNumber)
                        .deliveryNumber(bulkData.deliveryNumber())
                        .clientCode(bulkData.clientCode())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        int failureCount = totalCount - successCount;

        return new DeliveryBulkRegisterCommand.Result(
                totalCount,
                successCount,
                failureCount,
                failures
        );
    }

    private void validateBulkDeliveryData(DeliveryBulkRegisterCommand.BulkDeliveryData data) {
        if (data.deliveryNumber() == null || data.deliveryNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("납품 번호는 필수입니다");
        }
        if (data.clientCode() == null || data.clientCode().trim().isEmpty()) {
            throw new IllegalArgumentException("거래처 코드는 필수입니다");
        }
        if (data.orderedAt() == null) {
            throw new IllegalArgumentException("주문일은 필수입니다");
        }
        if (data.requestedAt() == null) {
            throw new IllegalArgumentException("출하 요청일은 필수입니다");
        }
        if (data.totalDiscountAmount() != null && data.totalDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("할인액은 0 이상이어야 합니다");
        }
    }

    private Task createOrderTask(Delivery delivery, Client client, String username, LocalDate orderedAt) {
        String title = String.format("[주문] %s - %s", client.getName(), delivery.getDeliveryNumber());
        String description = generateOrderTaskDescription(delivery);

        Task task = new Task(
                title,
                description,
                username,
                orderedAt,
                orderedAt,
                TaskStatus.TODO,
                Priority.MEDIUM
        );

        return taskRepository.save(task);
    }

    private String generateOrderTaskDescription(Delivery delivery) {
        StringBuilder description = new StringBuilder();

        // 주문일, 출하요청일 정보
        description.append(String.format("주문일: %s\n", delivery.getOrderedAt()));
        description.append(String.format("출하 요청일: %s\n", delivery.getRequestedAt()));
        if (delivery.getDeliveredAt() != null) {
            description.append(String.format("출하 완료일: %s\n", delivery.getDeliveredAt().toLocalDate()));
        }
        description.append("\n");

        // 납품 정보
        description.append(generateTaskDescription(delivery));

        return description.toString();
    }

    private Task createShipmentTask(Delivery delivery, Client client, String username, LocalDate requestedAt) {
        String title = String.format("[출하 요청] %s - %s", client.getName(), delivery.getDeliveryNumber());
        String description = generateShipmentTaskDescription(delivery);

        Task task = new Task(
                title,
                description,
                username,
                requestedAt,
                requestedAt,
                TaskStatus.TODO,
                Priority.MEDIUM
        );

        return taskRepository.save(task);
    }

    private String generateShipmentTaskDescription(Delivery delivery) {
        StringBuilder description = new StringBuilder();

        // 주문일 정보
        description.append(String.format("주문일: %s\n\n", delivery.getOrderedAt()));

        // 납품 정보
        description.append(generateTaskDescription(delivery));

        return description.toString();
    }

    private String generateTaskDescription(Delivery delivery) {
        StringBuilder description = new StringBuilder();
        description.append("납품 정보:\n");

        for (DeliveryItem item : delivery.getItems()) {
            description.append(String.format("- %s: %d개",
                    item.getProduct().getName(),
                    item.getQuantity()));

            // 할인 정보 표시
            if (item.isDiscounted()) {
                description.append(String.format(" (%.1f%% 할인)", item.getDiscountRate()));
            }
            description.append("\n");
        }

        description.append(String.format("\n소계: %s%s",
                delivery.getClient().getCurrency().getSymbol(),
                delivery.getSubtotalAmount()));

        // 전체 할인 정보
        if (delivery.hasDiscount()) {
            description.append(String.format("\n할인: -%s%s",
                    delivery.getClient().getCurrency().getSymbol(),
                    delivery.getTotalDiscountAmount()));
        }

        description.append(String.format("\n이액: %s%s",
                delivery.getClient().getCurrency().getSymbol(),
                delivery.getTotalAmount()));

        // 원화 환산 금액 추가
        if (delivery.getTotalAmountKRW() != null) {
            description.append(String.format(" (₩%s)", delivery.getTotalAmountKRW()));
        }

        return description.toString();
    }

    /**
     * DeliveryItem
     */
    @Transactional
    public DeliveryItemBulkRegisterCommand.Result bulkRegisterDeliveryItems(MultipartFile file, CustomUserDetails currentUser) {
        List<DeliveryItemBulkRegisterCommand.BulkDeliveryItemData> bulkItems;

        try {
            bulkItems = deliveryItemBulkFileParser.parseFile(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("파일 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }

        int totalCount = bulkItems.size();
        int successCount = 0;
        List<DeliveryItemBulkRegisterCommand.FailureDetail> failures = new ArrayList<>();

        for (int i = 0; i < bulkItems.size(); i++) {
            DeliveryItemBulkRegisterCommand.BulkDeliveryItemData bulkData = bulkItems.get(i);
            int rowNumber = i + 2; // Excel/CSV의 실제 행 번호 (헤더 + 0-based index)

            try {
                validateBulkDeliveryItemData(bulkData);

                // Delivery 조회
                Delivery delivery = deliveryRepository.findByDeliveryNumber(bulkData.deliveryNumber())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않는 납품 번호입니다: " + bulkData.deliveryNumber()));

                // Product 조회
                Product product = productRepository.findByProductCodeAndNotDeleted(bulkData.productCode())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않는 제품 코드입니다: " + bulkData.productCode()));

                // 기준 단가 조회
                BigDecimal baseUnitPrice = getBaseUnitPrice(
                        delivery.getClient().getId(),
                        product.getId(),
                        product
                );

                // 실제 적용 단가 결정
                BigDecimal actualUnitPrice;

                if (bulkData.isFreeItem() != null && bulkData.isFreeItem()) {
                    // 무상 제공 항목
                    DeliveryItem freeItem = DeliveryItem.createFreeItem(
                            delivery,
                            product,
                            bulkData.quantity(),
                            bulkData.priceNote()
                    );
                    delivery.addItem(freeItem);
                } else {
                    // 유상 항목
                    actualUnitPrice = bulkData.actualUnitPrice() != null
                            ? bulkData.actualUnitPrice()
                            : baseUnitPrice;

                    DeliveryItem item = new DeliveryItem(
                            delivery,
                            product,
                            bulkData.quantity(),
                            baseUnitPrice,
                            actualUnitPrice,
                            bulkData.priceNote()
                    );
                    delivery.addItem(item);
                }


                // Task 생성
                Task orderTask = createOrderTask(delivery, delivery.getClient(), currentUser.getUsername(), delivery.getOrderedAt());
                delivery.setOrderTask(orderTask);

                Task shipmentTask = createShipmentTask(delivery,  delivery.getClient(), currentUser.getUsername(), delivery.getRequestedAt());
                delivery.setShipmentTask(shipmentTask);

                if (delivery.getStatus() == DeliveryStatus.COMPLETED) {
                    LocalDate actualDeliveredDate = delivery.getDeliveredAt() != null
                            ? delivery.getDeliveredAt().toLocalDate()
                            : LocalDate.now();

                    orderTask.updateStatus(TaskStatus.COMPLETED);
                    shipmentTask.updatePeriod(actualDeliveredDate, actualDeliveredDate);
                    shipmentTask.updateTaskInfo(
                            "[출하 완료] " + delivery.getDeliveryNumber(),
                            shipmentTask.getDescription(),
                            shipmentTask.getPriority()
                    );
                    shipmentTask.updateStatus(TaskStatus.COMPLETED);
                }

                successCount++;

            } catch (Exception e) {
                failures.add(DeliveryItemBulkRegisterCommand.FailureDetail.builder()
                        .rowNumber(rowNumber)
                        .deliveryNumber(bulkData.deliveryNumber())
                        .productCode(bulkData.productCode())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        int failureCount = totalCount - successCount;

        return new DeliveryItemBulkRegisterCommand.Result(
                totalCount,
                successCount,
                failureCount,
                failures
        );
    }

    private void validateBulkDeliveryItemData(DeliveryItemBulkRegisterCommand.BulkDeliveryItemData data) {
        if (data.deliveryNumber() == null || data.deliveryNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("납품 번호는 필수입니다");
        }
        if (data.productCode() == null || data.productCode().trim().isEmpty()) {
            throw new IllegalArgumentException("제품 코드는 필수입니다");
        }
        if (data.quantity() == null || data.quantity() <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다");
        }
        if (data.actualUnitPrice() != null && data.actualUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("단가는 0 이상이어야 합니다");
        }
    }

    private BigDecimal getBaseUnitPrice(Long clientId, Long productId, Product product) {
        return priceRepository.findByClientIdAndProductId(clientId, productId)
                .map(ClientProductPrice::getUnitPrice)
                .orElseGet(() -> product.getDefaultUnitPrice() != null
                        ? product.getDefaultUnitPrice()
                        : BigDecimal.ZERO);
    }
}
