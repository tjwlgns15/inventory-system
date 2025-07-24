package com.yhs.inventroysystem.service;


import com.yhs.inventroysystem.dto.TaskDto.TaskCreateRequest;
import com.yhs.inventroysystem.dto.TaskDto.TaskListResponse;
import com.yhs.inventroysystem.dto.TaskDto.TaskResponse;
import com.yhs.inventroysystem.dto.TaskDto.TaskUpdateRequest;
import com.yhs.inventroysystem.entity.enumerate.Priority;
import com.yhs.inventroysystem.entity.Task;
import com.yhs.inventroysystem.entity.enumerate.TaskStatus;
import com.yhs.inventroysystem.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        validateDateRange(request.getStartDate(), request.getEndDate());

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .authorName(request.getAuthorName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .priority(request.getPriority())
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("새 작업이 생성되었습니다. ID: {}, 제목: {}, 우선순위: {}",
                savedTask.getId(), savedTask.getTitle(), savedTask.getPriority());

        return TaskResponse.from(savedTask);
    }

    public TaskResponse getTask(Long taskId) {
        Task task = findTaskById(taskId);
        return TaskResponse.from(task);
    }

    public TaskListResponse getAllTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> taskPage = taskRepository.findAllByOrderByPriorityDescCreatedAtDesc(pageable);

        List<TaskResponse> taskResponses = taskPage.getContent().stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());

        return TaskListResponse.builder()
                .tasks(taskResponses)
                .totalCount(taskPage.getTotalElements())
                .page(page)
                .size(size)
                .hasNext(taskPage.hasNext())
                .hasPrevious(taskPage.hasPrevious())
                .build();
    }

    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        List<Task> tasks = taskRepository.findByStatusOrderByPriorityDescCreatedAtDesc(status);
        return tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByPriority(Priority priority) {
        List<Task> tasks = taskRepository.findByPriorityOrderByCreatedAtDesc(priority);
        return tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getHighPriorityTasks() {
        List<Priority> highPriorities = Arrays.asList(Priority.HIGH, Priority.URGENT);
        List<Task> tasks = taskRepository.findHighPriorityTasks(highPriorities, TaskStatus.COMPLETED);
        return tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> searchTasksByTitle(String title) {
        List<Task> tasks = taskRepository.findByTitleContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(title);
        return tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> searchTasksByAuthor(String authorName) {
        List<Task> tasks = taskRepository.findByAuthorNameContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(authorName);
        return tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getOverdueTasks() {
        List<Task> tasks = taskRepository.findOverdueTasks(LocalDate.now(), TaskStatus.COMPLETED);
        return tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    public TaskListResponse searchTasks(String title, String authorName, List<TaskStatus> statusList, Priority priority,
                                        LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> taskPage = taskRepository.findTasksWithFilters(
                title, authorName, statusList, priority, startDate, endDate, pageable);

        List<TaskResponse> taskResponses = taskPage.getContent().stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());

        return TaskListResponse.builder()
                .tasks(taskResponses)
                .totalCount(taskPage.getTotalElements())
                .page(page)
                .size(size)
                .hasNext(taskPage.hasNext())
                .hasPrevious(taskPage.hasPrevious())
                .build();
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request) {
        validateDateRange(request.getStartDate(), request.getEndDate());

        Task task = findTaskById(taskId);

        task.updateTaskInfo(request.getTitle(), request.getDescription(), request.getPriority());
        task.updatePeriod(request.getStartDate(), request.getEndDate());
        task.updateStatus(request.getStatus());

        log.info("작업이 수정되었습니다. ID: {}, 제목: {}, 우선순위: {}",
                task.getId(), task.getTitle(), task.getPriority());

        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = findTaskById(taskId);
        task.updateStatus(status);

        log.info("작업 상태가 변경되었습니다. ID: {}, 상태: {}", task.getId(), status);

        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse updateTaskPriority(Long taskId, Priority priority) {
        Task task = findTaskById(taskId);
        task.updatePriority(priority);

        log.info("작업 우선순위가 변경되었습니다. ID: {}, 우선순위: {}", task.getId(), priority);

        return TaskResponse.from(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = findTaskById(taskId);
        taskRepository.delete(task);

        log.info("작업이 삭제되었습니다. ID: {}, 제목: {}", task.getId(), task.getTitle());
    }

    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 상태별 통계
        Map<TaskStatus, Long> statusStats = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            statusStats.put(status, taskRepository.countByStatus(status));
        }

        // 우선순위별 통계
        Map<Priority, Long> priorityStats = new HashMap<>();
        for (Priority priority : Priority.values()) {
            priorityStats.put(priority, taskRepository.countByPriority(priority));
        }

        statistics.put("status", statusStats);
        statistics.put("priority", priorityStats);

        return statistics;
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("작업을 찾을 수 없습니다. [id: %d]", taskId)));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    String.format("종료일(%s)은 시작일(%s)보다 이후여야 합니다.",
                            endDate, startDate)
            );
        }
    }
}