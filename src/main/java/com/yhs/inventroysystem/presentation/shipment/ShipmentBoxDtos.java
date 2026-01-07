package com.yhs.inventroysystem.presentation.shipment;

import com.yhs.inventroysystem.application.shipment.ShipmentBoxCommand;
import com.yhs.inventroysystem.application.shipment.ShipmentCommand.*;
import com.yhs.inventroysystem.domain.shipment.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.yhs.inventroysystem.application.shipment.ShipmentBoxCommand.*;

public class ShipmentBoxDtos {

    /**
     * 박스 정보 응답 DTO
     */
    public record ShipmentBoxResponse (
        Long id,
        String title,
        BigDecimal width,
        BigDecimal length,
        BigDecimal height,
        Boolean isActivate,
        String dimensionString
    ){
        public static ShipmentBoxResponse from(ShipmentBox box) {
            return new ShipmentBoxResponse(
                    box.getId(),
                    box.getTitle(),
                    box.getWidth(),
                    box.getLength(),
                    box.getHeight(),
                    box.getIsActive(),
                    box.getDimensionString()
            );
        }
    }
    /**
     * 박스 정보 DTO
     */
    public record ShipmentBoxCreate(

            @NotNull(message = "이름은 필수입니다")
            String title,

            @NotNull(message = "박스 가로는 필수입니다")
            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal width,

            @NotNull(message = "박스 세로는 필수입니다")
            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal length,

            @NotNull(message = "박스 높이는 필수입니다")
            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal height
    ) {
        public ShipmentBoxCreateCommand toCommand() {
            return new ShipmentBoxCreateCommand(
                    title,
                    width,
                    length,
                    height
            );
        }
    }

    /**
     * 박스 정보 DTO
     */
    public record ShipmentBoxUpdate(

            @NotNull(message = "이름은 필수입니다")
            String title,

            @NotNull(message = "박스 가로는 필수입니다")
            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal width,

            @NotNull(message = "박스 세로는 필수입니다")
            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal length,

            @NotNull(message = "박스 높이는 필수입니다")
            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal height
    ) {
        public ShipmentBoxUpdateCommand toCommand() {
            return new ShipmentBoxUpdateCommand(
                    title,
                    width,
                    length,
                    height
            );
        }
    }
}

