package com.yhs.inventroysystem.domain.delivery;

import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.*;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("SELECT d FROM Delivery d " +
            "JOIN FETCH d.client c " +
            "LEFT JOIN FETCH d.items i " +
            "LEFT JOIN FETCH i.product " +
            "WHERE d.id = :deliveryId")
    Optional<Delivery> findByIdWithItems(@Param("deliveryId") Long deliveryId);

    @Query("SELECT d FROM Delivery d " +
            "JOIN FETCH d.client " +
            "JOIN FETCH d.items di " +
            "JOIN FETCH di.product " +
            "WHERE d.id = :deliveryId")
    Optional<Delivery> findById(@Param("deliveryId") Long deliveryId);

    @Query("SELECT d FROM Delivery d " +
            "JOIN FETCH d.client " +
            "JOIN FETCH d.items di " +
            "JOIN FETCH di.product")
    List<Delivery> findAllWithClientAndItem();

    @Query("""
                SELECT new com.yhs.inventroysystem.presentation.sales.SalesStatsDtos$ProductSalesData(
                    p.id, p.productCode, p.name, 
                    CAST(SUM(di.quantity) AS integer), 
                    SUM(di.totalPrice)
                )
                FROM Delivery d 
                JOIN d.items di 
                JOIN di.product p
                WHERE d.status = 'COMPLETED' 
                AND d.deliveredAt BETWEEN :startDate AND :endDate
                GROUP BY p.id, p.productCode, p.name
                ORDER BY SUM(di.quantity) DESC
            """)
    List<ProductSalesData> findWeeklySalesByProduct(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
                SELECT d
                FROM Delivery d 
                JOIN FETCH d.client c
                WHERE d.status = 'COMPLETED' 
                AND YEAR(d.deliveredAt) = :year
                ORDER BY c.name
            """)
    List<Delivery> findCompletedDeliveriesByYear(@Param("year") int year);

    @Query("""
                SELECT new com.yhs.inventroysystem.presentation.sales.SalesStatsDtos$ProductSalesData(
                    p.id, p.productCode, p.name,
                    CAST(SUM(di.quantity) AS integer),
                    SUM(di.totalPrice)
                )
                FROM Delivery d 
                JOIN d.items di
                JOIN di.product p
                WHERE d.client.id = :clientId 
                AND d.status = 'COMPLETED'
                AND YEAR(d.deliveredAt) = :year
                GROUP BY p.id, p.productCode, p.name
                ORDER BY SUM(di.quantity) DESC
            """)
    List<ProductSalesData> findYearlySalesByClientAndProduct(
            @Param("clientId") Long clientId,
            @Param("year") int year
    );
}