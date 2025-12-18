package com.yhs.inventroysystem.domain.auth.service;

import com.yhs.inventroysystem.domain.auth.repository.UserRepository;
import com.yhs.inventroysystem.domain.auth.entity.User;
import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserDomainService {

    private final UserRepository userRepository;

    @Transactional
    public User saveUser(User newUser) {
        return userRepository.save(newUser);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.user(username));
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));
    }

    public void validateDuplicateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw DuplicateResourceException.username(username);
        }
    }

    public void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw DuplicateResourceException.email(email);
        }
    }
}
