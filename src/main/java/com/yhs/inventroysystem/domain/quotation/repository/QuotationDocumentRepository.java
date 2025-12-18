package com.yhs.inventroysystem.domain.quotation.repository;

import com.yhs.inventroysystem.domain.quotation.entity.QuotationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationDocumentRepository extends JpaRepository<QuotationDocument, Long> {

    @Query("SELECT q FROM QuotationDocument q " +
            "WHERE q.quotation.id = :quotationId " +
            "ORDER BY q.createdAt DESC")
    List<QuotationDocument> findByQuotationId(@Param("quotationId") Long quotationId);
}
