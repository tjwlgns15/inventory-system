package com.yhs.inventroysystem.application.delivery;

import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.domain.delivery.DeliveryItem;
import com.yhs.inventroysystem.domain.delivery.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryCsvService {

    private final DeliveryRepository deliveryRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] exportAllDeliveriesToCsv() {
        List<Delivery> deliveries = deliveryRepository.findAllWithClientAndItem();
        return generateCsv(deliveries);
    }

    public byte[] exportDeliveryByIdToCsv(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("납품을 찾을 수 없습니다: " + deliveryId));
        return generateCsv(List.of(delivery));
    }

    private byte[] generateCsv(List<Delivery> deliveries) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            // UTF-8 BOM 추가 (엑셀에서 한글 깨짐 방지)
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // CSV 헤더
            writer.println(String.join(",",
                    "수주일",
                    "영업접수번호",
                    "거래처명",
                    "통화",
                    "제품명",
                    "수량",
                    "기준단가",
                    "실제단가",
                    "할인액",
                    "가격조정사유",
                    "품목금액",
                    "소계",
                    "환율",
                    "원화환산금액",
                    "출하요청일",
                    "실출하일",
                    "상태",
                    "메모"
            ));

            // 데이터 행
            for (Delivery delivery : deliveries) {
                for (DeliveryItem item : delivery.getItems()) {
                    writer.println(String.join(",",
                            formatDate(delivery.getOrderedAt()),
                            escapeCsv(delivery.getDeliveryNumber()),
                            escapeCsv(delivery.getClient().getName()),
                            escapeCsv(delivery.getClient().getCurrency().name() + "(" + delivery.getClient().getCurrency().getSymbol() + ")"),
                            escapeCsv(item.getProduct().getName()),
                            item.getQuantity().toString(),
                            item.getBaseUnitPrice().toString(),
                            item.getActualUnitPrice().toString(),
                            item.getDiscountAmount() != null ? item.getDiscountAmount().toString() : "0",
                            escapeCsv(item.getPriceNote()),
                            item.getTotalPrice().toString(),
                            delivery.getSubtotalAmount() != null ? delivery.getSubtotalAmount().toString() : "0",
                            delivery.getExchangeRate() != null ? delivery.getExchangeRate().toString() : "-",
                            delivery.getTotalAmountKRW() != null ? delivery.getTotalAmountKRW().toString() : "-",
                            formatDate(delivery.getRequestedAt()),
                            formatDateTime(delivery.getDeliveredAt()),
                            getStatusText(delivery.getStatus().name()),
                            escapeCsv(delivery.getMemo())
                    ));
                }
            }

            writer.flush();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("CSV 생성 중 오류가 발생했습니다", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // 쉼표, 줄바꿈, 따옴표가 있으면 큰따옴표로 감싸기
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "-";
    }

    private String getStatusText(String status) {
        return switch (status) {
            case "PENDING" -> "대기";
            case "COMPLETED" -> "완료";
            case "CANCELLED" -> "취소";
            default -> status;
        };
    }
}
