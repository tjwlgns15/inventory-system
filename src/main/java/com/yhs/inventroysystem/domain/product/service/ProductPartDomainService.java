package com.yhs.inventroysystem.domain.product.service;

import com.yhs.inventroysystem.domain.part.entity.Part;
import com.yhs.inventroysystem.domain.product.repository.ProductPartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serial;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductPartDomainService {

    private final ProductPartRepository productPartRepository;

    public boolean existsByPart(Part part) {
        return productPartRepository.existsByPart(part);
    }

    public long countProductsByPart(Part part) {
        return productPartRepository.countProductsByPart(part);
    }
}
