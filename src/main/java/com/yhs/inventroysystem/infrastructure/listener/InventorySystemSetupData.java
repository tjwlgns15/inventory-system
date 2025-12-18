package com.yhs.inventroysystem.infrastructure.listener;

import com.yhs.inventroysystem.domain.auth.entity.User;
import com.yhs.inventroysystem.domain.auth.repository.UserRepository;
import com.yhs.inventroysystem.domain.task.entity.TaskCategory;
import com.yhs.inventroysystem.domain.task.repository.TaskCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventorySystemSetupData implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskCategoryRepository taskCategoryRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }
        setupData();
        alreadySetup = true;
    }

    private void setupData() {
        createDefaultUser();
        createDefaultTaskCategory("수주/납품", "수주, 납품 관련 카테고리", "#EF6351", 1);
        createDefaultTaskCategory("제품 생산", "제품 생산 관련 카테고리", "#AA8D8", 2);
        createDefaultTaskCategory("기타", "기타 카테고리", "#43B581", 3);

        log.info("재고관리 초기 데이터 생성이 완료되었습니다.");
    }

    /**
     * 관리자 계정 생성
     */
    private void createDefaultUser() {
        Optional<User> existingUser = userRepository.findByUsername("solmitech");

        if (existingUser.isPresent()) {
            log.info("계정이 이미 존재합니다.");
            return;
        }

        User newUser = User.createNewUser(
                "solmitech",
                passwordEncoder.encode("solmi!300"),
                "솔미테크",
                "hello@solmitech.com"
        );

        userRepository.save(newUser);
        log.info("계정이 생성되었습니다. (username: {})", newUser.getUsername());
    }

    /**
     * 일정관리 카테고리 생성
     */
    private void createDefaultTaskCategory(String taskCategoryName, String description, String colorCode, Integer displayOrder) {
        if (taskCategoryRepository.existsByName(taskCategoryName)) {
            log.info("{} 카테고리가 이미 존재합니다.", taskCategoryName);
            return;
        }

        TaskCategory taskCategory = new TaskCategory(
                taskCategoryName,
                description,
                colorCode,
                displayOrder
        );

        taskCategoryRepository.save(taskCategory);
        log.info("일정 카테고리가 생성되었습니다. (categoryName: {})", taskCategoryName);
    }
}