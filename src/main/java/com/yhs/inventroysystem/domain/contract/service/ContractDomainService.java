package com.yhs.inventroysystem.domain.contract.service;

import com.yhs.inventroysystem.domain.contract.entity.Contract;
import com.yhs.inventroysystem.domain.contract.repository.ContractRepository;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ContractDomainService {

    private final ContractRepository contractRepository;

    @Transactional
    public Contract createContract(LocalDate recordDate,
                                   String documentName,
                                   String companyName,
                                   LocalDate contractDate,
                                   LocalDate validityStartDate,
                                   LocalDate validityEndDate,
                                   LocalDate signedDate,
                                   String signerName,
                                   String note) {

        Contract contract = Contract.create(
                recordDate,
                documentName,
                companyName,
                contractDate,
                validityStartDate,
                validityEndDate,
                signedDate,
                signerName,
                note
        );

        return contractRepository.save(contract);
    }

    public Contract findById(Long contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> ResourceNotFoundException.contract(contractId));
    }

    public Contract findByIdWithDocuments(Long contractId) {
        return contractRepository.findByIdWithDocuments(contractId)
                .orElseThrow(() -> ResourceNotFoundException.contract(contractId));
    }

    public List<Contract> findContractsByPeriod(LocalDate startDate, LocalDate endDate) {
        return contractRepository.findContractsByPeriod(startDate, endDate);
    }

    public Page<Contract> searchByKeyword(String keyword, Pageable pageable) {
        return contractRepository.searchByKeyword(keyword, pageable);
    }

    public Page<Contract> findAllPaged(Pageable pageable) {
        return contractRepository.findAllPaged(pageable);
    }
}