package com.yhs.inventroysystem.domain.shipment.repository;

import com.yhs.inventroysystem.domain.shipment.entity.Shipment;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentType;
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
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * fetch join 사용
     * boxes, items, carrier 조인
     */
    @Query("SELECT s FROM Shipment s " +
            "LEFT JOIN FETCH s.carrier " +
            "WHERE s.id = :id " +
            "AND s.deletedAt IS NULL")
    Optional<Shipment> findById(Long id);

    @Query("SELECT s FROM Shipment s " +
            "WHERE s.deletedAt IS NULL")
    List<Shipment> findAll();

    /**
     * Invoice 번호 존재 여부 확인
     */
    boolean existsByInvoiceNumber(String invoiceNumber);

    /**
     * 기간별 선적 목록 조회
     */
    List<Shipment> findByInvoiceDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 연도별 최대 시퀀스 조회
     */
    @Query("SELECT MAX(s.sequence) FROM Shipment s WHERE s.year = :year")
    Optional<Integer> findMaxSequenceByYear(@Param("year") int year);

    /**
     * 연도별 선적 목록 조회 (시퀀스 내림차순)
     */
    List<Shipment> findByYearOrderBySequenceDesc(Integer year);

    @Query("SELECT s FROM Shipment s " +
            "WHERE s.deletedAt IS NULL " +
            "AND s.shipmentType = :shipmentType " +
            "AND s.soldToCompanyName LIKE %:keyword%")
    Page<Shipment> searchByKeywordAndType(@Param("keyword") String keyword,
                                          @Param("shipmentType") ShipmentType shipmentType,
                                          Pageable pageable);

    @Query("SELECT s FROM Shipment s " +
            "WHERE s.deletedAt IS NULL " +
            "AND s.soldToCompanyName LIKE %:keyword%")
    Page<Shipment> searchByKeyword(@Param("keyword") String keyword,
                                   Pageable pageable);

    @Query("SELECT s FROM Shipment s " +
            "WHERE s.deletedAt IS NULL " +
            "AND s.shipmentType = :shipmentType")
    Page<Shipment> findAllByType(@Param("shipmentType") ShipmentType shipmentType,
                                 Pageable pageable);

    @Query("SELECT s FROM Shipment s " +
            "WHERE s.deletedAt IS NULL")
    Page<Shipment> findAllPaged(Pageable pageable);
}
