package com.yhs.inventroysystem.domain.quotation.service;

import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.exchange.entity.Currency;
import com.yhs.inventroysystem.domain.quotation.entity.Quotation;
import com.yhs.inventroysystem.domain.quotation.entity.QuotationDocument;
import com.yhs.inventroysystem.domain.quotation.entity.QuotationItem;
import com.yhs.inventroysystem.domain.quotation.entity.QuotationType;
import com.yhs.inventroysystem.domain.quotation.repository.QuotationRepository;
import com.yhs.inventroysystem.infrastructure.file.FileStorageFactory;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageType;
import com.yhs.inventroysystem.infrastructure.pagenation.PageableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.yhs.inventroysystem.application.quotation.QuotationCommands.CreateCommand;
import static com.yhs.inventroysystem.application.quotation.QuotationCommands.UpdateCommand;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class QuotationDomainService {

    private final QuotationRepository quotationRepository;

    @Transactional
    public Quotation createQuotation(String quotationNumber,
                                QuotationType quotationType,
                                String companyName,
                                String representativeName,
                                boolean isTax,
                                Currency currency,
                                String note,
                                LocalDate orderedAt) {

        Quotation quotation = new Quotation(
                quotationNumber,
                quotationType,
                companyName,
                representativeName,
                isTax,
                currency,
                note,
                orderedAt
        );

        return quotationRepository.save(quotation);
    }

    public Quotation findById(Long quotationId) {
        return quotationRepository.findById(quotationId)
                .orElseThrow(() -> ResourceNotFoundException.quotation(quotationId));
    }

    public Page<Quotation> searchByKeyword(String keyword, Pageable pageable) {
        return quotationRepository.searchByKeyword(keyword, pageable);
    }

    public Page<Quotation> findAllPaged(Pageable pageable) {
        return quotationRepository.findAllPaged(pageable);
    }

    public Quotation findByIdWithItems(Long quotationId) {
        return quotationRepository.findByIdWithItems(quotationId)
                .orElseThrow(() -> ResourceNotFoundException.quotation(quotationId));
    }

    public Integer findLastSequenceByYearAndType(String prefix, String year) {
        return quotationRepository.findLastSequenceByYearAndType(prefix, year);
    }

    public boolean existsByQuotationNumber(String quotationNumber) {
        return quotationRepository.existsByQuotationNumber(quotationNumber);
    }

    public List<Quotation> findQuotationsByPeriod(LocalDate startDate, LocalDate endDate) {
        return quotationRepository.findQuotationsByPeriod(startDate, endDate);
    }
}
