package com.yhs.inventroysystem.presentation.product;

import com.yhs.inventroysystem.domain.product.entity.ProductLine;
import jakarta.validation.constraints.NotBlank;

public class ProductLineDtos {

    public record PLRegisterRequest(
            @NotBlank(message = "카테고리 이름은 필수입니다.")
            String name
    ) {}

    public record PLResponse(
            Long partId,
            String name
    ) {
        public static PLResponse from(ProductLine productLine) {
            return new PLResponse(
                    productLine.getId(),
                    productLine.getName()
            );
        }
    }

    public record PLUpdateRequest(
            @NotBlank(message = "카테고리 이름은 필수입니다.")
            String name
    ) {}
}
