package com.yhs.inventroysystem.application.task;


import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.task.TaskCommands.TaskCreateCommand;
import com.yhs.inventroysystem.application.task.TaskCommands.TaskUpdateCommand;
import com.yhs.inventroysystem.domain.task.*;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public Task createTask(TaskCreateCommand command, CustomUserDetails currentUser) {
        validateDateRange(command.startDate(), command.endDate());

        Task task = new Task(
                command.title(),
                command.description(),
                currentUser.getName(),
                command.startDate(),
                command.endDate(),
                command.status(),
                command.priority()
        );

        Task savedTask = taskRepository.save(task);
        log.info("새 작업이 생성되었습니다. ID: {}, 제목: {}, 우선순위: {}",
                savedTask.getId(), savedTask.getTitle(), savedTask.getPriority());

        return savedTask;
    }

    public Task getTask(Long taskId) {
        return findTaskByIdWithCategories(taskId);
    }

    /**
     * 페이징 조회 - 카테고리 정보 포함 (Fetch Join)
     */
    public Page<Task> getAllTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findAllWithCategories(pageable);
    }

    /**
     * 상태별 조회 - 카테고리 정보 포함 (Fetch Join)
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatusWithCategories(status);
    }

    /**
     * 우선순위별 조회 - 카테고리 정보 포함 (Fetch Join)
     */
    public List<Task> getTasksByPriority(Priority priority) {
        return taskRepository.findByPriorityWithCategories(priority);
    }

    /**
     * 높은 우선순위 작업 조회 - 카테고리 정보 포함 (Fetch Join)
     */
    public List<Task> getHighPriorityTasks() {
        List<Priority> highPriorities = Arrays.asList(Priority.HIGH, Priority.URGENT);
        return taskRepository.findHighPriorityTasksWithCategories(highPriorities, TaskStatus.COMPLETED);
    }

    public List<Task> searchTasksByTitle(String title) {
        return taskRepository.findByTitleContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(title);
    }

    public List<Task> searchTasksByAuthor(String authorName) {
        return taskRepository.findByAuthorNameContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(authorName);
    }

    /**
     * 지연된 작업 조회 - 카테고리 정보 포함 (Fetch Join)
     */
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasksWithCategories(LocalDate.now(), TaskStatus.COMPLETED);
    }

    /**
     * 복합 검색 - 카테고리 정보 포함 (Fetch Join)
     */
    public Page<Task> searchTasks(String title, String authorName, List<TaskStatus> statusList,
                                  Priority priority, LocalDate startDate, LocalDate endDate,
                                  int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findTasksWithFiltersAndCategories(
                title, authorName, statusList, priority, startDate, endDate, pageable);
    }

    @Transactional
    public Task updateTask(Long taskId, TaskUpdateCommand command) {
        validateDateRange(command.startDate(), command.endDate());

        Task task = findTaskByIdWithCategories(taskId);

        task.updateTaskInfo(command.title(), command.description(), command.priority());
        task.updatePeriod(command.startDate(), command.endDate());
        task.updateStatus(command.status());

        log.info("작업이 수정되었습니다. ID: {}, 제목: {}, 우선순위: {}",
                task.getId(), task.getTitle(), task.getPriority());

        return task;
    }

    @Transactional
    public Task updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = findTaskByIdWithCategories(taskId);
        task.updateStatus(status);

        log.info("작업 상태가 변경되었습니다. ID: {}, 상태: {}", task.getId(), status);

        return task;
    }

    @Transactional
    public Task updateTaskPriority(Long taskId, Priority priority) {
        Task task = findTaskByIdWithCategories(taskId);
        task.updatePriority(priority);

        log.info("작업 우선순위가 변경되었습니다. ID: {}, 우선순위: {}", task.getId(), priority);

        return task;
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = findTaskById(taskId);
        taskRepository.delete(task);

        log.info("작업이 삭제되었습니다. ID: {}, 제목: {}", task.getId(), task.getTitle());
    }

    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        Map<TaskStatus, Long> statusStats = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            statusStats.put(status, taskRepository.countByStatus(status));
        }

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
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("작업을 찾을 수 없습니다. [id: %d]", taskId)));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    String.format("종료일(%s)은 시작일(%s)보다 이후여야 합니다.",
                            endDate, startDate)
            );
        }
    }


    private Task findTaskByIdWithCategories(Long taskId) {
        return taskRepository.findByIdWithCategories(taskId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("작업을 찾을 수 없습니다. [id: %d]", taskId)));
    }
}