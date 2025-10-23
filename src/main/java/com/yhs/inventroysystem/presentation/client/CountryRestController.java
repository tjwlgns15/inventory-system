package com.yhs.inventroysystem.presentation.client;

import com.yhs.inventroysystem.domain.client.Country;
import com.yhs.inventroysystem.domain.client.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yhs.inventroysystem.presentation.client.CountryDtos.*;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryRestController {

    private final CountryRepository countryRepository;

    @GetMapping
    public ResponseEntity<List<CountryResponse>> getAllCountries() {
        List<Country> countries = countryRepository.findAll();
        List<CountryResponse> responses = countries.stream()
                .map(CountryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}