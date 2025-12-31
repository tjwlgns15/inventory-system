package com.yhs.inventroysystem.application.shipment;

import com.yhs.inventroysystem.application.shipment.ShipmentBoxCommand.ShipmentBoxUpdateCommand;
import com.yhs.inventroysystem.domain.shipment.entity.ShipmentBox;
import com.yhs.inventroysystem.domain.shipment.service.ShipmentBoxDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 박스 템플릿 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ShipmentBoxService {

    private final ShipmentBoxDomainService shipmentBoxDomainService;


    /**
     * 박스 템플릿 생성
     */
    @Transactional
    public ShipmentBox createBoxTemplate(ShipmentBoxCommand.ShipmentBoxCreateCommand command) {
        log.info("Creating box template: {}", command.title());

        ShipmentBox template = ShipmentBox.create(
                command.title(),
                command.width(),
                command.length(),
                command.height(),
                command.weight()
        );

        ShipmentBox savedTemplate = shipmentBoxDomainService.save(template);
        log.info("Box template created successfully: {}", savedTemplate.getId());

        return savedTemplate;
    }

    /**
     * 박스 템플릿 조회
     */
    public ShipmentBox findBoxTemplate(Long templateId) {
        return shipmentBoxDomainService.getShipmentBoxActivate(templateId);
    }

    /**
     * 활성화된 박스 템플릿 목록 조회
     */
    public List<ShipmentBox> findActiveBoxTemplates() {
        return shipmentBoxDomainService.findByIsActiveTrue();
    }

    /**
     * 전체 박스 템플릿 목록 조회
     */
    public List<ShipmentBox> findAllBoxTemplates() {
        return shipmentBoxDomainService.findAll();
    }

    /**
     * 박스 템플릿 수정
     */
    @Transactional
    public ShipmentBox updateBoxTemplate(Long templateId, ShipmentBoxUpdateCommand command) {
        log.info("Updating box template: {}", templateId);

        ShipmentBox template = findBoxTemplate(templateId);
        template.update(command.title(), command.width(), command.length(), command.height(), command.weight());

        log.info("Box template updated successfully: {}", templateId);
        return template;
    }

    /**
     * 박스 템플릿 활성화
     */
    @Transactional
    public ShipmentBox activateBoxTemplate(Long templateId) {
        log.info("Activating box template: {}", templateId);

        ShipmentBox template = shipmentBoxDomainService.getShipmentBox(templateId);
        template.activate();

        log.info("Box template activated: {}", templateId);
        return template;
    }

    /**
     * 박스 템플릿 비활성화
     */
    @Transactional
    public ShipmentBox deactivateBoxTemplate(Long templateId) {
        log.info("Deactivating box template: {}", templateId);

        ShipmentBox template = findBoxTemplate(templateId);
        template.deactivate();

        log.info("Box template deactivated: {}", templateId);
        return template;
    }

    /**
     * 박스 템플릿 삭제
     */
    @Transactional
    public void deleteBoxTemplate(Long templateId) {
        log.info("Deleting box template: {}", templateId);

        ShipmentBox template = findBoxTemplate(templateId);
        shipmentBoxDomainService.delete(template);

        log.info("Box template deleted successfully: {}", templateId);
    }
}
