package com.yhs.inventroysystem.application.contract;

import com.yhs.inventroysystem.domain.contract.entity.Contract;
import com.yhs.inventroysystem.domain.contract.entity.ContractDocument;
import com.yhs.inventroysystem.domain.contract.service.ContractDomainService;
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
import java.util.List;

import static com.yhs.inventroysystem.application.contract.ContractCommands.*;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ContractService {

    private final ContractDomainService contractDomainService;
    private final FileStorageService fileStorageService;

    public ContractService(ContractDomainService contractDomainService,
                           FileStorageFactory fileStorageFactory) {
        this.contractDomainService = contractDomainService;
        this.fileStorageService = fileStorageFactory.getStorageService(FileStorageType.CONTRACT_DOCUMENT);
    }


    @Transactional
    public void createContract(CreateCommand command) {
        contractDomainService.createContract(
                command.recordDate(),
                command.documentName(),
                command.companyName(),
                command.contractDate(),
                command.validityStartDate(),
                command.validityEndDate(),
                command.signedDate(),
                command.signerName(),
                command.note()
        );
    }

    public Page<Contract> searchContracts(String keyword, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);
        return contractDomainService.searchByKeyword(keyword, pageable);
    }

    public Page<Contract> findAllContractsPaged(int page, int size, String sortBy, String direction) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, direction);
        return contractDomainService.findAllPaged(pageable);
    }

    public Contract findContractById(Long contractId) {
        return contractDomainService.findByIdWithDocuments(contractId);
    }

    public List<Contract> findContractsByPeriod(LocalDate startDate, LocalDate endDate) {
        return contractDomainService.findContractsByPeriod(startDate, endDate);
    }

    @Transactional
    public Contract updateContract(Long contractId, UpdateCommand command) {
        Contract contract = contractDomainService.findByIdWithDocuments(contractId);

        contract.updateInfo(
                command.recordDate(),
                command.documentName(),
                command.companyName(),
                command.contractDate(),
                command.validityStartDate(),
                command.validityEndDate(),
                command.signedDate(),
                command.signerName(),
                command.note()
        );

        return contract;
    }

    @Transactional
    public void deleteContract(Long contractId) {
        Contract contract = contractDomainService.findByIdWithDocuments(contractId);
        List<ContractDocument> documents = contract.getDocuments();

        for (ContractDocument document : documents) {
            fileStorageService.delete(document.getFilePath());
        }

        contract.markAsDeleted();
    }
}