package com.yhs.inventroysystem.repository;

import com.yhs.inventroysystem.entity.ProductPart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductPartRepository extends JpaRepository<ProductPart, Long> {
}