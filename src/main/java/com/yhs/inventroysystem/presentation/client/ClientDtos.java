package com.yhs.inventroysystem.presentation.client;

import com.yhs.inventroysystem.domain.client.entity.Client;
import com.yhs.inventroysystem.domain.client.entity.ClientType;
import com.yhs.inventroysystem.domain.exchange.entity.Currency;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ClientDtos {

    public record ParentClientRegisterRequest(
            @NotBlank(message = "거래처 코드는 필수입니다")
            String clientCode,

            @NotNull(message = "국가는 필수입니다")
            Long countryId,

            @NotBlank(message = "거래처명은 필수입니다")
            String name,

            String address,
            String contactNumber,

            @Email(message = "올바른 이메일 형식이 아닙니다")
            String email,

            @NotNull(message = "통화는 필수입니다")
            Currency currency,

            // 배송지 정보
            String shipmentDestination, // 선적처
            String shipmentAddress, // 선적 주소
            String shipmentRepresentative, // 선적처 담당자
            String shipmentContactNumber, // 선적처 연락처
            String finalDestination // 도착항
    ) {}

    public record ChildClientRegisterRequest(
            @NotNull(message = "상위 거래처는 필수입니다")
            Long parentClientId,

            @NotBlank(message = "거래처 코드는 필수입니다")
            String clientCode,

            @NotNull(message = "국가는 필수입니다")
            Long countryId,

            @NotBlank(message = "거래처명은 필수입니다")
            String name,

            String address,
            String contactNumber,

            @Email(message = "올바른 이메일 형식이 아닙니다")
            String email,

            @NotNull(message = "통화는 필수입니다")
            Currency currency,

            // 배송지 정보
            String shipmentDestination,
            String shipmentAddress,
            String shipmentRepresentative,
            String shipmentContactNumber,
            String finalDestination
    ) {}

    public record ClientResponse(
            Long id,
            String clientCode,
            Long countryId,
            String countryName,
            String countryCode,
            String name,
            String address,
            String contactNumber,
            String email,
            Currency currency,
            String currencyName,
            String currencySymbol,
            ClientType clientType,
            Long parentClientId,
            String parentClientName,

            // 배송지 정보
            String shipmentDestination,
            String shipmentAddress,
            String shipmentRepresentative,
            String shipmentContactNumber,
            String finalDestination
    ) {
        public static ClientResponse from(Client client) {
            return new ClientResponse(
                    client.getId(),
                    client.getClientCode(),
                    client.getCountry() != null ? client.getCountry().getId() : null,
                    client.getCountry() != null ? client.getCountry().getName() : null,
                    client.getCountry() != null ? client.getCountry().getCode() : null,
                    client.getName(),
                    client.getAddress(),
                    client.getContactNumber(),
                    client.getEmail(),
                    client.getCurrency(),
                    client.getCurrency().getName(),
                    client.getCurrency().getSymbol(),
                    client.getClientType(),
                    client.getParentClient() != null ? client.getParentClient().getId() : null,
                    client.getParentClient() != null ? client.getParentClient().getName() : null,
                    client.getShipmentDestination(),
                    client.getShipmentAddress(),
                    client.getShipmentRepresentative(),
                    client.getShipmentContactNumber(),
                    client.getFinalDestination()
            );
        }
    }

    public record ClientUpdateRequest(
            @NotBlank(message = "거래처명은 필수입니다")
            String name,

            @NotNull(message = "국가는 필수입니다")
            Long countryId,

            String address,
            String contactNumber,
            String email,

            @NotNull(message = "통화는 필수입니다")
            Currency currency,

            String shipmentDestination,
            String shipmentAddress,
            String shipmentRepresentative,
            String shipmentContactNumber,
            String finalDestination


    ) {}
}
