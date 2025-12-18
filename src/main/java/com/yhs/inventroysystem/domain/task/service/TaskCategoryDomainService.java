package com.yhs.inventroysystem.domain.task.service;


import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.task.entity.TaskCategory;
import com.yhs.inventroysystem.domain.task.repository.TaskCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yhs.inventroysystem.application.task.TaskCategoryCommands.TaskCategoryDisplayOrderUpdateCommand;
import static com.yhs.inventroysystem.application.task.TaskCategoryCommands.TaskCategoryUpdateCommand;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskCategoryDomainService {

    private final TaskCategoryRepository taskCategoryRepository;


    @Transactional
    public TaskCategory createTaskCategory(String name, String description,
                                           String colorCode, Integer displayOrder) {
        validateDuplicateName(name);

        TaskCategory taskCategory = new TaskCategory(
                name,
                description,
                colorCode,
                displayOrder
        );

        return taskCategoryRepository.save(taskCategory);
    }

    public void validateDuplicateName(String name) {
        if (taskCategoryRepository.existsByName(name)) {
            throw DuplicateResourceException.taskCategory(name);
        }
    }

    public TaskCategory findByName(String name) {
        return taskCategoryRepository.findByName(name)
                .orElseThrow(() -> ResourceNotFoundException.taskCategory("카테고리 '" + name + "'을 찾을 수 없습니다."));
    }

    public List<TaskCategory> findAllByOrderByDisplayOrderAsc() {
        return taskCategoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    public TaskCategory findTaskCategoryById(Long taskCategoryId) {
        return taskCategoryRepository.findById(taskCategoryId)
                .orElseThrow(() -> ResourceNotFoundException.taskCategory(taskCategoryId));
    }

    public long countTasksByCategoryId(Long taskCategoryId) {
        return taskCategoryRepository.countTasksByCategoryId(taskCategoryId);
    }

    public List<TaskCategory> findAllByIsActiveTrueOrderByDisplayOrderAsc() {
        return taskCategoryRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();
    }

    public List<TaskCategory> searchCategories(String name, Boolean isActive) {
        return taskCategoryRepository.searchCategories(name, isActive);
    }

    @Transactional
    public void deleteCategory(Long taskCategoryId) {
        long taskCount = taskCategoryRepository.countTasksByCategoryId(taskCategoryId);

        if (taskCount > 0) {
            throw new IllegalStateException(String.format("카테고리에 %d개의 작업이 연결되어 있어 삭제할 수 없습니다.", taskCount));
        }

        taskCategoryRepository.deleteById(taskCategoryId);
    }
}