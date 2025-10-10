package com.yhs.inventroysystem.domain.client;

import com.yhs.inventroysystem.domain.exchange.Currency;
import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clients")
@Getter
@NoArgsConstructor
public class Client extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String clientCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(nullable = false)
    private String name;

    private String address;

    private String contactNumber;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    public Client(String clientCode, Country country, String name, String address, String contactNumber, String email, Currency currency) {
        this.clientCode = clientCode;
        this.country = country;
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.currency = currency;
    }

    public void updateInfo(String name, Country country, String address, String contactNumber, String email, Currency currency) {
        this.name = name;
        this.country = country;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.currency = currency;
    }
}

