package com.yhs.inventroysystem.domain.contract.repository;

import com.yhs.inventroysystem.domain.contract.entity.ContractDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractDocumentRepository extends JpaRepository<ContractDocument, Long> {

    @Query("SELECT c FROM ContractDocument c " +
            "WHERE c.contract.id = :contractId " +
            "ORDER BY c.createdAt DESC")
    List<ContractDocument> findByContractId(@Param("contractId") Long contractId);
}