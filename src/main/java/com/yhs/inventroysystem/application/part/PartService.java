package com.yhs.inventroysystem.application.part;


import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.part.Part;
import com.yhs.inventroysystem.domain.part.PartRepository;
import com.yhs.inventroysystem.domain.part.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.yhs.inventroysystem.application.part.PartCommands.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartService {

    private final PartRepository partRepository;
    private final StockTransactionRepository stockTransactionRepository;

    @Transactional
    public Part registerPart(PartRegisterCommand command) {
        validatePartCodeDuplication(command.partCode());

        Part part = new Part(
                command.partCode(),
                command.name(),
                command.specification(),
                command.initialStock(),
                command.unit()
        );

        return partRepository.save(part);
    }

    public List<Part> findAllPart() {
        return partRepository.findAll();
    }

    @Transactional
    public Part updatePart(Long partId, PartUpdateCommand command) {
        Part part = findPartById(partId);

        part.updateInfo(
                command.partCode(),
                command.name(),
                command.specification(),
                command.unit()
        );

        return partRepository.save(part);
    }

    @Transactional
    public void increaseStock(Long partId, Integer quantity) {
        Part part = findPartById(partId);
        part.increaseStock(quantity);
    }

    @Transactional
    public void decreaseStock(Long partId, Integer quantity) {
        Part part = findPartById(partId);
        part.decreaseStock(quantity);
    }

    public Part findPartById(Long partId) {
        return partRepository.findById(partId)
                .orElseThrow(() -> ResourceNotFoundException.part(partId));
    }

    public Part findPartByCode(String partCode) {
        return partRepository.findByPartCode(partCode)
                .orElseThrow(() -> new ResourceNotFoundException("부품을 찾을 수 없습니다: " + partCode));
    }

    private void validatePartCodeDuplication(String partCode) {
        if (partRepository.existsByPartCode(partCode)) {
            throw DuplicateResourceException.partCode(partCode);
        }
    }
}
