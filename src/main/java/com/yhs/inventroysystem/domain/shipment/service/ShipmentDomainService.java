package com.yhs.inventroysystem.domain.shipment.service;

import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.shipment.entity.Shipment;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentType;
import com.yhs.inventroysystem.domain.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ShipmentDomainService {

    private final ShipmentRepository shipmentRepository;


    // ========== 조회 메서드 ==========

    /**
     * ID로 Shipment 조회
     */
    public Shipment getShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> ResourceNotFoundException.shipment(shipmentId));
    }

    /**
     * Invoice 번호 존재 여부 확인
     */
    public boolean existsByInvoiceNumber(String invoiceNumber) {
        return shipmentRepository.existsByInvoiceNumber(invoiceNumber);
    }

    /**
     * 기간별 선적 목록 조회
     */
    public List<Shipment> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return shipmentRepository.findByInvoiceDateBetween(startDate, endDate);
    }
    /**
     * 기간별 선적 목록 조회
     */
    public List<Shipment> findByYear(Integer year) {
        return shipmentRepository.findByYearOrderBySequenceDesc(year);
    }

    /**
     * 연도의 최대 시퀀스 조회
     */
    public Optional<Integer> findMaxSequenceByYear(int year) {
        return shipmentRepository.findMaxSequenceByYear(year);
    }

    /**
     * 전체 선적 목록 조회
     */
    public List<Shipment> findAll() {
        return shipmentRepository.findAll();
    }

    // ========== 저장 메서드 ==========

    /**
     * Shipment 저장
     */
    @Transactional
    public Shipment save(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }

    /**
     * Shipment 삭제
     */
    @Transactional
    public void delete(Shipment shipment) {
        shipmentRepository.delete(shipment);
    }

    // ========== 비즈니스 로직 메서드 ==========

    /**
     * Invoice 번호 생성
     */
    public String generateInvoiceNumber(int year, int sequence) {
        return String.format("SOL-INV-%d-%04d", year, sequence);
    }

    /**
     * 다음 시퀀스 번호 조회
     */
    public int getNextSequence(int year) {
        return findMaxSequenceByYear(year)
                .map(seq -> seq + 1)
                .orElse(1);
    }

    /**
     * Invoice 번호 중복 검증
     */
    public void validateInvoiceNumberUniqueness(String invoiceNumber) {
        if (existsByInvoiceNumber(invoiceNumber)) {
            throw new IllegalArgumentException("이미 존재하는 Invoice 번호입니다: " + invoiceNumber);
        }
    }

    public Page<Shipment> searchByKeywordAndType(String keyword, ShipmentType shipmentType, Pageable pageable) {
        return shipmentRepository.searchByKeywordAndType(keyword, shipmentType, pageable);
    }

    public Page<Shipment> searchByKeyword(String keyword, Pageable pageable) {
        return shipmentRepository.searchByKeyword(keyword, pageable);
    }

    public Page<Shipment> findAllByType(ShipmentType shipmentType, Pageable pageable) {
        return shipmentRepository.findAllByType(shipmentType, pageable);
    }

    public Page<Shipment> findAllPaged(Pageable pageable) {
        return shipmentRepository.findAllPaged(pageable);
    }
}
