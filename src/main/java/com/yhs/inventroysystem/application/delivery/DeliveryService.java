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
            BigDecimal actualUnitPrice;
            if (itemInfo.actualUnitPrice() != null) {
                actualUnitPrice = itemInfo.actualUnitPrice();
            } else {
                actualUnitPrice = baseUnitPrice;
            }

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

        Task task = createTaskForDelivery(savedDelivery, client, currentUser, command.orderedAt(), command.requestedAt());
        savedDelivery.setRelatedTask(task);

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

        // 연관된 작업 완료 처리
        if (delivery.getRelatedTask() != null) {
            Task task = delivery.getRelatedTask();

            task.updateTaskInfo(
                    "[납품 완료] " + delivery.getDeliveryNumber(),
                    task.getDescription(),
                    task.getPriority()
            );
            task.updatePeriod(task.getStartDate(), LocalDate.now());
            task.updateStatus(TaskStatus.COMPLETED);
        }
    }

    @Transactional
    public void cancelDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findByIdWithItems(deliveryId)
                .orElseThrow(() -> ResourceNotFoundException.delivery(deliveryId));

        // 취소 처리
        delivery.cancel();

        if (delivery.getRelatedTask() != null) {
            Task task = delivery.getRelatedTask();

            task.updateTaskInfo(
                    "[거래 취소] " + delivery.getDeliveryNumber(),
                    task.getDescription(),
                    task.getPriority()
            );
            task.updatePeriod(task.getStartDate(), LocalDate.now());
            task.updateStatus(TaskStatus.COMPLETED);
        }
    }


    public Delivery findDeliveryById(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> ResourceNotFoundException.delivery(deliveryId));
    }

    private String generateDeliveryNumber(LocalDate orderedAt) {
        String yearMonth = orderedAt.format(DateTimeFormatter.ofPattern("yyMM"));

        String prefix = DELIVERY_PREFIX + yearMonth;
        Integer lastSequence = deliveryRepository.findLastSequenceByYearMonth(yearMonth);

        if (lastSequence == null) {
            return String.format("%s-%03d", prefix, 1);
        } else {
            return String.format("%s-%03d", prefix, lastSequence + 1);
        }
    }

    private BigDecimal getBaseUnitPrice(Long clientId, Long productId, Product product) {
        return priceRepository.findByClientIdAndProductId(clientId, productId)
                .map(ClientProductPrice::getUnitPrice)
                .orElseGet(() -> product.getDefaultUnitPrice() != null
                        ? product.getDefaultUnitPrice()
                        : BigDecimal.ZERO);
    }

    private Task createTaskForDelivery(Delivery delivery, Client client, CustomUserDetails currentUser, LocalDate orderedAt, LocalDate requestedAt) {
        String title = generateTaskTitle(client, delivery);
        String description = generateTaskDescription(delivery);


        Task task = new Task(
                title,
                description,
                currentUser.getName(),
                orderedAt,
                requestedAt,
                TaskStatus.TODO,
                Priority.MEDIUM
        );

        return taskRepository.save(task);
    }

    private String generateTaskTitle(Client client, Delivery delivery) {
        return String.format("[주문] %s - %s",
                client.getName(),
                delivery.getDeliveryNumber());
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

        description.append(String.format("\n총액: %s%s",
                delivery.getClient().getCurrency().getSymbol(),
                delivery.getTotalAmount()));

        // 원화 환산 금액 추가
        if (delivery.getTotalAmountKRW() != null) {
            description.append(String.format(" (₩%s)", delivery.getTotalAmountKRW()));
        }

        return description.toString();
    }
}
