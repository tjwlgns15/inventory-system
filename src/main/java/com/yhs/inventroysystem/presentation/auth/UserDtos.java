package com.yhs.inventroysystem.presentation.auth;

import com.yhs.inventroysystem.domain.auth.User;
import com.yhs.inventroysystem.domain.auth.UserRole;
import jakarta.validation.constraints.NotBlank;

public class UserDtos {

    public record SignupRequest(
            @NotBlank(message = "사용자명은 필수입니다")
//            @Size(min = 4, max = 20, message = "사용자명은 4-20자여야 합니다")
            String username,

            @NotBlank(message = "비밀번호는 필수입니다")
//            @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
            String password,

            @NotBlank(message = "이름은 필수입니다")
            String name,

            @NotBlank(message = "이메일은 필수입니다")
//            @Email(message = "올바른 이메일 형식이 아닙니다")
            String email
    ) {}

    public record LoginRequest(
            @NotBlank(message = "사용자명은 필수입니다")
            String username,

            @NotBlank(message = "비밀번호는 필수입니다")
            String password
    ) {}


    public record UserResponse(
            Long id,
            String username,
            String name,
            String email,
            UserRole userRole
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getName(),
                    user.getEmail(),
                    user.getUserRole()
            );
        }
    }

    public record TokenResponse(
            String token
    ) {}

    public record ChangePasswordRequest(
            @NotBlank(message = "현재 비밀번호는 필수입니다")
            String currentPassword,

            @NotBlank(message = "새 비밀번호는 필수입니다")
//            @Size(min = 8, message = "새 비밀번호는 최소 8자 이상이어야 합니다")
            String newPassword
    ) {}
}
