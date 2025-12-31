package com.yhs.inventroysystem.domain.carrier.repository;

import com.yhs.inventroysystem.domain.carrier.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Long> {
}
