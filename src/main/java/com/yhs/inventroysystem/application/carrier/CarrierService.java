package com.yhs.inventroysystem.application.carrier;

import com.yhs.inventroysystem.domain.carrier.entity.Carrier;
import com.yhs.inventroysystem.domain.carrier.service.CarrierCommand;
import com.yhs.inventroysystem.domain.carrier.service.CarrierDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yhs.inventroysystem.domain.carrier.service.CarrierCommand.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
@Slf4j
public class CarrierService {

    private final CarrierDomainService carrierDomainService;

    @Transactional
    public Carrier registerCarrier(CarrierRegisterCommand command) {
        Carrier carrier = carrierDomainService.saveCarrier(
                command.name(),
                command.nameEn(),
                command.contactNumber(),
                command.email(),
                command.notes()
        );

        log.info("운송 업체가 등록되었습니다. 업체명: {}", carrier.getName());
        return carrier;
    }

    @Transactional
    public Carrier updateCarrier(Long carrierId, CarrierUpdateCommand command) {
        Carrier carrier = carrierDomainService.updateCarrier(
                carrierId,
                command.name(),
                command.nameEn(),
                command.contactNumber(),
                command.email(),
                command.notes()
        );
        log.info("운송 업체 정보가 수정되었습니다. 업체명: {}", carrier.getName());

        return carrier;
    }

    public Carrier findCarrier(Long carrierId) {
        return carrierDomainService.findById(carrierId);
    }

    public List<Carrier> findAllCarrier() {
        return carrierDomainService.findAll();
    }
}
