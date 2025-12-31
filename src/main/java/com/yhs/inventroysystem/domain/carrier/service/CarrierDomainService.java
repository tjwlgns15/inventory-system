package com.yhs.inventroysystem.domain.carrier.service;

import com.yhs.inventroysystem.domain.carrier.entity.Carrier;
import com.yhs.inventroysystem.domain.carrier.repository.CarrierRepository;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
@Slf4j
public class CarrierDomainService {

    private final CarrierRepository carrierRepository;

    @Transactional
    public Carrier saveCarrier(String name, String nameEn, String contactNumber, String email, String notes) {
        Carrier carrier = new Carrier(name, nameEn, contactNumber, email, notes);
        return carrierRepository.save(carrier);
    }

    @Transactional
    public Carrier updateCarrier(Long carrierId, String name, String nameEn, String contactNumber, String email, String notes) {
        Carrier carrier = findById(carrierId);
        carrier.update(name, nameEn, contactNumber, email, notes);
        return carrierRepository.save(carrier);
    }

    public Carrier findById(Long carrierId) {
        return carrierRepository.findById(carrierId)
                .orElseThrow(() -> ResourceNotFoundException.carrier(carrierId));
    }

    public List<Carrier> findAll() {
        return carrierRepository.findAll();
    }
}
