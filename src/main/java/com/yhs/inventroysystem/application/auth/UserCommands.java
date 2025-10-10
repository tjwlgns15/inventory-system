package com.yhs.inventroysystem.application.auth;

public class UserCommands {

    public record SignupCommand(
            String username,
            String password,
            String name,
            String email
    ) {}


    public record LoginCommand(
            String username,
            String password
    ) {}

    public record ChangePasswordCommand(
            String currentPassword,
            String newPassword
    ) {}
}
