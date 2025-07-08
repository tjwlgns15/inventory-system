package com.yhs.inventroysystem.repository;

import com.yhs.inventroysystem.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 부품(Part) 엔티티를 위한 JPA 리포지토리
 * - 기본 CRUD 기능 제공
 * - 부품 코드 기반 중복 체크 및 조회 기능 포함
 */
public interface PartRepository extends JpaRepository<Part, Long> {

    /**
     * 주어진 부품 코드가 이미 존재하는지 확인 (삭제되지 않은 것만)
     * @param partCode 부품 코드
     * @return 존재 여부 (true/false)
     */
    @Query("SELECT COUNT(p) > 0 FROM Part p WHERE p.partCode = :partCode AND p.deletedAt IS NULL")
    boolean existsByPartCode(String partCode);

    /**
     * 부품 코드를 기준으로 부품 정보 조회 (삭제되지 않은 것만)
     * @param partCode 부품 코드
     * @return Optional 형태의 부품 객체
     */
    @Query("SELECT p FROM Part p WHERE p.partCode = :partCode AND p.deletedAt IS NULL")
    Optional<Part> findByPartCode(String partCode);

    // 삭제되지 않은 부품들만 조회
    @Query("SELECT p FROM Part p WHERE p.deletedAt IS NULL")
    List<Part> findAll();

    // 삭제되지 않은 부품 단건 조회
    @Query("SELECT p FROM Part p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Part> findById(Long id);
}
