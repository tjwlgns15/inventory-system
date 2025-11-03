package com.yhs.inventroysystem.application.task;


import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.task.TaskCommands.TaskCreateCommand;
import com.yhs.inventroysystem.application.task.TaskCommands.TaskUpdateCommand;
import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.task.*;
import jakarta.validation.constraints.Min;
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

import static com.yhs.inventroysystem.application.task.TaskCategoryCommands.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskCategoryService {

    private final TaskCategoryRepository taskCategoryRepository;
    private final TaskRepository taskRepository;
    private final TaskCategoryMappingRepository taskCategoryMappingRepository;

    @Transactional
    public TaskCategory createTaskCategory(TaskCategoryCreateCommand command) {
        validateDuplicateName(command.name());

        TaskCategory taskCategory = new TaskCategory(
                command.name(),
                command.description(),
                command.colorCode(),
                command.displayOrder()
        );
        return taskCategoryRepository.save(taskCategory);
    }

    public List<TaskCategory> getAllCategories() {
        return taskCategoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    public TaskCategory getCategory(Long taskCategoryId) {
        return findTaskCategoryById(taskCategoryId);
    }

    public long getCategoryTaskCount(Long taskCategoryId) {
        return taskCategoryRepository.countTasksByCategoryId(taskCategoryId);
    }

    public List<TaskCategory> getActiveCategories() {
        return taskCategoryRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();
    }

    public List<TaskCategory> searchCategories(String name, Boolean isActive) {
        return taskCategoryRepository.searchCategories(name, isActive);
    }

    @Transactional
    public TaskCategory updateCategory(Long taskCategoryId, TaskCategoryUpdateCommand command) {
        TaskCategory taskCategory = findTaskCategoryById(taskCategoryId);

        if (!taskCategory.getName().equals(command.name())) {
            validateDuplicateName(command.name());
        }

        taskCategory.updateInfo(
                command.name(),
                command.description(),
                command.colorCode()
        );

        if (command.displayOrder() != null) {
            taskCategory.updateDisplayOrder(command.displayOrder());
        }

        return taskCategory;
    }

    @Transactional
    public TaskCategory updateDisplayOrder(Long taskCategoryId, Integer displayOrder) {
        TaskCategory taskCategory = findTaskCategoryById(taskCategoryId);
        taskCategory.updateDisplayOrder(displayOrder);

        return taskCategory;
    }
@Transactional
    public void updateDisplayOrders(List<TaskCategoryDisplayOrderUpdateCommand> commands) {
        for (TaskCategoryDisplayOrderUpdateCommand command : commands) {
            TaskCategory taskCategory = findTaskCategoryById(command.taskCategoryId());
            taskCategory.updateDisplayOrder(command.displayOrder());
        }
    }

    @Transactional
    public TaskCategory activateCategory(Long taskCategoryId) {
        TaskCategory taskCategory = findTaskCategoryById(taskCategoryId);
        taskCategory.activate();

        return taskCategory;
    }

    @Transactional
    public TaskCategory deactivateCategory(Long taskCategoryId) {
        TaskCategory taskCategory = findTaskCategoryById(taskCategoryId);
        taskCategory.deactivate();

        return taskCategory;
    }

    @Transactional
    public void deleteCategory(Long taskCategoryId) {
        long taskCount = taskCategoryRepository.countTasksByCategoryId(taskCategoryId);

        if (taskCount > 0) {
            throw new IllegalStateException(String.format("카테고리에 %d개의 작업이 연결되어 있어 삭제할 수 없습니다.", taskCount));
        }

        taskCategoryRepository.deleteById(taskCategoryId);
    }

    private void validateDuplicateName(String name) {
        if (taskCategoryRepository.existsByName(name)) {
            throw DuplicateResourceException.taskCategory(name);
        }
    }

    private TaskCategory findTaskCategoryById(Long taskCategoryId) {
        return taskCategoryRepository.findById(taskCategoryId)
                .orElseThrow(() -> ResourceNotFoundException.taskCategory(taskCategoryId));
    }
}