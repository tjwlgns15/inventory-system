package com.yhs.inventroysystem.presentation.part;

import com.yhs.inventroysystem.domain.part.PartStockTransaction;
import com.yhs.inventroysystem.domain.part.TransactionType;

import java.time.LocalDateTime;

public class PartTransactionDtos {

    public record PartTransactionResponse(
            Long id,
            Long partId,
            String partCode,
            String partName,
            TransactionType type,
            String typeDisplayName,
            Integer beforeStock,
            Integer changeQuantity,
            Integer afterStock,
            LocalDateTime createdAt
    ) {
        public static PartTransactionResponse from(PartStockTransaction transaction) {
            return new PartTransactionResponse(
                    transaction.getId(),
                    transaction.getPart().getId(),
                    transaction.getPart().getPartCode(),
                    transaction.getPart().getName(),
                    transaction.getType(),
                    transaction.getType().getDisplayName(),
                    transaction.getBeforeStock(),
                    transaction.getChangeQuantity(),
                    transaction.getAfterStock(),
                    transaction.getCreatedAt()
            );
        }
    }
}
