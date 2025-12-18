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
            Currency currency
    ) {}

    public record ChildClientRegisterCommand(
            Long parentClientId,
            String clientCode,
            Long countryId,
            String name,
            String address,
            String contactNumber,
            String email,
            Currency currency
    ) {}

    public record ChildClientUpdateCommand(
            String name,
            Long countryId,
            String address,
            String contactNumber,
            String email,
            Currency currency
    ) {}
}
