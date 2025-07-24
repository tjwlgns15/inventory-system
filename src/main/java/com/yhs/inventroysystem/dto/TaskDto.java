package com.yhs.inventroysystem.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.yhs.inventroysystem.entity.enumerate.Priority;
import com.yhs.inventroysystem.entity.Task;
import com.yhs.inventroysystem.entity.enumerate.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TaskDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskCreateRequest {
        @NotBlank(message = "작업 제목은 필수입니다")
        private String title;

        private String description;

        @NotBlank(message = "작성자 이름은 필수입니다")
        private String authorName;

        @NotNull(message = "시작일은 필수입니다")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @NotNull(message = "종료일은 필수입니다")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        @Builder.Default
        private TaskStatus status = TaskStatus.TODO;

        @NotNull(message = "우선순위는 필수입니다")
        @Builder.Default
        private Priority priority = Priority.MEDIUM;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskUpdateRequest {
        @NotBlank(message = "작업 제목은 필수입니다")
        @Size(max = 200, message = "작업 제목은 200자를 초과할 수 없습니다")
        private String title;

        @Size(max = 1000, message = "작업 설명은 1000자를 초과할 수 없습니다")
        private String description;

        @NotNull(message = "시작일은 필수입니다")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @NotNull(message = "종료일은 필수입니다")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        @NotNull(message = "상태는 필수입니다")
        private TaskStatus status;

        @NotNull(message = "우선순위는 필수입니다")
        private Priority priority;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TaskResponse {
        private Long id;
        private String title;
        private String description;
        private String authorName;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        private TaskStatus status;
        private String statusDisplayName;

        private Priority priority;
        private String priorityDisplayName;
        private String priorityColorCode;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

        private long durationDays;
        private boolean overdue;
        private boolean highPriority;
        private boolean urgent;

        public static TaskResponse from(Task task) {
            return TaskResponse.builder()
                    .id(task.getId())
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .authorName(task.getAuthorName())
                    .startDate(task.getStartDate())
                    .endDate(task.getEndDate())
                    .status(task.getStatus())
                    .statusDisplayName(task.getStatus().getDisplayName())
                    .priority(task.getPriority())
                    .priorityDisplayName(task.getPriority().getDisplayName())
                    .priorityColorCode(task.getPriority().getColorCode())
                    .createdAt(task.getCreatedAt())
                    .updatedAt(task.getLastModifiedAt())
                    .durationDays(task.getDurationDays())
                    .overdue(task.isOverdue())
                    .highPriority(task.isHighPriority())
                    .urgent(task.isUrgent())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TaskListResponse {
        private List<TaskResponse> tasks;
        private long totalCount;
        private int page;
        private int size;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}
