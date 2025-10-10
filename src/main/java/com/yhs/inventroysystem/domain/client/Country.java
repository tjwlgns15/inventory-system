package com.yhs.inventroysystem.domain.client;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "countries")
@Getter
@NoArgsConstructor
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // KR, US, JP 등

    @Column(nullable = false)
    private String name; // 대한민국, 미국, 일본 등

    private String englishName; // South Korea, United States, Japan 등

    public Country(String code, String name, String englishName) {
        this.code = code;
        this.name = name;
        this.englishName = englishName;
    }
}