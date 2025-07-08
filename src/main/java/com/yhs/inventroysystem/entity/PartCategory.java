package com.yhs.inventroysystem.entity;

import com.yhs.inventroysystem.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity @Getter
@AllArgsConstructor @NoArgsConstructor
@SQLDelete(sql = "UPDATE part_category SET deleted_at = NOW() WHERE id = ?")  // 삭제 요청이 들어올 경우 삭제 시간 업데이트 (소프트 삭제)
//@SQLRestriction("deleted_at IS NULL")
public class PartCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    private Long id;

    private String name; // 분류 이름 (예: 전자, 기계 등)

    public void updateName(String name) {
        validateName(name);
        this.name = name;
    }

    // 검증 메서드
    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리명은 필수입니다");
        }
    }
}