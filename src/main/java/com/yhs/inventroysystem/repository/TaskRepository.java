package com.yhs.inventroysystem.repository;


import com.yhs.inventroysystem.entity.enumerate.Priority;
import com.yhs.inventroysystem.entity.Task;
import com.yhs.inventroysystem.entity.enumerate.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<Task, Long> {

    // 상태별 조회
    List<Task> findByStatusOrderByPriorityDescCreatedAtDesc(TaskStatus status);

    // 우선순위별 조회
    List<Task> findByPriorityOrderByCreatedAtDesc(Priority priority);

    // 작성자별 조회
    List<Task> findByAuthorNameContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(String authorName);

    // 제목으로 검색
    List<Task> findByTitleContainingIgnoreCaseOrderByPriorityDescCreatedAtDesc(String title);

    // 기간별 조회 (우선순위 순으로 정렬)
    List<Task> findByStartDateBetweenOrderByPriorityDescStartDateAsc(LocalDate startDate, LocalDate endDate);

    // 지연된 작업 조회 (우선순위 순)
    @Query("SELECT t FROM Task t WHERE t.endDate < :currentDate AND t.status != :completedStatus ORDER BY t.priority DESC, t.endDate ASC")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDate currentDate,
                                @Param("completedStatus") TaskStatus completedStatus);

    // 높은 우선순위 작업 조회
    @Query("SELECT t FROM Task t WHERE t.priority IN (:priorities) AND t.status != :completedStatus ORDER BY t.priority DESC, t.createdAt DESC")
    List<Task> findHighPriorityTasks(@Param("priorities") List<Priority> priorities,
                                     @Param("completedStatus") TaskStatus completedStatus);

    // 상태별 개수 조회
    long countByStatus(TaskStatus status);

    // 우선순위별 개수 조회
    long countByPriority(Priority priority);

    // 페이징 조회 (우선순위, 생성일 순)
    Page<Task> findAllByOrderByPriorityDescCreatedAtDesc(Pageable pageable);

    // 복합 검색
    @Query("SELECT t FROM Task t WHERE " +
            "(:title IS NULL OR UPPER(t.title) LIKE UPPER(CONCAT('%', :title, '%'))) AND " +
            "(:authorName IS NULL OR UPPER(t.authorName) LIKE UPPER(CONCAT('%', :authorName, '%'))) AND " +
            "(:statusList IS NULL OR t.status IN :statusList) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:startDate IS NULL OR t.startDate >= :startDate) AND " +
            "(:endDate IS NULL OR t.endDate <= :endDate) " +
            "ORDER BY t.priority DESC, t.createdAt DESC")
    Page<Task> findTasksWithFilters(@Param("title") String title,
                                    @Param("authorName") String authorName,
                                    @Param("statusList") List<TaskStatus> statusList,
                                    @Param("priority") Priority priority,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    Pageable pageable);
}