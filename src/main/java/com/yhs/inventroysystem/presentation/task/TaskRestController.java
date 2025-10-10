package com.yhs.inventroysystem.presentation.task;


import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.task.TaskCommands;
import com.yhs.inventroysystem.application.task.TaskCommands.*;
import com.yhs.inventroysystem.domain.task.Priority;
import com.yhs.inventroysystem.domain.task.Task;
import com.yhs.inventroysystem.domain.task.TaskStatus;
import com.yhs.inventroysystem.application.task.TaskService;
import com.yhs.inventroysystem.presentation.task.TaskDto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Validated  // @Min, @Max 등의 유효성 검사를 위해 필요
public class TaskRestController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        TaskCreateCommand command = new TaskCreateCommand(
                request.title(),
                request.description(),
                request.startDate(),
                request.endDate(),
                request.status(),
                request.priority()
        );

        Task task = taskService.createTask(command, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskResponse.from(task));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable @Min(1) Long taskId) {
        Task task = taskService.getTask(taskId);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @GetMapping
    public ResponseEntity<TaskListResponse> getAllTasks(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int size) {

        Page<Task> taskPage = taskService.getAllTasks(page, size);

        List<TaskResponse> taskResponses = taskPage.getContent().stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());

        TaskListResponse response = new TaskListResponse(
                taskResponses,
                taskPage.getTotalElements(),
                page,
                size,
                taskPage.hasNext(),
                taskPage.hasPrevious()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(@PathVariable TaskStatus status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        List<TaskResponse> response = tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TaskResponse>> getTasksByPriority(@PathVariable Priority priority) {
        List<Task> tasks = taskService.getTasksByPriority(priority);
        List<TaskResponse> response = tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/high-priority")
    public ResponseEntity<List<TaskResponse>> getHighPriorityTasks() {
        List<Task> tasks = taskService.getHighPriorityTasks();
        List<TaskResponse> response = tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/priority")
    public ResponseEntity<TaskResponse> updateTaskPriority(
            @PathVariable @Min(1) Long taskId,
            @RequestParam Priority priority) {
        Task task = taskService.updateTaskPriority(taskId, priority);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @GetMapping("/search")
    public ResponseEntity<TaskListResponse> searchTasks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) List<TaskStatus> status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        Page<Task> taskPage = taskService.searchTasks(
                title, authorName, status, priority, startDate, endDate, page, size
        );

        List<TaskResponse> taskResponses = taskPage.getContent().stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());

        TaskListResponse response = new TaskListResponse(
                taskResponses,
                taskPage.getTotalElements(),
                page,
                size,
                taskPage.hasNext(),
                taskPage.hasPrevious()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> getOverdueTasks() {
        List<Task> tasks = taskService.getOverdueTasks();
        List<TaskResponse> response = tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTaskStatistics() {
        Map<String, Object> response = taskService.getTaskStatistics();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable @Min(1) Long taskId,
            @Valid @RequestBody TaskUpdateRequest request) {

        TaskUpdateCommand command = new TaskUpdateCommand(
                request.title(),
                request.description(),
                request.startDate(),
                request.endDate(),
                request.status(),
                request.priority()
        );

        Task task = taskService.updateTask(taskId, command);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable @Min(1) Long taskId,
            @RequestParam TaskStatus status) {
        Task task = taskService.updateTaskStatus(taskId, status);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable @Min(1) Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok().build();
    }
}

