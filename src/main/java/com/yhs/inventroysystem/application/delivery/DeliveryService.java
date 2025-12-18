package com.yhs.inventroysystem.application.delivery;

import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.domain.client.entity.Client;
import com.yhs.inventroysystem.domain.client.service.ClientDomainService;
import com.yhs.inventroysystem.domain.delivery.entity.Delivery;
import com.yhs.inventroysystem.domain.delivery.entity.DeliveryDocument;
import com.yhs.inventroysystem.domain.delivery.entity.DeliveryItem;
import com.yhs.inventroysystem.domain.delivery.service.DeliveryDomainService;
import com.yhs.inventroysystem.domain.exchange.entity.Currency;
import com.yhs.inventroysystem.domain.exchange.entity.ExchangeRate;
import com.yhs.inventroysystem.domain.exchange.service.ExchangeDomainService;
import com.yhs.inventroysystem.domain.price.entity.ClientProductPrice;
import com.yhs.inventroysystem.domain.price.service.PriceDomainService;
import com.yhs.inventroysystem.domain.product.entity.Product;
import com.yhs.inventroysystem.domain.product.entity.ProductTransactionType;
import com.yhs.inventroysystem.domain.product.service.ProductDomainService;
import com.yhs.inventroysystem.domain.product.service.ProductStockTransactionDomainService;
import com.yhs.inventroysystem.domain.task.entity.Priority;
import com.yhs.inventroysystem.domain.task.entity.Task;
import com.yhs.inventroysystem.domain.task.entity.TaskCategory;
import com.yhs.inventroysystem.domain.task.entity.TaskStatus;
import com.yhs.inventroysystem.domain.task.service.TaskCategoryDomainService;
import com.yhs.inventroysystem.domain.task.service.TaskDomainService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageFactory;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageType;
import com.yhs.inventroysystem.infrastructure.pagenation.PageableUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.yhs.inventroysystem.application.delivery.DeliveryCommands.*;

@Service
@Transactional(readOnly = true)
@Slf4j
public class DeliveryService {

    private final DeliveryDomainService deliveryDomainService;
    private final ClientDomainService clientDomainService;
    private final ExchangeDomainService exchangeDomainService;
    private final ProductDomainService productDomainService;
    private final PriceDomainService priceDomainService;
    private final TaskDomainService taskDomainService;
    private final TaskCategoryDomainService taskCategoryDomainService;
    private final ProductStockTransactionDomainService productStockTransactionDomainService;
    private final FileStorageService fileStorageService;
    private final RestTemplate restTemplate;

    public DeliveryService(DeliveryDomainService deliveryDomainService,
                           ClientDomainService clientDomainService,
                           ExchangeDomainService exchangeDomainService,
                           ProductDomainService productDomainService,
                           PriceDomainService priceDomainService,
                           TaskDomainService taskDomainService,
                           TaskCategoryDomainService taskCategoryDomainService,
                           ProductStockTransactionDomainService productStockTransactionDomainService,
                           FileStorageFactory fileStorageFactory,
                           RestTemplate restTemplate) {
        this.deliveryDomainService = deliveryDomainService;
        this.clientDomainService = clientDomainService;
        this.exchangeDomainService = exchangeDomainService;
        this.productDomainService = productDomainService;
        this.priceDomainService = priceDomainService;
        this.taskDomainService = taskDomainService;
        this.taskCategoryDomainService = taskCategoryDomainService;
        this.productStockTransactionDomainService = productStockTransactionDomainService;
        this.fileStorageService = fileStorageFactory.getStorageService(FileStorageType.DELIVERY_DOCUMENT);
        this.restTemplate = restTemplate;
    }

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/KRW";
    private static final String DELIVERY_PREFIX = "SOLM-PO-";


    @Transactional
    public Delivery createDelivery(DeliveryCreateCommand command, CustomUserDetails currentUser) {
        Client client = clientDomainService.findById(command.clientId());

        String deliveryNumber = generateDeliveryNumber(command.orderedAt());
        Delivery delivery = new Delivery(deliveryNumber, client, command.orderedAt(), command.requestedAt());

        // 납품 항목 추가
        for (DeliveryItemInfo itemInfo : command.items()) {
            Product product = productDomainService.findById(itemInfo.productId());

            // 기준 거래 단가 조회
            BigDecimal baseUnitPrice = getBaseUnitPrice(command.clientId(), itemInfo.productId(), product);

            // 실적용 단가
            BigDecimal actualUnitPrice = itemInfo.actualUnitPrice() != null
                    ? itemInfo.actualUnitPrice()
                    : baseUnitPrice;

            DeliveryItem item = new DeliveryItem(
                    delivery,
                    product,
                    itemInfo.quantity(),
                    baseUnitPrice,
                    actualUnitPrice,
                    itemInfo.priceNote()
            );
            delivery.addItem(item);
        }

        // 환율 조회 및 설정
        ExchangeRate exchangeRate = getLatestExchangeRate(client.getCurrency());
        delivery.setExchangeRate(exchangeRate.getRate());

        Delivery savedDelivery = deliveryDomainService.saveDelivery(delivery);

        TaskCategory orderDeliveryCategory = taskCategoryDomainService.findByName("수주/납품");

        // 주문일 Task
        Task orderTask = createOrderTask(savedDelivery, client, currentUser, command.orderedAt());
        orderTask.addCategory(orderDeliveryCategory);
        savedDelivery.setOrderTask(orderTask);

        // 출하 요청일 Task
        Task shipmentTask = createShipmentTask(savedDelivery, client, currentUser, command.requestedAt());
        shipmentTask.addCategory(orderDeliveryCategory);
        savedDelivery.setShipmentTask(shipmentTask);

        return savedDelivery;
    }

    public Page<Delivery> searchDeliveries(String keyword, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);
        Page<Delivery> deliveryPage = deliveryDomainService.searchByKeyword(keyword, pageable);

        // items 초기화 (BatchSize 동작, LAZY 로딩 때문)
        deliveryPage.getContent().forEach(delivery -> {
            delivery.getItems().forEach(item -> {
                item.getProduct().getName();
            });
        });

        return deliveryPage;
    }

    public Page<Delivery> findAllDeliveriesPaged(int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);
        Page<Delivery> deliveryPage = deliveryDomainService.findAllPaged(pageable);

        // items 초기화 (BatchSize 동작, LAZY 로딩 때문)
        deliveryPage.getContent().forEach(delivery -> {
            delivery.getItems().forEach(item -> {
                item.getProduct().getName();
            });
        });

        return deliveryPage;
    }

    @Transactional
    public Delivery updateMemo(Long deliveryId, String memo) {
        Delivery delivery = findDeliveryById(deliveryId);

        delivery.updateMemo(memo);
        return delivery;
    }

    @Transactional
    public Delivery applyDiscount(Long deliveryId, DeliveryDiscountCommand command) {
        Delivery delivery = findDeliveryById(deliveryId);
        delivery.applyDiscount(command.discountAmount(), command.note());
        return delivery;
    }

    @Transactional
    public Delivery applyDiscountRate(Long deliveryId, DeliveryDiscountRateCommand command) {
        Delivery delivery = findDeliveryById(deliveryId);
        delivery.applyDiscountRate(command.discountRate(), command.note());
        return delivery;
    }

    @Transactional
    public Delivery clearDiscount(Long deliveryId) {
        Delivery delivery = findDeliveryById(deliveryId);
        delivery.clearDiscount();
        return delivery;
    }

    @Transactional
    public void completeDelivery(Long deliveryId) {
        Delivery delivery = deliveryDomainService.findByIdWithItems(deliveryId);

        // 재고 차감
        deductProductStock(delivery);

        // 납품 완료 처리
        delivery.complete();

        LocalDate actualDeliveredDate = delivery.getDeliveredAt().toLocalDate();

        // 주문 Task 완료 처리
        if (delivery.getOrderTask() != null) {
            Task orderTask = delivery.getOrderTask();
            orderTask.updateStatus(TaskStatus.COMPLETED);
        }

        // 출하 요청 Task 완료 처리 및 날짜 업데이트
        if (delivery.getShipmentTask() != null) {
            Task shipmentTask = delivery.getShipmentTask();
            shipmentTask.updatePeriod(actualDeliveredDate, actualDeliveredDate);
            shipmentTask.updateTaskInfo(
                    "[출하 완료] " + delivery.getDeliveryNumber(),
                    shipmentTask.getDescription(),
                    shipmentTask.getPriority()
            );
            shipmentTask.updateStatus(TaskStatus.COMPLETED);
        }
    }

    @Transactional
    public void cancelDelivery(Long deliveryId) {
        Delivery delivery = deliveryDomainService.findByIdWithItems(deliveryId);

        // 취소 처리
        delivery.cancel();

        LocalDate cancelDate = LocalDate.now();

        // 주문 Task 취소 처리
        if (delivery.getOrderTask() != null) {
            Task orderTask = delivery.getOrderTask();
            orderTask.updateTaskInfo(
                    "[주문 취소] " + delivery.getDeliveryNumber(),
                    orderTask.getDescription(),
                    orderTask.getPriority()
            );
            orderTask.updatePeriod(orderTask.getStartDate(), orderTask.getStartDate());
            orderTask.updateStatus(TaskStatus.COMPLETED);
        }

        // 출하 요청 Task 삭제
        if (delivery.getShipmentTask() != null) {
            Task shipmentTask = delivery.getShipmentTask();
            delivery.clearShipmentTask(); // 연관관계 제거
            taskDomainService.deleteTask(shipmentTask.getId());
        }
    }

    @Transactional
    public void deleteDelivery(Long deliveryId) {
        Delivery delivery = deliveryDomainService.findByIdWithItems(deliveryId);

        deliveryDomainService.validateDeliveryCompleted(delivery);

        List<DeliveryDocument> documents = delivery.getDocuments();

        for (DeliveryDocument document : documents) {
            fileStorageService.delete(document.getFilePath());
        }

        // 삭제
        delivery.markAsDeleted();

        // 제품 수량 복구
        delivery.getItems().forEach(deliveryItem -> {
            Product product = deliveryItem.getProduct();
            Integer quantity = deliveryItem.getQuantity();

            Integer beforeStock = product.getStockQuantity();
            product.increaseStock(quantity);

            productStockTransactionDomainService.recordTransaction(
                    product,
                    ProductTransactionType.DELIVERY_CANCELLED,
                    beforeStock,
                    quantity
            );
        });

        LocalDate cancelDate = LocalDate.now();

        // 주문 Task 취소 처리
        if (delivery.getOrderTask() != null) {
            Task orderTask = delivery.getOrderTask();
            orderTask.updateTaskInfo(
                    "[납품 취소] " + delivery.getDeliveryNumber(),
                    orderTask.getDescription(),
                    orderTask.getPriority()
            );
            orderTask.updatePeriod(orderTask.getStartDate(), orderTask.getStartDate());
            orderTask.updateStatus(TaskStatus.COMPLETED);
        }

        // 출하 요청 Task 삭제
        if (delivery.getShipmentTask() != null) {
            Task shipmentTask = delivery.getShipmentTask();
            delivery.clearShipmentTask(); // 연관관계 제거
            taskDomainService.deleteTask(shipmentTask.getId());
        }
    }

    public Delivery findDeliveryById(Long deliveryId) {
        return deliveryDomainService.findById(deliveryId);
    }

    /**
     * 수주 번호 생성
     */
    private String generateDeliveryNumber(LocalDate orderedAt) {
        String year = orderedAt.format(DateTimeFormatter.ofPattern("yyyy"));
        String prefix = DELIVERY_PREFIX + year;

        // synchronized 블록으로 동시성 제어
        synchronized (this) {
            Integer lastSequence = deliveryDomainService.findLastSequenceByYear(year);
            int nextSequence = (lastSequence == null) ? 1 : lastSequence + 1;

            String deliveryNumber = String.format("%s-%04d", prefix, nextSequence);

            // 중복 체크 (만약을 위해)
            while (deliveryDomainService.existsByDeliveryNumber(deliveryNumber)) {
                nextSequence++;
                deliveryNumber = String.format("%s-%04d", prefix, nextSequence);
            }

            return deliveryNumber;
        }
    }

    private BigDecimal getBaseUnitPrice(Long clientId, Long productId, Product product) {
        return priceDomainService.optionalClientIdAndProductId(clientId, productId)
                .map(ClientProductPrice::getUnitPrice)
                .orElseGet(() -> product.getDefaultUnitPrice() != null
                        ? product.getDefaultUnitPrice()
                        : BigDecimal.ZERO);
    }

    private Task createOrderTask(Delivery delivery, Client client, CustomUserDetails currentUser, LocalDate orderedAt) {
        String title = String.format("[주문] %s - %s", client.getName(), delivery.getDeliveryNumber());
        String description = generateOrderTaskDescription(delivery);

        Task task = new Task(
                title,
                description,
                currentUser.getName(),
                orderedAt,
                orderedAt,
                TaskStatus.TODO,
                Priority.MEDIUM
        );

        return taskDomainService.saveTask(task);
    }

    private Task createShipmentTask(Delivery delivery, Client client, CustomUserDetails currentUser, LocalDate requestedAt) {
        String title = String.format("[출하 요청] %s - %s", client.getName(), delivery.getDeliveryNumber());
        String description = generateShipmentTaskDescription(delivery);

        Task task = new Task(
                title,
                description,
                currentUser.getName(),
                requestedAt,
                requestedAt,
                TaskStatus.TODO,
                Priority.MEDIUM
        );

        return taskDomainService.saveTask(task);
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
     * 최신 환율 조회 (오늘 날짜 기준)
     */
    @Transactional
    public ExchangeRate getLatestExchangeRate(Currency currency) {
        LocalDate now = LocalDate.now();

        // KRW는 환율이 1
        if (currency == Currency.KRW) {
            return new ExchangeRate(Currency.KRW, BigDecimal.ONE, now);
        }

        return exchangeDomainService.findByCurrencyAndDate(currency, now)
                .orElseGet(() -> fetchAndSaveExchangeRate(currency, now));
    }

    /**
     * 외부 API에서 환율 가져와서 저장
     */
    @Transactional
    public ExchangeRate fetchAndSaveExchangeRate(Currency currency, LocalDate date) {
        try {
            // API 호출
            Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);

            if (response != null && response.containsKey("rates")) {
                Map<String, Double> rates = (Map<String, Double>) response.get("rates");

                // KRW 기준이므로 역수 계산
                // 예: API에서 1 KRW = 0.00075 USD라면, 1 USD = 1333.33 KRW
                Double rate = rates.get(currency.getCode());
                BigDecimal krwRate = BigDecimal.ONE.divide(
                        BigDecimal.valueOf(rate),
                        6,
                        BigDecimal.ROUND_HALF_UP
                );

                ExchangeRate exchangeRate = new ExchangeRate(currency, krwRate, date);
                return exchangeDomainService.saveExchangeRate(exchangeRate);
            }
        } catch (Exception e) {
            log.error("환율 조회 실패: {}", e.getMessage());
        }

        // 실패 시 기본값 반환 (고정 환율)
        return getDefaultExchangeRate(currency, date);
    }

    /**
     * API 실패 시 기본 환율 반환
     */
    private ExchangeRate getDefaultExchangeRate(Currency currency, LocalDate date) {
        BigDecimal defaultRate = switch (currency) {
            case USD -> BigDecimal.valueOf(1300.0);
            case JPY -> BigDecimal.valueOf(9.5);
            case EUR -> BigDecimal.valueOf(1400.0);
            case CNY -> BigDecimal.valueOf(180.0);
            case GBP -> BigDecimal.valueOf(1650.0);
            default -> BigDecimal.ONE;
        };

        ExchangeRate exchangeRate = new ExchangeRate(currency, defaultRate, date);
        return exchangeDomainService.saveExchangeRate(exchangeRate);
    }

    private void deductProductStock(Delivery delivery) {
        for (DeliveryItem item : delivery.getItems()) {
            Product product = productDomainService.findProductWithParts(item.getProduct().getId());
            Integer beforeStock = product.getStockQuantity();

            // 제품 재고 차감
            product.decreaseStock(item.getQuantity());

            productStockTransactionDomainService.recordTransaction(
                    product,
                    ProductTransactionType.DELIVERY,
                    beforeStock,
                    -item.getQuantity()
            );
        }
    }
}
