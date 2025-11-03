package com.yhs.inventroysystem.domain.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Long> {

    Optional<TaskCategory> findByName(String name);

    boolean existsByName(String name);

    List<TaskCategory> findAllByIsActiveTrueOrderByDisplayOrderAsc();

    List<TaskCategory> findAllByOrderByDisplayOrderAsc();

    Page<TaskCategory> findAllByIsActiveTrueOrderByDisplayOrderAsc(Pageable pageable);

    @Query("SELECT tc FROM TaskCategory tc WHERE tc.isActive = :isActive ORDER BY tc.displayOrder ASC")
    List<TaskCategory> findByIsActive(@Param("isActive") Boolean isActive);

    @Query("SELECT COUNT(tcm) FROM TaskCategoryMapping tcm WHERE tcm.category.id = :categoryId")
    long countTasksByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT tc FROM TaskCategory tc WHERE " +
            "(:name IS NULL OR tc.name LIKE %:name%) AND " +
            "(:isActive IS NULL OR tc.isActive = :isActive) " +
            "ORDER BY tc.displayOrder ASC")
    List<TaskCategory> searchCategories(
            @Param("name") String name,
            @Param("isActive") Boolean isActive);
}
