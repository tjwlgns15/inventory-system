package com.yhs.inventroysystem.application.task;


import com.yhs.inventroysystem.domain.task.entity.TaskCategory;
import com.yhs.inventroysystem.domain.task.service.TaskCategoryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yhs.inventroysystem.application.task.TaskCategoryCommands.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskCategoryService {

    private final TaskCategoryDomainService taskCategoryDomainService;


    @Transactional
    public TaskCategory createTaskCategory(TaskCategoryCreateCommand command) {
        return taskCategoryDomainService.createTaskCategory(
                command.name(),
                command.description(),
                command.colorCode(),
                command.displayOrder()
        );
    }

    public List<TaskCategory> getAllCategories() {
        return taskCategoryDomainService.findAllByOrderByDisplayOrderAsc();
    }

    public TaskCategory getCategory(Long taskCategoryId) {
        return taskCategoryDomainService.findTaskCategoryById(taskCategoryId);
    }

    public long getCategoryTaskCount(Long taskCategoryId) {
        return taskCategoryDomainService.countTasksByCategoryId(taskCategoryId);
    }

    public List<TaskCategory> getActiveCategories() {
        return taskCategoryDomainService.findAllByIsActiveTrueOrderByDisplayOrderAsc();
    }

    public List<TaskCategory> searchCategories(String name, Boolean isActive) {
        return taskCategoryDomainService.searchCategories(name, isActive);
    }

    @Transactional
    public TaskCategory updateCategory(Long taskCategoryId, TaskCategoryUpdateCommand command) {
        TaskCategory taskCategory = taskCategoryDomainService.findTaskCategoryById(taskCategoryId);

        if (!taskCategory.getName().equals(command.name())) {
            taskCategoryDomainService.validateDuplicateName(command.name());
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
        TaskCategory taskCategory = taskCategoryDomainService.findTaskCategoryById(taskCategoryId);
        taskCategory.updateDisplayOrder(displayOrder);

        return taskCategory;
    }

    @Transactional
    public void updateDisplayOrders(List<TaskCategoryDisplayOrderUpdateCommand> commands) {
        for (TaskCategoryDisplayOrderUpdateCommand command : commands) {
            TaskCategory taskCategory = taskCategoryDomainService.findTaskCategoryById(command.taskCategoryId());
            taskCategory.updateDisplayOrder(command.displayOrder());
        }
    }

    @Transactional
    public TaskCategory activateCategory(Long taskCategoryId) {
        TaskCategory taskCategory = taskCategoryDomainService.findTaskCategoryById(taskCategoryId);
        taskCategory.activate();

        return taskCategory;
    }

    @Transactional
    public TaskCategory deactivateCategory(Long taskCategoryId) {
        TaskCategory taskCategory = taskCategoryDomainService.findTaskCategoryById(taskCategoryId);
        taskCategory.deactivate();

        return taskCategory;
    }

    @Transactional
    public void deleteCategory(Long taskCategoryId) {
        taskCategoryDomainService.deleteCategory(taskCategoryId);
    }

}