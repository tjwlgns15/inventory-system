package com.yhs.inventroysystem.application.auth;

import com.yhs.inventroysystem.application.auth.UserCommands.LoginCommand;
import com.yhs.inventroysystem.application.auth.UserCommands.SignupCommand;
import com.yhs.inventroysystem.domain.auth.User;
import com.yhs.inventroysystem.domain.auth.UserRepository;
import com.yhs.inventroysystem.domain.exception.DuplicateEmailException;
import com.yhs.inventroysystem.domain.exception.DuplicateUsernameException;
import com.yhs.inventroysystem.domain.exception.InvalidPasswordException;
import com.yhs.inventroysystem.domain.exception.UserNotFoundException;
import com.yhs.inventroysystem.infrastructure.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public User signup(SignupCommand signupCommand) {
        log.info("회원가입 요청: username={}", signupCommand.username());

        validateDuplicateUsername(signupCommand.username());
        validateDuplicateEmail(signupCommand.email());

        User newUser = createNewUser(signupCommand);
        User savedUser = userRepository.save(newUser);

        log.info("회원가입 완료: username={}", savedUser.getUsername());
        return  savedUser;
    }

    public String login(LoginCommand command) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        command.username(),
                        command.password()
                )
        );

        return jwtTokenProvider.generateToken(authentication);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedNewPassword);
    }

    @Transactional
    public void disableUser(Long userId) {
        User user = findUserById(userId);
        user.disable();
    }

    @Transactional
    public void enableUser(Long userId) {
        User user = findUserById(userId);
        user.enable();
    }

    private void validateDuplicateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException(username);
        }
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    private User createNewUser(SignupCommand request) {
        String encodedPassword = encodePassword(request.password());
        return User.createNewUser(
                request.username(),
                encodedPassword,
                request.name(),
                request.email()
        );
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
