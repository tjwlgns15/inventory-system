package com.yhs.inventroysystem.application.delivery;

import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.exchange.ExchangeRateService;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.exchange.ExchangeRate;
import com.yhs.inventroysystem.domain.price.ClientProductPriceRepository;
import com.yhs.inventroysystem.domain.client.Client;
import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.domain.delivery.DeliveryItem;
import com.yhs.inventroysystem.domain.price.ClientProductPrice;
import com.yhs.inventroysystem.domain.product.Product;
import com.yhs.inventroysystem.domain.client.ClientRepository;
import com.yhs.inventroysystem.domain.delivery.DeliveryRepository;
import com.yhs.inventroysystem.domain.product.ProductRepository;
import com.yhs.inventroysystem.domain.task.Priority;
import com.yhs.inventroysystem.domain.task.Task;
import com.yhs.inventroysystem.domain.task.TaskRepository;
import com.yhs.inventroysystem.domain.task.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static com.yhs.inventroysystem.application.delivery.DeliveryCommands.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final ClientProductPriceRepository priceRepository;
    private final StockDeductionService stockDeductionService;
    private final TaskRepository taskRepository;
    private final ExchangeRateService exchangeRateService;

    private static final String DELIVERY_PREFIX = "SOLM-PO-";


    @Transactional
    public Delivery createDelivery(DeliveryCreateCommand command, CustomUserDetails currentUser) {
        Client client = clientRepository.findById(command.clientId())
                .orElseThrow(() -> ResourceNotFoundException.client(command.clientId()));

        String deliveryNumber = generateDeliveryNumber(command.orderedAt());
        Delivery delivery = new Delivery(deliveryNumber, client, command.orderedAt(),  command.requestedAt());

        // 납품 항목 추가
        for (DeliveryItemInfo itemInfo : command.items()) {
            Product product = productRepository.findById(itemInfo.productId())
                    .orElseThrow(() -> ResourceNotFoundException.product(itemInfo.productId()));

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
        ExchangeRate exchangeRate = exchangeRateService.getLatestExchangeRate(client.getCurrency());
        delivery.setExchangeRate(exchangeRate.getRate());

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // 주문일 Task
        Task orderTask = createOrderTask(savedDelivery, client, currentUser, command.orderedAt());
        savedDelivery.setOrderTask(orderTask);

        // 출하 요청일 Task
        Task shipmentTask = createShipmentTask(savedDelivery, client, currentUser, command.requestedAt());
        savedDelivery.setShipmentTask(shipmentTask);

        return savedDelivery;
    }

    public List<Delivery> findAllDelivery() {
        return deliveryRepository.findAllWithClientAndItem();
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
        Delivery delivery = deliveryRepository.findByIdWithItems(deliveryId)
                .orElseThrow(() -> ResourceNotFoundException.delivery(deliveryId));

        // 재고 차감
        stockDeductionService.deductStock(delivery);

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
        Delivery delivery = deliveryRepository.findByIdWithItems(deliveryId)
                .orElseThrow(() -> ResourceNotFoundException.delivery(deliveryId));

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
            taskRepository.delete(shipmentTask);
        }
    }


    public Delivery findDeliveryById(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> ResourceNotFoundException.delivery(deliveryId));
    }

    private String generateDeliveryNumber(LocalDate orderedAt) {
        String year = orderedAt.format(DateTimeFormatter.ofPattern("yyyy"));
        String prefix = DELIVERY_PREFIX + year;

        for (int attempt = 0; attempt < 10; attempt++) {
            Integer lastSequence = deliveryRepository.findLastSequenceByYear(year);
            int nextSequence = (lastSequence == null) ? 1 : lastSequence + 1;

            String deliveryNumber = String.format("%s-%04d", prefix, nextSequence);

            if (!deliveryRepository.existsByDeliveryNumber(deliveryNumber)) {
                return deliveryNumber;
            }
        }

        return String.format("%s-%s", prefix,
                UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }


    private BigDecimal getBaseUnitPrice(Long clientId, Long productId, Product product) {
        return priceRepository.findByClientIdAndProductId(clientId, productId)
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

        return taskRepository.save(task);
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
}
