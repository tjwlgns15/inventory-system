package com.yhs.inventroysystem.domain.delivery.service;

import com.yhs.inventroysystem.domain.delivery.entity.Delivery;
import com.yhs.inventroysystem.domain.delivery.entity.DeliveryStatus;
import com.yhs.inventroysystem.domain.delivery.repository.DeliveryRepository;
import com.yhs.inventroysystem.domain.exception.InvalidDeliveryStateException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DeliveryDomainService {

    private final DeliveryRepository deliveryRepository;

    @Transactional
    public Delivery saveDelivery(Delivery delivery) {
        return deliveryRepository.save(delivery);
    }

    public Page<Delivery> searchByKeyword(String keyword, Pageable pageable) {
        return deliveryRepository.searchByKeyword(keyword, pageable);
    }

    public Page<Delivery> findAllPaged(Pageable pageable) {
        return deliveryRepository.findAllPaged(pageable);
    }

    public Delivery findById(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> ResourceNotFoundException.delivery(deliveryId));
    }

    public Delivery findByIdWithItems(Long deliveryId) {
        return deliveryRepository.findByIdWithItems(deliveryId)
                .orElseThrow(() -> ResourceNotFoundException.delivery(deliveryId));
    }

    public void validateDeliveryCompleted(Delivery delivery) {
        if (!delivery.getStatus().equals(DeliveryStatus.COMPLETED)) {
            throw new InvalidDeliveryStateException(delivery.getStatus(), "납품 취소");
        }
    }

    public Integer findLastSequenceByYear(String year) {
        return deliveryRepository.findLastSequenceByYear(year);
    }

    public boolean existsByDeliveryNumber(String deliveryNumber) {
        return deliveryRepository.existsByDeliveryNumber(deliveryNumber);
    }

    public List<Delivery> findCompletedDeliveriesByPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return deliveryRepository.findCompletedDeliveriesByPeriod(startDateTime, endDateTime);
    }

    public List<Delivery> findWeeklySales(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return deliveryRepository.findWeeklySales(startDateTime, endDateTime);
    }

    public List<Delivery> findCompletedDeliveriesByYear(int year) {
        return deliveryRepository.findCompletedDeliveriesByYear(year);
    }


}
