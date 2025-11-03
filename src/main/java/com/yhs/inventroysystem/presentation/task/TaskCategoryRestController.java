package com.yhs.inventroysystem.presentation.task;


import com.yhs.inventroysystem.application.task.TaskCategoryCommands.TaskCategoryCreateCommand;
import com.yhs.inventroysystem.application.task.TaskCategoryCommands.TaskCategoryUpdateCommand;
import com.yhs.inventroysystem.application.task.TaskCategoryService;
import com.yhs.inventroysystem.domain.task.TaskCategory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.yhs.inventroysystem.application.task.TaskCategoryCommands.TaskCategoryDisplayOrderUpdateCommand;
import static com.yhs.inventroysystem.presentation.task.TaskCategoryDto.*;

@RestController
@RequestMapping("/api/task-categories")
@RequiredArgsConstructor
@Validated
public class TaskCategoryRestController {

    private final TaskCategoryService taskCategoryService;

    @PostMapping
    public ResponseEntity<TaskCategoryResponse> createTaskCategory(@Valid @RequestBody TaskCategoryCreateRequest request) {

        TaskCategoryCreateCommand command = new TaskCategoryCreateCommand(
                request.name(),
                request.description(),
                request.colorCode(),
                request.displayOrder()
        );

        TaskCategory taskCategory = taskCategoryService.createTaskCategory(command);
        long categoryTaskCount = taskCategoryService.getCategoryTaskCount(taskCategory.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(TaskCategoryResponse.from(taskCategory, categoryTaskCount));
    }

    @GetMapping
    public ResponseEntity<TaskCategoryListResponse> getAllTaskCategories() {
        List<TaskCategory> categories = taskCategoryService.getAllCategories();

        List<TaskCategoryResponse> responses = categories.stream()
                .map(category -> TaskCategoryResponse.from(
                        category,
                        taskCategoryService.getCategoryTaskCount(category.getId())
                ))
                .toList();

        TaskCategoryListResponse taskCategoryListResponse = new TaskCategoryListResponse(
                responses,
                categories.size()
        );

        return ResponseEntity.ok(taskCategoryListResponse);
    }

    @GetMapping("/{taskCategoryId}")
    public ResponseEntity<TaskCategoryResponse> getTaskCategory(@PathVariable Long taskCategoryId) {
        TaskCategory taskCategory = taskCategoryService.getCategory(taskCategoryId);
        long taskCount = taskCategoryService.getCategoryTaskCount(taskCategoryId);

        return ResponseEntity.ok(TaskCategoryResponse.from(taskCategory, taskCount));
    }

    @GetMapping("/active")
    public ResponseEntity<List<TaskCategorySimpleResponse>> getActiveCategories() {
        List<TaskCategory> categories = taskCategoryService.getActiveCategories();

        List<TaskCategorySimpleResponse> response = categories.stream()
                .map(TaskCategorySimpleResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<TaskCategoryListResponse> searchCategories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive) {

        List<TaskCategory> categories = taskCategoryService.searchCategories(name, isActive);

        List<TaskCategoryResponse> categoryResponses = categories.stream()
                .map(category -> TaskCategoryResponse.from(
                        category,
                        taskCategoryService.getCategoryTaskCount(category.getId())
                ))
                .collect(Collectors.toList());

        TaskCategoryListResponse response = new TaskCategoryListResponse(
                categoryResponses,
                categories.size()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskCategoryId}")
    public ResponseEntity<TaskCategoryResponse> updateCategory(
            @PathVariable Long taskCategoryId,
            @Valid @RequestBody TaskCategoryUpdateRequest request) {

        TaskCategoryUpdateCommand command = new TaskCategoryUpdateCommand(
                request.name(),
                request.description(),
                request.colorCode(),
                request.displayOrder()
        );

        TaskCategory category = taskCategoryService.updateCategory(taskCategoryId, command);
        long taskCount = taskCategoryService.getCategoryTaskCount(taskCategoryId);

        return ResponseEntity.ok(TaskCategoryResponse.from(category, taskCount));
    }

    @PatchMapping("/{taskCategoryId}/display-order")
    public ResponseEntity<TaskCategoryResponse> updateDisplayOrder(
            @PathVariable Long taskCategoryId,
            @RequestParam Integer displayOrder) {

        TaskCategory category = taskCategoryService.updateDisplayOrder(taskCategoryId, displayOrder);
        long categoryTaskCount = taskCategoryService.getCategoryTaskCount(taskCategoryId);

        return ResponseEntity.ok(TaskCategoryResponse.from(category, categoryTaskCount));
    }

    @PatchMapping("/display-orders")
    public ResponseEntity<Void> updateDisplayOrders(@Valid @RequestBody TaskCategoryDisplayOrderBatchUpdateRequest request) {

        List<TaskCategoryDisplayOrderUpdateCommand> commands = request.updates().stream()
                .map(update -> new TaskCategoryDisplayOrderUpdateCommand(
                        update.taskCategoryId(),
                        update.displayOrder()
                ))
                .collect(Collectors.toList());

        taskCategoryService.updateDisplayOrders(commands);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{taskCategoryId}/activate")
    public ResponseEntity<TaskCategoryResponse> activateCategory(@PathVariable Long taskCategoryId) {

        TaskCategory category = taskCategoryService.activateCategory(taskCategoryId);
        long categoryTaskCount = taskCategoryService.getCategoryTaskCount(taskCategoryId);

        return ResponseEntity.ok(TaskCategoryResponse.from(category, categoryTaskCount));
    }

    @PatchMapping("/{taskCategoryId}/deactivate")
    public ResponseEntity<TaskCategoryResponse> deactivateCategory(@PathVariable Long taskCategoryId) {

        TaskCategory category = taskCategoryService.deactivateCategory(taskCategoryId);
        long categoryTaskCount = taskCategoryService.getCategoryTaskCount(taskCategoryId);

        return ResponseEntity.ok(TaskCategoryResponse.from(category, categoryTaskCount));
    }

    @DeleteMapping("/{taskCategoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long taskCategoryId) {

        taskCategoryService.deleteCategory(taskCategoryId);
        return ResponseEntity.ok().build();
    }
}