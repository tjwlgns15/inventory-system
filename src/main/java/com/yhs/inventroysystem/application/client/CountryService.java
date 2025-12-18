package com.yhs.inventroysystem.application.client;

import com.yhs.inventroysystem.domain.client.entity.Country;
import com.yhs.inventroysystem.domain.client.service.CountryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CountryService {

    private final CountryDomainService countryDomainService;


    public List<Country> findAllCountry() {
        return countryDomainService.findAll();
    }
}
