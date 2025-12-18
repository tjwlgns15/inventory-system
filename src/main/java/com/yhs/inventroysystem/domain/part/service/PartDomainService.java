package com.yhs.inventroysystem.domain.part.service;


import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.part.entity.Part;
import com.yhs.inventroysystem.domain.part.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PartDomainService {

    private final PartRepository partRepository;

    @Transactional
    public Part savePart(Part newPart) {
        return partRepository.save(newPart);
    }

    public Part findById(Long partId) {
        return partRepository.findByIdAndNotDeleted(partId)
                .orElseThrow(() -> ResourceNotFoundException.part(partId));
    }

    public List<Part> findAllActive(Sort sort) {
        return partRepository.findAllActive(sort);
    }

    public Page<Part> searchByKeyword(String keyword, Pageable pageable) {
        return partRepository.searchByKeyword(keyword, pageable);
    }

    public Page<Part> findAllActive(Pageable pageable) {
        return partRepository.findAllActive(pageable);
    }

    public void validatePartCodeDuplication(String partCode) {
        if (partRepository.existsByPartCodeAndNotDeleted(partCode)) {
            throw DuplicateResourceException.partCode(partCode);
        }
    }
}
