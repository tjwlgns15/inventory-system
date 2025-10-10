package com.yhs.inventroysystem.presentation.client;

import com.yhs.inventroysystem.domain.client.Country;
import jakarta.validation.constraints.NotBlank;

public class CountryDtos {

    public record CountryRegisterRequest(
            @NotBlank(message = "국가 코드는 필수입니다")
            String code,

            @NotBlank(message = "국가명은 필수입니다")
            String name,

            String englishName
    ) {}

    public record CountryResponse(
            Long id,
            String code,
            String name,
            String englishName
    ) {
        public static CountryResponse from(Country country) {
            return new CountryResponse(
                    country.getId(),
                    country.getCode(),
                    country.getName(),
                    country.getEnglishName()
            );
        }
    }
}