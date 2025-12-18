package com.yhs.inventroysystem.presentation.task;


import com.yhs.inventroysystem.domain.task.entity.TaskCategory;
import jakarta.validation.constraints.*;

import java.util.List;

public class TaskCategoryDto {

    public record TaskCategoryCreateRequest(
            @NotBlank(message = "카테고리 이름은 필수입니다")
            @Size(max = 50, message = "카테고리 이름은 50자를 초과할 수 없습니다")
            String name,

            @Size(max = 200, message = "설명은 200자를 초과할 수 없습니다")
            String description,

            @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상 코드는 #RRGGBB 형식이어야 합니다")
            String colorCode,

            @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다")
            Integer displayOrder
    ) {
        public TaskCategoryCreateRequest {
            if (displayOrder == null) {
                displayOrder = 0;
            }
        }
    }

    public record TaskCategoryUpdateRequest(
            @NotBlank(message = "카테고리 이름은 필수입니다")
            @Size(max = 50, message = "카테고리 이름은 50자를 초과할 수 없습니다")
            String name,

            @Size(max = 200, message = "설명은 200자를 초과할 수 없습니다")
            String description,

            @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상 코드는 #RRGGBB 형식이어야 합니다")
            String colorCode,

            @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다")
            Integer displayOrder
    ) {}

    public record TaskCategoryDisplayOrderUpdateRequest(
            @NotNull(message = "카테고리 ID는 필수입니다")
            @Min(value = 1, message = "카테고리 ID는 1 이상이어야 합니다")
            Long taskCategoryId,

            @NotNull(message = "표시 순서는 필수입니다")
            @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다")
            Integer displayOrder
    ) {}

    public record TaskCategoryResponse(
            Long id,
            String name,
            String description,
            String colorCode,
            Integer displayOrder,
            Boolean isActive,
            long taskCount
    ) {
        public static TaskCategoryResponse from(TaskCategory category, long taskCount) {
            return new TaskCategoryResponse(
                    category.getId(),
                    category.getName(),
                    category.getDescription(),
                    category.getColorCode(),
                    category.getDisplayOrder(),
                    category.isActive(),
                    taskCount
            );
        }
    }

    public record TaskCategorySimpleResponse(
            Long id,
            String name,
            String colorCode,
            Boolean isActive
    ) {
        public static TaskCategorySimpleResponse from(TaskCategory category) {
            return new TaskCategorySimpleResponse(
                    category.getId(),
                    category.getName(),
                    category.getColorCode(),
                    category.isActive()
            );
        }
    }

    public record TaskCategoryListResponse(
            List<TaskCategoryResponse> categories,
            long totalCount
    ) {}

    public record TaskCategoryDisplayOrderBatchUpdateRequest(
            @NotEmpty(message = "업데이트할 카테고리 목록이 비어있습니다")
            List<TaskCategoryDisplayOrderUpdateRequest> updates
    ) {}
}
