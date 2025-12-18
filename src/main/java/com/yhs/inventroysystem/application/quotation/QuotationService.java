package com.yhs.inventroysystem.application.quotation;

import com.yhs.inventroysystem.domain.quotation.entity.Quotation;
import com.yhs.inventroysystem.domain.quotation.entity.QuotationDocument;
import com.yhs.inventroysystem.domain.quotation.entity.QuotationItem;
import com.yhs.inventroysystem.domain.quotation.entity.QuotationType;
import com.yhs.inventroysystem.domain.quotation.service.QuotationDomainService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageFactory;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageType;
import com.yhs.inventroysystem.infrastructure.pagenation.PageableUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.yhs.inventroysystem.application.quotation.QuotationCommands.*;

@Service
@Transactional(readOnly = true)
@Slf4j
public class QuotationService {

    private final QuotationDomainService quotationDomainService;

    private final FileStorageService fileStorageService;

    public QuotationService(QuotationDomainService quotationDomainService,
                            FileStorageFactory fileStorageFactory) {
        this.quotationDomainService = quotationDomainService;
        this.fileStorageService = fileStorageFactory.getStorageService(FileStorageType.QUOTATION_DOCUMENT);
    }
    private static final String QUOTATION_RECEIPT_PREFIX = "SOLM-RECEIPT-";
    private static final String QUOTATION_ISSUANCE_PREFIX = "SOLM-ISSUANCE-";


    @Transactional
    public void createQuotation(CreateCommand command) {
        String quotationNumber = generateQuotationNumber(command.orderedAt(), command.quotationType());

        Quotation quotation = quotationDomainService.createQuotation(
                quotationNumber,
                command.quotationType(),
                command.companyName(),
                command.representativeName(),
                command.isTax(),
                command.currency(),
                command.note(),
                command.orderedAt()
        );

        List<QuotationItem> items = getItems(command.items(), quotation);
        quotation.addItems(items);
    }

    public Page<Quotation> searchQuotations(String keyword, QuotationType quotationType, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);

        Page<Quotation> quotationPage;
        if (quotationType != null) {
            quotationPage =  quotationDomainService.searchByKeywordAndType(keyword, quotationType, pageable);
        } else {
            quotationPage = quotationDomainService.searchByKeyword(keyword, pageable);
        }

        quotationPage.getContent().forEach(quotation ->
                quotation.getItems().size()
        );

        return quotationPage;
    }

    public Page<Quotation> findAllQuotationsPaged(QuotationType quotationType, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);

        Page<Quotation> quotationPage;
        if (quotationType != null) {
            quotationPage = quotationDomainService.findAllByType(quotationType, pageable);
        } else {
            quotationPage = quotationDomainService.findAllPaged(pageable);
        }
        quotationPage.getContent().forEach(quotation ->
                quotation.getItems().size()
        );

        return quotationPage;
    }

    public Quotation findQuotationById(Long quotationId) {
        return quotationDomainService.findByIdWithItems(quotationId);
    }

    @Transactional
    public Quotation updateQuotation(Long quotationId, UpdateCommand command) {
        Quotation quotation = quotationDomainService.findByIdWithItems(quotationId);

        quotation.update(
                command.quotationType(),
                command.companyName(),
                command.representativeName(),
                command.isTax(),
                command.currency(),
                command.note(),
                command.orderedAt()
        );

        quotation.clearItems();

        List<QuotationItem> newItems = getItems(command.items(), quotation);
        quotation.addItems(newItems);

        return quotation;
    }


    @Transactional
    public void deleteQuotation(Long quotationId) {
        Quotation quotation = quotationDomainService.findByIdWithItems(quotationId);
        List<QuotationDocument> documents = quotation.getDocuments();

        for (QuotationDocument document : documents) {
            fileStorageService.delete(document.getFilePath());
        }

        quotation.markAsDeleted();
    }

    private static List<QuotationItem> getItems(List<ItemCommand> itemCommands, Quotation quotation) {
        return itemCommands.stream()
                .map(item -> new QuotationItem(
                        quotation,
                        item.productName(),
                        item.quantity(),
                        item.unitPrice()
                ))
                .toList();
    }


    private String generateQuotationNumber(LocalDate orderedAt, QuotationType type) {
        String year = orderedAt.format(DateTimeFormatter.ofPattern("yyyy"));
        String prefix = type == QuotationType.RECEIPT ? QUOTATION_RECEIPT_PREFIX : QUOTATION_ISSUANCE_PREFIX;
        prefix = prefix + year;

        synchronized (this) {
            Integer lastSequence = quotationDomainService.findLastSequenceByYearAndType(prefix, year);
            int nextSequence = (lastSequence == null) ? 1 : lastSequence + 1;

            String quotationNumber = String.format("%s-%04d", prefix, nextSequence);

            // 중복 체크 (만약을 위해)
            while (quotationDomainService.existsByQuotationNumber(quotationNumber)) {
                nextSequence++;
                quotationNumber = String.format("%s-%04d", prefix, nextSequence);
            }

            return quotationNumber;
        }
    }
}
