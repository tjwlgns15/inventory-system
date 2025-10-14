package com.yhs.inventroysystem.domain.part;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 부품(Part) 엔티티를 위한 JPA 리포지토리
 * - 기본 CRUD 기능 제공
 * - 조회 시 삭제 되지 않은 것만 검색
 */
public interface PartRepository extends JpaRepository<Part, Long> {

    /**
     * 주어진 부품 코드가 이미 존재하는지 확인
     */
    @Query("SELECT COUNT(p) > 0 FROM Part p WHERE p.partCode = :partCode AND p.deletedAt IS NULL")
    boolean existsByPartCodeAndNotDeleted(String partCode);

    /**
     * 주어진 부품 이름이 이미 존재하는지 확인
     */
    @Query("SELECT COUNT(p) > 0 FROM Part p WHERE p.name = :name AND p.deletedAt IS NULL")
    boolean existsByNameAndNotDeleted(String name);

    /**
     * 부품 코드를 기준으로 부품 정보 조회
     */
    @Query("SELECT p FROM Part p WHERE p.partCode = :partCode AND p.deletedAt IS NULL")
    Optional<Part> findByPartCodeAndNotDeleted(String partCode);

    /**
     * 부품 이름을 기준으로 부품 정보 조회
     */
    @Query("SELECT p FROM Part p WHERE p.name = :name AND p.deletedAt IS NULL")
    Optional<Part> findByNameAndNotDeleted(String name);

    /**
     * 삭제되지 않은 모든 부품들 조회
     */
    @Query("SELECT p FROM Part p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Part> findAllActive();

    /**
     * 삭제되지 않은 부품 단건 조회
     */
    @Query("SELECT p FROM Part p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Part> findByIdAndNotDeleted(Long id);
}
