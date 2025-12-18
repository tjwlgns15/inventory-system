package com.yhs.inventroysystem.application.task;


import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.task.TaskCommands.TaskCreateCommand;
import com.yhs.inventroysystem.application.task.TaskCommands.TaskUpdateCommand;
import com.yhs.inventroysystem.domain.task.entity.Priority;
import com.yhs.inventroysystem.domain.task.entity.Task;
import com.yhs.inventroysystem.domain.task.entity.TaskCategory;
import com.yhs.inventroysystem.domain.task.entity.TaskStatus;
import com.yhs.inventroysystem.domain.task.service.TaskDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskService {

    private final TaskDomainService taskDomainService;

    @Transactional
    public Task createTask(TaskCreateCommand command, CustomUserDetails currentUser) {

        Task savedTask = taskDomainService.createTask(
                command.title(),
                command.description(),
                currentUser.getName(),
                command.startDate(),
                command.endDate(),
                command.status(),
                command.priority()
        );

        log.info("새 작업이 생성되었습니다. ID: {}, 제목: {}, 우선순위: {}",
                savedTask.getId(), savedTask.getTitle(), savedTask.getPriority());

        return savedTask;
    }

    public Task getTask(Long taskId) {
        return taskDomainService.findTaskByIdWithCategories(taskId);
    }

    /**
     * 페이징 조회 - 카테고리 정보 포함 (Fetch Join)
     */
    public Page<Task> getAllTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskDomainService.findCategoriesWithCategories(pageable);

        tasks.getContent().forEach(task ->
                task.getCategories().forEach(TaskCategory::getName)  // TaskCategory 필드 접근으로 강제 로딩
        );

        return tasks;
    }

    /**
     * 상태별 조회 - 카테고리 정보 포함 (Fetch Join)
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskDomainService.findByStatusWithCategories(status);
    }

    /**
     * 우선순위별 조회 - 카테고리 정보 포함 (Fetch Join)
     */
    public List<Task> getTasksByPriority(Priority priority) {
        return taskDomainService.findByPriorityWithCategories(priority);
    }

    /**
     * 높은 우선순위 작업 조회 - 카테고리 정보 포함 (Fetch Join)
     */
    public List<Task> getHighPriorityTasks() {
        return taskDomainService.findHighPriorityTasksWithCategories();
    }

    public List<Task> searchTasksByTitle(String title) {
        return taskDomainService.findByTitleContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(title);
    }

    public List<Task> searchTasksByAuthor(String authorName) {
        return taskDomainService.findByAuthorNameContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(authorName);
    }

    /**
     * 지연된 작업 조회 - 카테고리 정보 포함 (Fetch Join)
     */
    public List<Task> getOverdueTasks() {
        return taskDomainService.findOverdueTasksWithCategories(LocalDate.now(), TaskStatus.COMPLETED);
    }

    /**
     * 복합 검색 - 카테고리 정보 포함 (Fetch Join)
     */
    public Page<Task> searchTasks(String title, String authorName, List<TaskStatus> statusList,
                                  Priority priority, LocalDate startDate, LocalDate endDate,
                                  int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Task> tasks = taskDomainService.findTasksWithFiltersAndCategories(
                title, authorName, statusList, priority, startDate, endDate, pageable);

        // 트랜잭션 안에서 카테고리 강제 로딩
        tasks.getContent().forEach(task ->
                task.getCategories().forEach(TaskCategory::getName)  // TaskCategory 필드 접근으로 강제 로딩
        );

        return tasks;
    }

    @Transactional
    public Task updateTask(Long taskId, TaskUpdateCommand command) {
        taskDomainService.validateDateRange(command.startDate(), command.endDate());

        Task task = taskDomainService.findTaskByIdWithCategories(taskId);

        task.updateTaskInfo(command.title(), command.description(), command.priority());
        task.updatePeriod(command.startDate(), command.endDate());
        task.updateStatus(command.status());

        log.info("작업이 수정되었습니다. ID: {}, 제목: {}, 우선순위: {}",
                task.getId(), task.getTitle(), task.getPriority());

        return task;
    }

    @Transactional
    public Task updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskDomainService.findTaskByIdWithCategories(taskId);
        task.updateStatus(status);

        log.info("작업 상태가 변경되었습니다. ID: {}, 상태: {}", task.getId(), status);

        return task;
    }

    @Transactional
    public Task updateTaskPriority(Long taskId, Priority priority) {
        Task task = taskDomainService.findTaskByIdWithCategories(taskId);
        task.updatePriority(priority);

        log.info("작업 우선순위가 변경되었습니다. ID: {}, 우선순위: {}", task.getId(), priority);

        return task;
    }

    @Transactional
    public void deleteTask(Long taskId) {
        String title = taskDomainService.findTaskById(taskId).getTitle();

        taskDomainService.deleteTask(taskId);

        log.info("작업이 삭제되었습니다. ID: {}, 제목: {}", taskId, title);
    }

    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        Map<TaskStatus, Long> statusStats = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            Long count = taskDomainService.countByStatus(status);
            statusStats.put(status, count);
        }

        Map<Priority, Long> priorityStats = new HashMap<>();
        for (Priority priority : Priority.values()) {
            Long count = taskDomainService.countByPriority(priority);
            priorityStats.put(priority, count);
        }

        statistics.put("status", statusStats);
        statistics.put("priority", priorityStats);

        return statistics;
    }

}