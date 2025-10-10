package com.yhs.inventroysystem.presentation.auth;

import com.yhs.inventroysystem.application.auth.UserService;
import com.yhs.inventroysystem.domain.auth.User;
import com.yhs.inventroysystem.presentation.auth.UserDtos.UserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.yhs.inventroysystem.application.auth.UserCommands.*;
import static com.yhs.inventroysystem.presentation.auth.UserDtos.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private static final String JWT_COOKIE_NAME = "JWT_TOKEN";
    private static final int COOKIE_MAX_AGE = 24 * 60 * 60;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupCommand command = new SignupCommand(
                request.username(),
                request.password(),
                request.name(),
                request.email()
        );

        User newUser = userService.signup(command);
        return ResponseEntity.ok(UserResponse.from(newUser));
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        LoginCommand command = new LoginCommand(
                request.username(),
                request.password()
        );

        String token = userService.login(command);

        Cookie jwtCookie = createJwtCookie(token);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie jwtCookie = createExpiredJwtCookie();
        response.addCookie(jwtCookie);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestAttribute("username") String username) {
        User user = userService.findUserByUsername(username);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        User user = userService.findUserByUsername(userDetails.getUsername());

        ChangePasswordCommand command = new ChangePasswordCommand(
                request.currentPassword(),
                request.newPassword()
        );

        userService.changePassword(user.getId(), command.currentPassword(), command.newPassword());

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{userId}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable Long userId) {
        userService.disableUser(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{userId}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable Long userId) {
        userService.enableUser(userId);
        return ResponseEntity.ok().build();
    }

    private Cookie createJwtCookie(String token) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 개발환경: false, 운영환경: true (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    private Cookie createExpiredJwtCookie() {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
