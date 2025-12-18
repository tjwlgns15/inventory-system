package com.yhs.inventroysystem.domain.client.service;

import com.yhs.inventroysystem.domain.client.entity.Country;
import com.yhs.inventroysystem.domain.client.repository.CountryRepository;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CountryDomainService {

    private final CountryRepository countryRepository;

    public Country findById(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> ResourceNotFoundException.country(countryId));
    }

    public List<Country> findAll() {
        return countryRepository.findAll();
    }


}
