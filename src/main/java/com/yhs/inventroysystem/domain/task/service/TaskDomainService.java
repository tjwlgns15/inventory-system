package com.yhs.inventroysystem.domain.task.service;


import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.task.TaskCommands.TaskCreateCommand;
import com.yhs.inventroysystem.application.task.TaskCommands.TaskUpdateCommand;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.task.entity.Priority;
import com.yhs.inventroysystem.domain.task.entity.Task;
import com.yhs.inventroysystem.domain.task.entity.TaskCategory;
import com.yhs.inventroysystem.domain.task.entity.TaskStatus;
import com.yhs.inventroysystem.domain.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskDomainService {

    private final TaskRepository taskRepository;


    @Transactional
    public Task createTask(String title, String description,
                           String name, LocalDate startDate,
                           LocalDate endDate, TaskStatus status,
                           Priority priority) {

        validateDateRange(startDate, endDate);

        Task task = new Task(
                title,
                description,
                name,
                startDate,
                endDate,
                status,
                priority
        );
        return saveTask(task);
    }
    @Transactional
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    public Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> ResourceNotFoundException.task(taskId));
    }

    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }
    public Task findTaskByIdWithCategories(Long taskId) {
        return taskRepository.findByIdWithCategories(taskId)
                .orElseThrow(() -> ResourceNotFoundException.task(taskId));
    }

    public Page<Task> findCategoriesWithCategories(Pageable pageable) {
        return taskRepository.findCategoriesWithCategories(pageable);
    }

    public List<Task> findByStatusWithCategories(TaskStatus status) {
        return taskRepository.findByStatusWithCategories(status);
    }
    public List<Task> findTasksByIdsWithCategories(List<Long> categoryIds) {
        return taskRepository.findTasksByIdsWithCategories(categoryIds);
    }

    public List<Task> findByPriorityWithCategories(Priority priority) {
        return taskRepository.findByPriorityWithCategories(priority);
    }

    public List<Task> findHighPriorityTasksWithCategories() {
        List<Priority> highPriorities = Arrays.asList(Priority.HIGH, Priority.URGENT);
        return taskRepository.findHighPriorityTasksWithCategories(highPriorities, TaskStatus.COMPLETED);
    }

    public List<Task> findByTitleContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(String title) {
        return taskRepository.findByTitleContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(title);
    }

    public List<Task> findByAuthorNameContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(String authorName) {
        return taskRepository.findByAuthorNameContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(authorName);
    }

    public List<Task> findOverdueTasksWithCategories(LocalDate targetDate, TaskStatus status) {
        return taskRepository.findOverdueTasksWithCategories(targetDate, status);
    }

    public Page<Task> findTasksWithFiltersAndCategories(String title, String authorName,
                                                        List<TaskStatus> statusList, Priority priority,
                                                        LocalDate startDate, LocalDate endDate,
                                                        Pageable pageable) {
        return taskRepository.findTasksWithFiltersAndCategories(
                title, authorName, statusList, priority, startDate, endDate, pageable);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = findTaskById(taskId);
        taskRepository.delete(task);
    }

    public Long countByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    public Long countByPriority(Priority priority) {
        return taskRepository.countByPriority(priority);
    }

    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    String.format("종료일(%s)은 시작일(%s)보다 이후여야 합니다.",
                            endDate, startDate)
            );
        }
    }
}