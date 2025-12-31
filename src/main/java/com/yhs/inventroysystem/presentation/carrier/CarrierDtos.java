package com.yhs.inventroysystem.presentation.carrier;

import com.yhs.inventroysystem.domain.carrier.entity.Carrier;
import com.yhs.inventroysystem.domain.carrier.service.CarrierCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CarrierDtos {

    public record CarrierResponse(
            Long id,
            String name,
            String nameEn,
            String contactNumber,
            String email,
            String notes
    ) {
        public static CarrierResponse from(Carrier carrier) {
            return new CarrierResponse(
                    carrier.getId(),
                    carrier.getName(),
                    carrier.getNameEn(),
                    carrier.getContactNumber(),
                    carrier.getEmail(),
                    carrier.getNotes()
            );
        }
    }

    public record CarrierRegister(

            @NotBlank(message = "운송 방법은 필수입니다.")
            @Size(max = 50, message = "운송 방법은 50자 이내여야 합니다.")
            String name,

            @Size(max = 50, message = "영문명은 50자 이내여야 합니다.")
            String nameEn,

            @Pattern(regexp = "^[0-9\\-]{9,15}$",
                    message = "연락처 형식이 올바르지 않습니다.")
            String contactNumber,

            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @Size(max = 100, message = "이메일은 100자 이내여야 합니다.")
            String email,

            @Size(max = 255, message = "비고는 255자 이내여야 합니다.")
            String notes
    ) {
        public CarrierCommand.CarrierRegisterCommand toCommand() {
            return new CarrierCommand.CarrierRegisterCommand(
                    name,
                    nameEn,
                    contactNumber,
                    email,
                    notes
            );
        }
    }

    public record CarrierUpdate(

            @Size(max = 50, message = "운송 방법은 50자 이내여야 합니다.")
            String name,

            @Size(max = 50, message = "영문명은 50자 이내여야 합니다.")
            String nameEn,

            @Pattern(regexp = "^[0-9\\-]{9,15}$",
                    message = "연락처 형식이 올바르지 않습니다.")
            String contactNumber,

            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @Size(max = 100, message = "이메일은 100자 이내여야 합니다.")
            String email,

            @Size(max = 255, message = "비고는 255자 이내여야 합니다.")
            String notes
    ) {
        public CarrierCommand.CarrierUpdateCommand toCommand() {
            return new CarrierCommand.CarrierUpdateCommand(
                    name,
                    nameEn,
                    contactNumber,
                    email,
                    notes
            );
        }
    }
}
