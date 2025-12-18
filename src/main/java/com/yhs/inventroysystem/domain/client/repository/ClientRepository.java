package com.yhs.inventroysystem.domain.client.repository;

import com.yhs.inventroysystem.domain.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("SELECT COUNT(c) > 0 FROM Client c WHERE c.clientCode = :clientCode AND c.deletedAt IS NULL")
    boolean existsByClientCodeAndNotDeleted(String clientCode);

    @Query("SELECT c FROM Client c " +
            "LEFT JOIN FETCH c.parentClient " +
            "WHERE c.id = :clientId AND c.deletedAt IS NULL")
    Optional<Client> findByIdAndDeletedAt(Long clientId);

    @Query("SELECT c FROM Client c " +
            "WHERE c.clientCode = :clientCode AND c.deletedAt IS NULL")
    Optional<Client> findByClientCodeAndNotDeleted(String clientCode);

    @Query("SELECT c FROM Client c " +
            "JOIN FETCH c.country " +
            "WHERE c.deletedAt IS NULL " +
            "ORDER BY c.createdAt DESC")
    List<Client> findAllActiveWithCountry();

}
