package com.yhs.inventroysystem.controller;


import com.yhs.inventroysystem.dto.TaskDto;
import com.yhs.inventroysystem.dto.TaskDto.TaskListResponse;
import com.yhs.inventroysystem.dto.TaskDto.TaskResponse;
import com.yhs.inventroysystem.entity.enumerate.Priority;
import com.yhs.inventroysystem.entity.enumerate.TaskStatus;
import com.yhs.inventroysystem.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Validated  // @Min, @Max 등의 유효성 검사를 위해 필요
public class TaskRestController {

    private final TaskService taskService;

    /**
     * 작업 생성
     * POST /api/tasks
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskDto.TaskCreateRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 작업 단건 조회
     * GET /api/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable @Min(1) Long taskId) {
        TaskResponse response = taskService.getTask(taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 작업 목록 조회 (페이지네이션 포함)
     * GET /api/tasks?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<TaskListResponse> getAllTasks(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                        @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int size) {
        TaskListResponse response = taskService.getAllTasks(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 상태별 작업 조회
     * GET /api/tasks/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(@PathVariable TaskStatus status) {
        List<TaskResponse> response = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(response);
    }

    /**
     * 우선순위별 작업 조회
     * GET /api/tasks/priority/{priority}
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TaskResponse>> getTasksByPriority(@PathVariable Priority priority) {
        List<TaskResponse> response = taskService.getTasksByPriority(priority);
        return ResponseEntity.ok(response);
    }

    /**
     * 높은 우선순위 작업만 조회
     * GET /api/tasks/high-priority
     */
    @GetMapping("/high-priority")
    public ResponseEntity<List<TaskResponse>> getHighPriorityTasks() {
        List<TaskResponse> response = taskService.getHighPriorityTasks();
        return ResponseEntity.ok(response);
    }

    /**
     * 작업 우선순위 수정
     * PATCH /api/tasks/{taskId}/priority?priority=HIGH
     */
    @PatchMapping("/{taskId}/priority")
    public ResponseEntity<TaskResponse> updateTaskPriority(@PathVariable @Min(1) Long taskId,
                                                           @RequestParam Priority priority) {
        TaskResponse response = taskService.updateTaskPriority(taskId, priority);
        return ResponseEntity.ok(response);
    }

    /**
     * 조건 검색 (제목, 작성자, 상태, 우선순위, 날짜 범위)
     * GET /api/tasks/search?title=...&authorName=...&startDate=yyyy-MM-dd
     */
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

        TaskListResponse response = taskService.searchTasks(
                title, authorName, status, priority, startDate, endDate, page, size
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 마감일이 지난 작업 목록 조회
     * GET /api/tasks/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> getOverdueTasks() {
        List<TaskResponse> response = taskService.getOverdueTasks();
        return ResponseEntity.ok(response);
    }

    /**
     * 작업 통계 정보 조회
     * GET /api/tasks/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTaskStatistics() {
        Map<String, Object> response = taskService.getTaskStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * 작업 전체 수정
     * PUT /api/tasks/{taskId}
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable @Min(1) Long taskId,
                                                   @Valid @RequestBody TaskDto.TaskUpdateRequest request) {
        TaskResponse response = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 작업 상태 변경
     * PATCH /api/tasks/{taskId}/status?status=COMPLETE
     */
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable @Min(1) Long taskId,
                                                         @RequestParam TaskStatus status) {
        TaskResponse response = taskService.updateTaskStatus(taskId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * 작업 삭제
     * DELETE /api/tasks/{taskId}
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable @Min(1) Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(null);
    }
}

