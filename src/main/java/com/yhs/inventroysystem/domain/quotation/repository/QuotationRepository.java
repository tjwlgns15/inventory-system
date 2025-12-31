package com.yhs.inventroysystem.domain.quotation.repository;

import com.yhs.inventroysystem.domain.quotation.entity.Quotation;
import com.yhs.inventroysystem.domain.quotation.entity.QuotationType;
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
public interface QuotationRepository extends JpaRepository<Quotation, Long> {


    @Query("SELECT q FROM Quotation q " +
            "WHERE q.id = :quotationId " +
            "AND q.deletedAt IS NULL")
    Optional<Quotation> findById(@Param("quotationId") Long quotationId);

    /**
     * 특정 연도와 타입에 해당하는 마지막 시퀀스 번호 조회
     * 예: SOLM-RECEIPT-2024-0001 -> 1
     * SOLM-ISSUANCE-2024-0005 -> 5
     */
    @Query(value = """
            SELECT MAX(
                CAST(
                    SUBSTRING_INDEX(quotation_number, '-', -1)
                    AS UNSIGNED
                )
            )
            FROM quotaions
            WHERE deleted_at IS NULL
            AND quotation_number LIKE CONCAT(:prefix, '-', :year, '-%')
            AND SUBSTRING_INDEX(quotation_number, '-', -1) REGEXP '^[0-9]+$'
            """, nativeQuery = true)
    Integer findLastSequenceByYearAndType(
            @Param("prefix") String prefix,
            @Param("year") String year
    );

    boolean existsByQuotationNumber(String quotationNumber);

    @Query("SELECT q FROM Quotation q " +
            "LEFT JOIN FETCH q.items " +
            "WHERE q.id = :quotationId " +
            "AND q.deletedAt IS NULL")
    Optional<Quotation> findByIdWithItems(@Param("quotationId") Long quotationId);

    @Query("""
            SELECT q
            FROM Quotation q 
            JOIN FETCH q.items 
            WHERE q.deletedAt IS NULL
            AND q.orderedAt BETWEEN :startDate AND :endDate
            ORDER BY q.orderedAt
            """)
    List<Quotation> findQuotationsByPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT q FROM Quotation q " +
            "WHERE q.deletedAt IS NULL " +
            "AND q.quotationType = :quotationType " +
            "AND q.companyName LIKE %:keyword%")
    Page<Quotation> searchByKeywordAndType(@Param("keyword") String keyword,
                                           @Param("quotationType") QuotationType quotationType,
                                           Pageable pageable);

    @Query("SELECT q FROM Quotation q " +
            "WHERE q.deletedAt IS NULL " +
            "AND q.quotationType = :quotationType")
    Page<Quotation> findAllByType(@Param("quotationType") QuotationType quotationType,
                                  Pageable pageable);


    @Query(
            value = "SELECT DISTINCT q " +
                    "FROM Quotation q " +
                    "WHERE  q.deletedAt IS NULL " +
                    "AND (LOWER(q.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            countQuery = "SELECT COUNT(DISTINCT q) " +
                    "FROM Quotation q " +
                    "WHERE  q.deletedAt IS NULL " +
                    "AND (LOWER(q.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))"
    )
    Page<Quotation> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query(
            value = "SELECT DISTINCT q " +
                    "FROM Quotation q " +
                    "WHERE q.deletedAt IS NULL",
            countQuery = "SELECT COUNT(DISTINCT q) " +
                    "FROM Quotation q " +
                    "WHERE q.deletedAt IS NULL"
    )
    Page<Quotation> findAllPaged(Pageable pageable);
}
