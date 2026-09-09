package com.yhs.inventroysystem.domain.contract.repository;

import com.yhs.inventroysystem.domain.contract.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    @Query("SELECT c FROM Contract c " +
            "WHERE c.id = :contractId " +
            "AND c.deletedAt IS NULL")
    Optional<Contract> findById(@Param("contractId") Long contractId);

    @Query("SELECT DISTINCT c FROM Contract c " +
            "LEFT JOIN FETCH c.documents " +
            "WHERE c.id = :contractId " +
            "AND c.deletedAt IS NULL")
    Optional<Contract> findByIdWithDocuments(@Param("contractId") Long contractId);

    @Query("""
            SELECT c
            FROM Contract c
            WHERE c.deletedAt IS NULL
            AND c.contractDate BETWEEN :startDate AND :endDate
            ORDER BY c.contractDate
            """)
    List<Contract> findContractsByPeriod(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * 문서명 또는 고객사명으로 검색
     */
    @Query(
            value = "SELECT DISTINCT c FROM Contract c " +
                    "WHERE c.deletedAt IS NULL " +
                    "AND (LOWER(c.documentName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            countQuery = "SELECT COUNT(DISTINCT c) FROM Contract c " +
                    "WHERE c.deletedAt IS NULL " +
                    "AND (LOWER(c.documentName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))"
    )
    Page<Contract> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query(
            value = "SELECT DISTINCT c FROM Contract c WHERE c.deletedAt IS NULL ORDER BY c.recordDate DESC",
            countQuery = "SELECT COUNT(DISTINCT c) FROM Contract c WHERE c.deletedAt IS NULL"
    )
    Page<Contract> findAllPaged(Pageable pageable);
}