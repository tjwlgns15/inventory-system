package com.yhs.inventroysystem.domain.task.service;

import com.yhs.inventroysystem.domain.task.entity.TaskCategory;
import com.yhs.inventroysystem.domain.task.repository.TaskCategoryMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskMappingDomainService {

    private final TaskCategoryMappingRepository taskCategoryMappingRepository;

    public List<TaskCategory> findCategoriesByTaskId(Long taskId) {
        return taskCategoryMappingRepository.findCategoriesByTaskId(taskId);
    }
    public List<Long> findTaskIdsByCategoryId(Long categoryId) {
        return taskCategoryMappingRepository.findTaskIdsByCategoryId(categoryId);
    }

}
