package com.yhs.inventroysystem.domain.carrier.entity;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carriers")
@Getter
@NoArgsConstructor
public class Carrier extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String nameEn;

    @Column(length = 50)
    private String contactNumber;

    @Column(length = 100)
    private String email;

    @Column(length = 500)
    private String notes;

    public Carrier(String name, String nameEn, String contactNumber, String email, String notes) {
        this.name = name;
        this.nameEn = nameEn;
        this.contactNumber = contactNumber;
        this.email = email;
        this.notes = notes;
    }

    public void update(String name, String nameEn, String contactNumber, String email, String notes) {
        this.name = name;
        this.nameEn = nameEn;
        this.contactNumber = contactNumber;
        this.email = email;
        this.notes = notes;
    }
}
