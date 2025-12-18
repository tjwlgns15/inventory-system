package com.yhs.inventroysystem.domain.task.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task_categories", indexes = {
        @Index(name = "idx_category_name", columnList = "name")
})
@Getter
@NoArgsConstructor
public class TaskCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "color_code", length = 7)
    private String colorCode;

    @Column(name = "display_order")
    private Integer displayOrder;  // 표시될 때 보여줄 순서

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskCategoryMapping> taskMappings = new ArrayList<>();

    public TaskCategory(String name, String description, String colorCode, Integer displayOrder) {
        validateName(name);
        this.name = name;
        this.description = description;
        this.colorCode = colorCode;
        this.displayOrder = displayOrder;
        this.isActive = true;
    }

    public void updateInfo(String name, String description, String colorCode) {
        if (name != null) {
            validateName(name);
            this.name = name;
        }
        this.description = description;
        this.colorCode = colorCode;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public int getTaskCount() {
        return taskMappings.size();
    }

    public boolean hasColorCode() {
        return this.colorCode != null && !this.colorCode.isBlank();
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("카테고리 이름은 필수입니다.");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("카테고리 이름은 50자를 초과할 수 없습니다.");
        }
    }
}
