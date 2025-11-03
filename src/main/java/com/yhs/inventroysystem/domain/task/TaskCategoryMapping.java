package com.yhs.inventroysystem.domain.task;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "task_category_mappings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_task_category", columnNames = {"task_id", "category_id"})
        },
        indexes = {
                @Index(name = "idx_task_id", columnList = "task_id"),
                @Index(name = "idx_category_id", columnList = "category_id")
        }
)
@Getter
@NoArgsConstructor
public class TaskCategoryMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mapping_task"))
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mapping_category"))
    private TaskCategory category;

    public TaskCategoryMapping(Task task, TaskCategory category) {
        validateMapping(task, category);
        this.task = task;
        this.category = category;
    }

    public boolean isMappedTo(Task task) {
        return this.task.equals(task);
    }

    public boolean isMappedTo(TaskCategory category) {
        return this.category.equals(category);
    }

    private void validateMapping(Task task, TaskCategory category) {
        if (task == null) {
            throw new IllegalArgumentException("작업은 필수입니다.");
        }
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        if (!category.isActive()) {
            throw new IllegalStateException("비활성화된 카테고리에는 작업을 연결할 수 없습니다.");
        }
    }
}