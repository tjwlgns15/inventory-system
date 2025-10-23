package com.yhs.inventroysystem.presentation.price;

import com.yhs.inventroysystem.domain.client.Client;
import com.yhs.inventroysystem.domain.client.ClientType;
import com.yhs.inventroysystem.domain.price.ClientProductPrice;
import com.yhs.inventroysystem.domain.exchange.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PriceDtos {

    public record PriceRegisterRequest(
            @NotNull(message = "거래처 ID는 필수입니다")
            Long clientId,

            @NotNull(message = "제품 ID는 필수입니다")
            Long productId,

            @NotNull(message = "단가는 필수입니다")
            @Positive(message = "단가는 0보다 커야 합니다")
            BigDecimal unitPrice
    ) {
    }


    public record PriceUpdateRequest(
            @NotNull(message = "거래처 ID는 필수입니다")
            Long clientId,

            @NotNull(message = "제품 ID는 필수입니다")
            Long productId,

            @NotNull(message = "새 단가는 필수입니다")
            @Positive(message = "새 단가는 0보다 커야 합니다")
            BigDecimal newPrice
    ) {
    }


    public record PriceResponse(
            Long id,
            Long clientId,
            String clientName,
            String clientCode,
            ClientType clientType,
            Long parentClientId,
            String parentClientName,
            Long productId,
            String productName,
            String productCode,
            BigDecimal unitPrice,
            Currency currency,
            String currencyName,
            String currencySymbol
    ) {
        public static PriceResponse from(ClientProductPrice price) {
            Client client = price.getClient();
            return new PriceResponse(
                    price.getId(),
                    price.getClient().getId(),
                    price.getClient().getName(),
                    client.getClientCode(),
                    client.getClientType(),
                    client.getParentClient() != null ? client.getParentClient().getId() : null,
                    client.getParentClient() != null ? client.getParentClient().getName() : null,
                    price.getProduct().getId(),
                    price.getProduct().getName(),
                    price.getProduct().getProductCode(),
                    price.getUnitPrice(),
                    price.getClient().getCurrency(),
                    price.getClient().getCurrency().getName(),
                    price.getClient().getCurrency().getSymbol()
            );
        }
    }
}
