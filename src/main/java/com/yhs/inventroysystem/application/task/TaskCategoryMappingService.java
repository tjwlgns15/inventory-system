package com.yhs.inventroysystem.application.task;

import com.yhs.inventroysystem.domain.task.entity.Task;
import com.yhs.inventroysystem.domain.task.entity.TaskCategory;
import com.yhs.inventroysystem.domain.task.service.TaskCategoryDomainService;
import com.yhs.inventroysystem.domain.task.service.TaskDomainService;
import com.yhs.inventroysystem.domain.task.service.TaskMappingDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskCategoryMappingService {

    private final TaskDomainService taskDomainService;
    private final TaskCategoryDomainService taskCategoryDomainService;
    private final TaskMappingDomainService taskMappingDomainService;

    /**
     * 작업에 카테고리 추가
     */
    @Transactional
    public void addTaskCategoryToTask(Long taskId, Long categoryId) {
        Task task = findTaskById(taskId);
        TaskCategory taskCategory = findTaskCategoryById(categoryId);

        task.addCategory(taskCategory);
        log.info("작업에 카테고리가 추가되었습니다. taskId: {}, categoryId: {}", taskId, categoryId);
    }

    /**
     * 작업에서 카테고리 제거
     */
    @Transactional
    public void removeCategoryFromTask(Long taskId, Long categoryId) {
        Task task = findTaskById(taskId);
        TaskCategory taskCategory = findTaskCategoryById(categoryId);

        task.removeCategory(taskCategory);
        log.info("작업에서 카테고리가 제거되었습니다. taskId: {}, categoryId: {}", taskId, categoryId);
    }

    /**
     * 작업의 카테고리 목록 조회
     */
    public List<TaskCategory> getTaskCategories(Long taskId) {
        findTaskById(taskId);
        return taskMappingDomainService.findCategoriesByTaskId(taskId);
    }

    /**
     * 작업의 카테고리 목록을 한 번에 업데이트
     */
    @Transactional
    public void updateTaskCategories(Long taskId, List<Long> taskCategoryIds) {
        Task task = findTaskById(taskId);

        task.clearCategories();

        // 새로운 카테고리들 추가
        if (taskCategoryIds != null && !taskCategoryIds.isEmpty()) {
            taskCategoryIds.stream()
                    .map(this::findTaskCategoryById)
                    .forEach(task::addCategory);
        }

        log.info("작업의 카테고리가 업데이트되었습니다. taskId: {}, 카테고리 개수: {}",
                taskId, taskCategoryIds != null ? taskCategoryIds.size() : 0);
    }

    /**
     * 카테고리별 작업 목록 조회 (Fetch Join 사용)
     */
    public List<Task> getTasksByCategory(Long categoryId) {
        findTaskCategoryById(categoryId);

        // 1단계: 해당 카테고리에 속한 Task ID 목록 조회
        List<Long> taskIds = taskMappingDomainService.findTaskIdsByCategoryId(categoryId);

        if (taskIds.isEmpty()) {
            return List.of();
        }

        // 2단계: Task ID로 Fetch Join하여 Task + 카테고리 정보 조회
        // TaskRepository에 이미 있는 메서드 활용
        return taskDomainService.findTasksByIdsWithCategories(taskIds);
    }

    /**
     * 모든 작업에 카테고리 매핑 (초기 데이터 설정용)
     */
    @Transactional
    public void mappingAllTasks() {
        log.info("모든 Task에 대한 자동 카테고리 매핑 시작");

        // 필요한 카테고리들 미리 조회
        TaskCategory orderDeliveryCategory = taskCategoryDomainService.findByName("수주/납품");

        TaskCategory productionCategory = taskCategoryDomainService.findByName("제품 생산");

        TaskCategory etcCategory = taskCategoryDomainService.findByName("기타");

        // 모든 Task 조회
        List<Task> allTasks = taskDomainService.findAllTasks();
        log.info("이 {} 개의 Task를 조회했습니다.", allTasks.size());

        int mappedCount = 0;
        int skippedCount = 0;

        for (Task task : allTasks) {
            // 이미 카테고리가 매핑되어 있으면 건너뛰기
            if (task.getCategoryCount() > 0) {
                skippedCount++;
                continue;
            }

            TaskCategory targetCategory = determineCategory(
                    task.getTitle(),
                    orderDeliveryCategory,
                    productionCategory,
                    etcCategory
            );

            task.addCategory(targetCategory);
            mappedCount++;

            log.debug("Task[{}] '{}' -> 카테고리 '{}'로 매핑",
                    task.getId(), task.getTitle(), targetCategory.getName());
        }

        log.info("자동 카테고리 매핑 완료 - 매핑: {}개, 건너뜀: {}개", mappedCount, skippedCount);
    }

    /**
     * 작업 제목을 기반으로 적절한 카테고리 결정
     */
    private TaskCategory determineCategory(
            String title,
            TaskCategory orderDeliveryCategory,
            TaskCategory productionCategory,
            TaskCategory etcCategory) {

        if (title == null) {
            return etcCategory;
        }

        // "출하", "주문", "납품" 중 하나라도 포함되어 있으면 수주/납품
        if (title.contains("출하") || title.contains("주문") || title.contains("납품")) {
            return orderDeliveryCategory;
        }

        // "생산"이 포함되어 있으면 생산
        if (title.contains("생산")) {
            return productionCategory;
        }

        // 둘 다 해당 없으면 기타
        return etcCategory;
    }

    private Task findTaskById(Long taskId) {
        return taskDomainService.findTaskById(taskId);
    }

    private TaskCategory findTaskCategoryById(Long taskCategoryId) {
        return taskCategoryDomainService.findTaskCategoryById(taskCategoryId);
    }
}