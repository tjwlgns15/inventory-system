package com.yhs.inventroysystem.presentation.task;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.yhs.inventroysystem.domain.task.Priority;
import com.yhs.inventroysystem.domain.task.Task;
import com.yhs.inventroysystem.domain.task.TaskStatus;
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

    public record TaskCreateRequest(
            @NotBlank(message = "작업 제목은 필수입니다")
            String title,

            String description,

            @NotNull(message = "시작일은 필수입니다")
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,

            @NotNull(message = "종료일은 필수입니다")
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate endDate,

            TaskStatus status,

            @NotNull(message = "우선순위는 필수입니다")
            Priority priority
    ) {
        // 기본값 설정을 위한 생성자
        public TaskCreateRequest {
            if (status == null) {
                status = TaskStatus.TODO;
            }
            if (priority == null) {
                priority = Priority.MEDIUM;
            }
        }
    }

    public record TaskUpdateRequest(
            @NotBlank(message = "작업 제목은 필수입니다")
            @Size(max = 200, message = "작업 제목은 200자를 초과할 수 없습니다")
            String title,

            @Size(max = 1000, message = "작업 설명은 1000자를 초과할 수 없습니다")
            String description,

            @NotNull(message = "시작일은 필수입니다")
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,

            @NotNull(message = "종료일은 필수입니다")
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate endDate,

            @NotNull(message = "상태는 필수입니다")
            TaskStatus status,

            @NotNull(message = "우선순위는 필수입니다")
            Priority priority
    ) {}

    public record TaskResponse(
            Long id,
            String title,
            String description,
            String authorName,

            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,

            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate endDate,

            TaskStatus status,
            String statusDisplayName,

            Priority priority,
            String priorityDisplayName,
            String priorityColorCode,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime updatedAt,

            long durationDays,
            boolean overdue,
            boolean highPriority,
            boolean urgent
    ) {
        public static TaskResponse from(Task task) {
            return new TaskResponse(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getAuthorName(),
                    task.getStartDate(),
                    task.getEndDate(),
                    task.getStatus(),
                    task.getStatus().getDisplayName(),
                    task.getPriority(),
                    task.getPriority().getDisplayName(),
                    task.getPriority().getColorCode(),
                    task.getCreatedAt(),
                    task.getLastModifiedAt(),
                    task.getDurationDays(),
                    task.isOverdue(),
                    task.isHighPriority(),
                    task.isUrgent()
            );
        }
    }

    public record TaskListResponse(
            List<TaskResponse> tasks,
            long totalCount,
            int page,
            int size,
            boolean hasNext,
            boolean hasPrevious
    ) {}
}
