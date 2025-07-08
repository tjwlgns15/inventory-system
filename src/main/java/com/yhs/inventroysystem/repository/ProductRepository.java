package com.yhs.inventroysystem.repository;

import com.yhs.inventroysystem.entity.Product;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.name = :name AND p.deletedAt IS NULL")
    boolean existsByName(String name);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    List<Product> findAll();

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Product> findById(Long id);
}