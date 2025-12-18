package com.yhs.inventroysystem.domain.task.repository;

import com.yhs.inventroysystem.domain.task.entity.Task;
import com.yhs.inventroysystem.domain.task.entity.TaskCategory;
import com.yhs.inventroysystem.domain.task.entity.TaskCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskCategoryMappingRepository extends JpaRepository<TaskCategoryMapping, Long> {

    /**
     * 특정 작업의 카테고리 목록 조회
     */
    @Query("SELECT tcm.category FROM TaskCategoryMapping tcm " +
            "WHERE tcm.task.id = :taskId " +
            "ORDER BY tcm.category.displayOrder ASC")
    List<TaskCategory> findCategoriesByTaskId(@Param("taskId") Long taskId);

    /**
     * 여러 작업의 카테고리를 한 번에 조회 (N+1 문제 해결)
     */
    @Query("SELECT tcm FROM TaskCategoryMapping tcm " +
            "JOIN FETCH tcm.task " +
            "JOIN FETCH tcm.category " +
            "WHERE tcm.task.id IN :taskIds " +
            "ORDER BY tcm.category.displayOrder ASC")
    List<TaskCategoryMapping> findCategoriesByTaskIds(@Param("taskIds") List<Long> taskIds);

    /**
     * 특정 카테고리에 속한 작업 목록 조회 (카테고리 정보 없이)
     */
    @Query("SELECT tcm.task FROM TaskCategoryMapping tcm " +
            "WHERE tcm.category.id = :categoryId " +
            "ORDER BY tcm.task.priority DESC, tcm.task.createdAt DESC")
    List<Task> findTasksByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 특정 카테고리에 속한 작업 ID 목록 조회
     * 이 방법을 사용하여 Task를 다시 조회하면 Fetch Join 가능
     */
    @Query("SELECT tcm.task.id FROM TaskCategoryMapping tcm " +
            "WHERE tcm.category.id = :categoryId " +
            "ORDER BY tcm.task.priority DESC, tcm.task.createdAt DESC")
    List<Long> findTaskIdsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 특정 작업과 카테고리 매핑 존재 여부 확인
     */
    boolean existsByTaskIdAndCategoryId(Long taskId, Long categoryId);

    /**
     * 특정 작업과 카테고리 매핑 조회
     */
    Optional<TaskCategoryMapping> findByTaskIdAndCategoryId(Long taskId, Long categoryId);

    /**
     * 특정 작업의 모든 매핑 삭제
     */
    void deleteByTaskId(Long taskId);

    /**
     * 특정 카테고리의 모든 매핑 삭제
     */
    void deleteByCategoryId(Long categoryId);

    /**
     * 특정 카테고리에 연결된 작업 수 조회
     */
    long countByCategoryId(Long categoryId);
}