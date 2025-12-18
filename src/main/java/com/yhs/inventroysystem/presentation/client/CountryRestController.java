package com.yhs.inventroysystem.presentation.client;

import com.yhs.inventroysystem.application.client.CountryService;
import com.yhs.inventroysystem.domain.client.entity.Country;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.yhs.inventroysystem.presentation.client.CountryDtos.CountryResponse;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryRestController {

    private final CountryService countryService;

    @GetMapping
    public ResponseEntity<List<CountryResponse>> getAllCountries() {
        List<Country> countries = countryService.findAllCountry();
        List<CountryResponse> responses = countries.stream()
                .map(CountryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}