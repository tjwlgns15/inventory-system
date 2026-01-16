package com.yhs.inventroysystem.application.shipment;

import com.yhs.inventroysystem.domain.carrier.entity.Carrier;
import com.yhs.inventroysystem.domain.carrier.service.CarrierDomainService;
import com.yhs.inventroysystem.domain.shipment.entity.*;
import com.yhs.inventroysystem.domain.shipment.service.ShipmentBoxDomainService;
import com.yhs.inventroysystem.domain.shipment.service.ShipmentDomainService;
import com.yhs.inventroysystem.infrastructure.pagenation.PageableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.yhs.inventroysystem.application.shipment.ShipmentCommand.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ShipmentService {

    private final ShipmentDomainService shipmentDomainService;
    private final ShipmentBoxDomainService shipmentBoxDomainService;
    private final CarrierDomainService carrierDomainService;

    /**
     * 선적 생성
     */
    @Transactional
    public Shipment createShipment(ShipmentCreateCommand command) {
        log.info("Creating shipment for client: {}", command.clientId());

        // Invoice 번호 생성
        int year = command.invoiceDate().getYear();
        int sequence = shipmentDomainService.getNextSequence(year);
        String invoiceNumber = shipmentDomainService.generateInvoiceNumber(year, sequence);

        // Invoice 번호 중복 확인
        shipmentDomainService.validateInvoiceNumberUniqueness(invoiceNumber);

        // Carrier 조회
        Carrier carrier = findCarrier(command.carrierId());

        // Shipment 엔티티 생성
        Shipment shipment = Shipment.create(
                invoiceNumber,
                command.invoiceDate(),
                year,
                sequence,
                command.shipperCompanyName(),
                command.shipperAddress(),
                command.shipperContactPerson(),
                command.shipperPhone(),
                command.clientId(),
                command.soldToCompanyName(),
                command.soldToAddress(),
                command.soldToContactPerson(),
                command.soldToPhone(),
                command.shipToCompanyName(),
                command.shipToAddress(),
                command.shipToContactPerson(),
                command.shipToPhone(),
                command.portOfLoading(),
                command.finalDestination(),
                carrier,
                command.carrierName(),
                command.freightDate(),
                command.trackingNumber(),
                command.exportLicenseNumber(),
                command.lcNo(),
                command.lcDate(),
                command.lcIssuingBank(),
                command.shipmentType(),
                command.tradeTerms(),
                command.originDescription(),
                command.additionalRemarks(),
                command.currency()
        );

        // 박스 정보 추가
        if (command.boxItems() != null) {
            command.boxItems().forEach(boxItemCmd -> {
                ShipmentBoxItem boxItem = createBoxItem(boxItemCmd);
                shipment.addBoxItem(boxItem);
            });
        }

        // 제품 정보 추가
        command.items().forEach(itemCmd -> {
            ShipmentItem item = ShipmentItem.create(
                    itemCmd.sequence(),
                    itemCmd.productId(),
                    itemCmd.productCode(),
                    itemCmd.productDescription(),
                    itemCmd.hsCode(),
                    itemCmd.unit(),
                    itemCmd.quantity(),
                    itemCmd.unitPrice(),
                    itemCmd.netWeight(),      // 개별 제품 순중량
                    itemCmd.grossWeight(),    // 개별 제품 총중량
                    itemCmd.cbm()             // 개별 제품 CBM
            );
            shipment.addItem(item);
        });

        // 저장
        Shipment savedShipment = shipmentDomainService.save(shipment);
        log.info("Shipment created successfully: {}", savedShipment.getInvoiceNumber());

        return savedShipment;
    }

    /**
     * 선적 조회
     */
    public Shipment findShipment(Long shipmentId) {
        Shipment shipment = shipmentDomainService.getShipment(shipmentId);

        // 패치조인 없이 배치사이즈 사용(Lazy Loading 트리거)
        shipment.getBoxItems().size();
        shipment.getItems().size();
        return shipment;
    }

    /**
     * 선적 목록 조회
     */
    public List<Shipment> findAllShipments() {
        return shipmentDomainService.findAll();
    }

    public Page<Shipment> searchShipments(String keyword, ShipmentType shipmentType, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);

        Page<Shipment> shipmentPage;
        if (shipmentType != null) {
            shipmentPage = shipmentDomainService.searchByKeywordAndType(keyword, shipmentType, pageable);
        } else {
            shipmentPage = shipmentDomainService.searchByKeyword(keyword, pageable);
        }

        // 명시적으로 items 컬렉션 로딩 (배치사이즈 활용)
        shipmentPage.getContent().forEach(shipment -> {
            shipment.getItems().size();
        });

        return shipmentPage;
    }

    public Page<Shipment> findAllShipmentsPaged(ShipmentType shipmentType, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);

        Page<Shipment> shipmentPage;
        if (shipmentType != null) {
            shipmentPage = shipmentDomainService.findAllByType(shipmentType, pageable);
        } else {
            shipmentPage = shipmentDomainService.findAllPaged(pageable);
        }

        // 명시적으로 items 컬렉션 로딩 (배치사이즈 활용)
        shipmentPage.getContent().forEach(shipment -> {
            shipment.getItems().size();
        });

        return shipmentPage;
    }

    /**
     * 기간별 선적 목록 조회
     */
    public List<Shipment> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return shipmentDomainService.findByDateRange(startDate, endDate);
    }

    /**
     * 연도별 선적 목록 조회
     */
    public List<Shipment> findByYear(Integer year) {
        return shipmentDomainService.findByYear(year);
    }

    /**
     * 선적 수정
     */
    @Transactional
    public Shipment updateShipment(Long shipmentId, ShipmentUpdateCommand command) {
        log.info("Updating shipment: {}", shipmentId);

        Shipment shipment = shipmentDomainService.getShipment(shipmentId);

        // Carrier 조회
        Carrier carrier = findCarrier(command.carrierId());

        // 기본 정보 업데이트
        shipment.updateInvoiceInfo(command.invoiceDate(), command.freightDate());
        shipment.updateShipperInfo(
                command.shipperCompanyName(),
                command.shipperAddress(),
                command.shipperContactPerson(),
                command.shipperPhone()
        );
        shipment.updateSoldToInfo(
                command.soldToCompanyName(),
                command.soldToAddress(),
                command.soldToContactPerson(),
                command.soldToPhone()
        );
        shipment.updateShipToInfo(
                command.shipToCompanyName(),
                command.shipToAddress(),
                command.shipToContactPerson(),
                command.shipToPhone()
        );
        shipment.updateShippingInfo(command.portOfLoading(),
                command.finalDestination(),
                command.trackingNumber(),
                command.exportLicenseNumber()
        );
        shipment.assignCarrier(carrier, command.carrierName());
        shipment.updateRemarks(
                command.shipmentType(),
                command.tradeTerms(),
                command.originDescription(),
                command.additionalRemarks()
        );
        shipment.updateLcInfo(command.lcNo(), command.lcDate(), command.lcIssuingBank());

        // 박스 정보 업데이트
        shipment.clearBoxItems();
        if (command.boxItems() != null) {
            command.boxItems().forEach(boxItemCmd -> {
                ShipmentBoxItem boxItem = createBoxItem(boxItemCmd);
                shipment.addBoxItem(boxItem);
            });
        }

        // 제품 정보 업데이트
        shipment.clearItems();
        command.items().forEach(itemCmd -> {
            ShipmentItem item = ShipmentItem.create(
                    itemCmd.sequence(),
                    itemCmd.productId(),
                    itemCmd.productCode(),
                    itemCmd.productDescription(),
                    itemCmd.hsCode(),
                    itemCmd.unit(),
                    itemCmd.quantity(),
                    itemCmd.unitPrice(),
                    itemCmd.netWeight(),      // 개별 제품 순중량
                    itemCmd.grossWeight(),    // 개별 제품 총중량
                    itemCmd.cbm()             // 개별 제품 CBM
            );
            shipment.addItem(item);
        });

        log.info("Shipment updated successfully: {}", shipment.getInvoiceNumber());
        return shipment;
    }

    @Transactional
    public Shipment updateMemo(Long shipmentId, String newMemo) {
        Shipment shipment = shipmentDomainService.getShipment(shipmentId);

        shipment.updateMemo(newMemo);
        return shipment;
    }
    /**
     * 선적 삭제
     */
    @Transactional
    public void deleteShipment(Long shipmentId) {
        log.info("Deleting shipment: {}", shipmentId);

        Shipment shipment = shipmentDomainService.getShipment(shipmentId);
        shipment.markAsDeleted();

        log.info("Shipment deleted successfully: {}", shipment.getInvoiceNumber());
    }


    // ========== Private Helper Methods ==========

    /**
     * BoxItem 생성 (템플릿 선택 OR 직접 입력)
     */
    private ShipmentBoxItem createBoxItem(ShipmentBoxItemCommand command) {
        if (command.isFromTemplate()) {
            // 템플릿에서 선택
            ShipmentBox template = shipmentBoxDomainService.getShipmentBoxActivate(command.boxTemplateId());

            // 사이즈 커스터마이징 여부 확인
            boolean isCustomized = command.width() != null || command.length() != null || command.height() != null;

            if (isCustomized) {
                // 템플릿 기반 + 사이즈 커스터마이징
                return ShipmentBoxItem.createFromTemplateWithCustomSize(
                        command.sequence(),
                        template,
                        command.width() != null ? command.width() : template.getWidth(),
                        command.length() != null ? command.length() : template.getLength(),
                        command.height() != null ? command.height() : template.getHeight(),
                        command.quantity()
                );
            } else {
                // 템플릿 그대로 사용
                return ShipmentBoxItem.createFromTemplate(
                        command.sequence(),
                        template,
                        command.quantity()
                );
            }
        } else {
            // 직접 입력
            return ShipmentBoxItem.createDirect(
                    command.sequence(),
                    command.title(),
                    command.width(),
                    command.length(),
                    command.height(),
                    command.quantity()
            );
        }
    }

    /**
     * Carrier 조회
     */
    private Carrier findCarrier(Long carrierId) {
        if (carrierId == null) {
            return null;
        }
        return carrierDomainService.findById(carrierId);
    }
}