package com.yhs.inventroysystem.domain.carrier.service;

public class CarrierCommand {

    public record CarrierRegisterCommand(
            String name,
            String nameEn,
            String contactNumber,
            String email,
            String notes
    ) {
    }

    public record CarrierUpdateCommand(
            String name,
            String nameEn,
            String contactNumber,
            String email,
            String notes
    ) {
    }
}
