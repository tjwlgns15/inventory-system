package com.yhs.inventroysystem.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);

    @Query("SELECT p FROM Product p JOIN FETCH p.partMappings pm JOIN FETCH pm.part WHERE p.id = :productId")
    Optional<Product> findByIdWithParts(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p JOIN FETCH p.partMappings pm JOIN FETCH pm.part")
    List<Product> findAllWithPart();
}