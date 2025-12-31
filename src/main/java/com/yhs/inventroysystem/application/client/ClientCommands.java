package com.yhs.inventroysystem.application.client;

import com.yhs.inventroysystem.domain.exchange.entity.Currency;

public class ClientCommands {

    public record ParentClientRegisterCommand(
            String clientCode,
            Long countryId,
            String name,
            String address,
            String contactNumber,
            String email,
            Currency currency,


            String shipmentDestination, // 선적처
            String shipmentAddress, // 선적 주소
            String shipmentRepresentative, // 선적처 담당자
            String shipmentContactNumber, // 선적처 연락처
            String finalDestination // 도착항
    ) {}

    public record ChildClientRegisterCommand(
            Long parentClientId,
            String clientCode,
            Long countryId,
            String name,
            String address,
            String contactNumber,
            String email,
            Currency currency,

            String shipmentDestination,
            String shipmentAddress,
            String shipmentRepresentative,
            String shipmentContactNumber,
            String finalDestination
    ) {}

    public record ChildClientUpdateCommand(
            String name,
            Long countryId,
            String address,
            String contactNumber,
            String email,
            Currency currency,

            String shipmentDestination,
            String shipmentAddress,
            String shipmentRepresentative,
            String shipmentContactNumber,
            String finalDestination
    ) {}
}
