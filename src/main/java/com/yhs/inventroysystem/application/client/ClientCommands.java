package com.yhs.inventroysystem.application.client;

import com.yhs.inventroysystem.domain.exchange.Currency;

public class ClientCommands {

    public record ClientRegisterCommand(
            String clientCode,
            Long countryId,
            String name,
            String address,
            String contactNumber,
            String email,
            Currency currency
    ) {}
}
