package com.yhs.inventroysystem.domain.price;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientProductPriceRepository extends JpaRepository<ClientProductPrice, Long> {

    @Query("SELECT cpp FROM ClientProductPrice cpp " +
            "JOIN FETCH cpp.client c " +
            "LEFT JOIN FETCH c.parentClient " +
            "LEFT JOIN FETCH c.country " +
            "JOIN FETCH cpp.product " +
            "WHERE cpp.client.id = :clientId")
    List<ClientProductPrice> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT cpp FROM ClientProductPrice cpp " +
            "JOIN FETCH cpp.client c " +
            "LEFT JOIN FETCH c.parentClient " +
            "LEFT JOIN FETCH c.country " +
            "JOIN FETCH cpp.product")
    List<ClientProductPrice> findAllWithClientAndProduct();

    @Query("SELECT cpp FROM ClientProductPrice cpp " +
            "JOIN FETCH cpp.client c " +
            "LEFT JOIN FETCH c.parentClient " +
            "LEFT JOIN FETCH c.country " +
            "JOIN FETCH cpp.product " +
            "WHERE cpp.client.id = :clientId AND cpp.product.id = :productId")
    Optional<ClientProductPrice> findByClientIdAndProductId(@Param("clientId") Long clientId, @Param("productId") Long productId);

    boolean existsByClientIdAndProductId(Long clientId, Long productId);
}
