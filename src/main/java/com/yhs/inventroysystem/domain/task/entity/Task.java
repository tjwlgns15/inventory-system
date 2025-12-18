package com.yhs.inventroysystem.domain.task.entity;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_priority", columnList = "priority"),
        @Index(name = "idx_start_date", columnList = "startDate"),
        @Index(name = "idx_end_date", columnList = "endDate"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Slf4j
public class Task extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "author_name", nullable = false, length = 50)
    private String authorName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority = Priority.MEDIUM;

    @BatchSize(size = 200)
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)  // OneToMany는 기본이 Lazy loading: 트랜잭션 범위 밖(컨트롤러)에서 getCategoryMappings 호출 시 에러 발생
    private List<TaskCategoryMapping> categoryMappings = new ArrayList<>();


    public Task(String title, String description, String authorName, LocalDate startDate, LocalDate endDate, TaskStatus status, Priority priority) {
        this.title = title;
        this.description = description;
        this.authorName = authorName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.priority = priority;
    }

    public void updateTaskInfo(String title, String description, Priority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    public void updatePeriod(LocalDate startDate, LocalDate endDate) {
        validateDatePeriod(startDate, endDate);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateStatus(TaskStatus status) {
        this.status = status;
    }

    public void updatePriority(Priority priority) {
        this.priority = priority;
    }

    public void addCategory(TaskCategory category) {
        if (hasCategory(category)) {
            log.info("Task {} already has category {}. Skipping.", id, category.getId());
            return;
        }

        TaskCategoryMapping mapping = new TaskCategoryMapping(this, category);
        categoryMappings.add(mapping);
    }

    public void removeCategory(TaskCategory category) {
        categoryMappings.removeIf(mapping -> mapping.isMappedTo(category));
    }

    public void clearCategories() {
        categoryMappings.clear();
    }

    public boolean hasCategory(TaskCategory category) {
        return categoryMappings.stream()
                .anyMatch(mapping -> mapping.isMappedTo(category));
    }

    public List<TaskCategory> getCategories() {
        return categoryMappings.stream()
                .map(TaskCategoryMapping::getCategory)
                .collect(Collectors.toList());
    }

    public int getCategoryCount() {
        return categoryMappings.size();
    }

    public boolean isInProgress() {
        return this.status == TaskStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return this.status == TaskStatus.COMPLETED;
    }

    public boolean isOverdue() {
        return LocalDate.now().isAfter(endDate) && !isCompleted();
    }

    public boolean isHighPriority() {
        return this.priority == Priority.HIGH || this.priority == Priority.URGENT;
    }

    public boolean isUrgent() {
        return this.priority == Priority.URGENT;
    }

    public long getDurationDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    private void validateDatePeriod(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    String.format("종료일(%s)은 시작일(%s)보다 이후여야 합니다.",
                            endDate, startDate)
            );
        }
    }
}