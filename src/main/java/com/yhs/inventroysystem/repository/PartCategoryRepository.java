package com.yhs.inventroysystem.repository;

import com.yhs.inventroysystem.entity.PartCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 부품 분류(PartCategory) 엔티티를 위한 JPA 리포지토리
 * - 기본 CRUD 기능 제공
 * - 분류명 기준 조회 및 중복 확인 기능 포함
 */
public interface PartCategoryRepository extends JpaRepository<PartCategory, Long> {

    /**
     * 분류명이 이미 존재하는지 확인 (삭제되지 않은 것만)
     * @param name 분류 이름
     * @return 존재 여부 (true/false)
     */
    @Query("SELECT COUNT(c) > 0 FROM PartCategory c WHERE c.name = :name AND c.deletedAt IS NULL")
    boolean existsByName(String name);

    /**
     * 분류명으로 분류 정보 조회 (삭제되지 않은 것만)
     * @param name 분류 이름
     * @return Optional 형태의 분류 객체
     */
    @Query("SELECT c FROM PartCategory c WHERE c.name = :name AND c.deletedAt IS NULL")
    Optional<PartCategory> findByName(String name);

    // 삭제되지 않은 분류들만 조회
    @Query("SELECT c FROM PartCategory c WHERE c.deletedAt IS NULL")
    List<PartCategory> findAll();

    // 삭제되지 않은 분류 단건 조회
    @Query("SELECT c FROM PartCategory c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<PartCategory> findById(Long id);
}
