package com.yhs.inventroysystem.dto;

import com.yhs.inventroysystem.entity.StockTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockTransactionResponse {
    private Long id;
    private PartInfo part;
    private String type;
    private int beforeStock;
    private int changeQuantity;
    private int afterStock;
    private LocalDateTime timestamp;

    // Part 정보를 담는 내부 클래스
    @Getter
    @AllArgsConstructor
    public static class PartInfo {
        private Long id;
        private String partCode;
        private String name;
    }

    // StockTransaction 엔티티에서 DTO로 변환하는 팩토리 메서드
    public static StockTransactionResponse from(StockTransaction transaction) {
        PartInfo partInfo = new PartInfo(
                transaction.getPart().getId(),
                transaction.getPart().getPartCode(),
                transaction.getPart().getName()
        );

        return new StockTransactionResponse(
                transaction.getId(),
                partInfo,
                transaction.getType().name(),
                transaction.getBeforeStock(),
                transaction.getChangeQuantity(),
                transaction.getAfterStock(),
                transaction.getCreatedAt()
        );
    }
}