package com.yhs.inventroysystem.domain.delivery;

import com.yhs.inventroysystem.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("SELECT d FROM Delivery d " +
            "JOIN FETCH d.client c " +
            "LEFT JOIN FETCH d.items i " +
            "LEFT JOIN FETCH i.product " +
            "WHERE d.id = :deliveryId " +
            "AND d.deletedAt IS NULL")
    Optional<Delivery> findByIdWithItems(@Param("deliveryId") Long deliveryId);

    @Query("SELECT d FROM Delivery d " +
            "JOIN FETCH d.client " +
            "JOIN FETCH d.items di " +
            "JOIN FETCH di.product " +
            "WHERE d.id = :deliveryId " +
            "AND d.deletedAt IS NULL")
    Optional<Delivery> findById(@Param("deliveryId") Long deliveryId);

    /**
     * 페이징 조회 - 컬렉션 페치 제거하여 경고 해결
     * items는 @BatchSize로 N+1 문제 해결
     */
    @Query(value = "SELECT DISTINCT d FROM Delivery d " +
            "JOIN FETCH d.client c " +
            "WHERE d.deletedAt IS NULL",
            countQuery = "SELECT COUNT(DISTINCT d) FROM Delivery d " +
                    "WHERE d.deletedAt IS NULL")
    Page<Delivery> findAllPaged(Pageable pageable);

    @Query(value = "SELECT DISTINCT d FROM Delivery d " +
            "JOIN FETCH d.client c " +
            "WHERE d.deletedAt IS NULL " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(d.deliveryNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            countQuery = "SELECT COUNT(DISTINCT d) FROM Delivery d " +
                    "JOIN d.client c " +
                    "WHERE d.deletedAt IS NULL " +
                    "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(d.deliveryNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Delivery> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT d
        FROM Delivery d 
        JOIN FETCH d.items di 
        JOIN FETCH di.product p
        JOIN FETCH d.client c
        WHERE d.deletedAt IS NULL
        AND d.status = 'COMPLETED' 
        AND d.deliveredAt BETWEEN :startDate AND :endDate
        ORDER BY d.deliveredAt
    """)
    List<Delivery> findWeeklySales(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
    SELECT d
    FROM Delivery d 
    JOIN FETCH d.items di 
    JOIN FETCH di.product p
    JOIN FETCH d.client c
    WHERE d.deletedAt IS NULL
    AND d.status = 'COMPLETED' 
    AND d.deliveredAt BETWEEN :startDate AND :endDate
    ORDER BY d.deliveredAt
    """)
    List<Delivery> findCompletedDeliveriesByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT d
        FROM Delivery d 
        JOIN FETCH d.client c
        LEFT JOIN FETCH c.parentClient 
        JOIN FETCH d.items di 
        JOIN FETCH di.product p 
        WHERE d.deletedAt IS NULL
        AND d.status = 'COMPLETED' 
        AND YEAR(d.deliveredAt) = :year 
        ORDER BY c.name, d.deliveredAt
    """)
    List<Delivery> findCompletedDeliveriesByYear(@Param("year") int year);

    @Query("""
        SELECT CAST(SUBSTRING(d.deliveryNumber, 15, 3) AS integer)
        FROM Delivery d
        WHERE d.deletedAt IS NULL
        AND d.deliveryNumber LIKE CONCAT('SOLM-PO-', :yearMonth, '%')
        ORDER BY d.deliveryNumber DESC
        LIMIT 1
    """)
    Integer findLastSequenceByYearMonth(@Param("yearMonth") String yearMonth);


    boolean existsByDeliveryNumber(String deliveryNumber);

    @Query("SELECT d FROM Delivery d " +
            "JOIN FETCH d.client " +
            "WHERE d.deletedAt IS NULL " +
            "AND d.deliveryNumber = :deliveryNumber")
    Optional<Delivery> findByDeliveryNumber(@Param("deliveryNumber") String deliveryNumber);

    @Query(value = """
    SELECT MAX(
        CAST(
            SUBSTRING_INDEX(delivery_number, '-', -1)
            AS UNSIGNED
        )
    )
    FROM deliveries
    WHERE deleted_at IS NULL
    AND delivery_number LIKE CONCAT('SOLM-PO-', :year, '-%')
    AND SUBSTRING_INDEX(delivery_number, '-', -1) REGEXP '^[0-9]+$'
    """, nativeQuery = true)
    Integer findLastSequenceByYear(@Param("year") String year);

}