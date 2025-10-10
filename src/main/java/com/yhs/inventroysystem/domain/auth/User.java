package com.yhs.inventroysystem.domain.auth;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,  unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Column(nullable = false)
    private Boolean enabled = true;

    private User(String username, String password, String name, String email, UserRole userRole) {
        validateUsername(username);
        validatePassword(password);
        validateName(name);

        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.userRole = userRole;
    }

    public static User createNewUser(String username, String encodedPassword, String name, String email) {
        return new User(username, encodedPassword, name, email, UserRole.USER);
    }
    public static User createAdmin(String username, String encodedPassword, String name, String email) {
        return new User(username, encodedPassword, name, email, UserRole.ADMIN);
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("아이디는 필수입니다");
        }
        if (username.length() < 4 || username.length() > 20) {
            throw new IllegalArgumentException("아이디는 4-20자 사이여야 합니다");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
    }

    // 비즈니스 메서드
    public void updatePassword(String newEncodedPassword) {
        validatePassword(newEncodedPassword);
        this.password = newEncodedPassword;
    }

    public void updateName(String newName) {
        validateName(newName);
        this.name = newName;
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }
}
