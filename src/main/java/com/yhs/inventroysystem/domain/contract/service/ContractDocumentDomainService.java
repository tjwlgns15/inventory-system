package com.yhs.inventroysystem.domain.contract.service;

import com.yhs.inventroysystem.domain.contract.entity.Contract;
import com.yhs.inventroysystem.domain.contract.entity.ContractDocument;
import com.yhs.inventroysystem.domain.contract.repository.ContractDocumentRepository;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ContractDocumentDomainService {

    private final ContractDocumentRepository contractDocumentRepository;


    @Transactional
    public ContractDocument createDocument(Contract contract,
                                           String originalFileName, String storedFileName,
                                           String filePath, Long fileSize,
                                           String contentType, String description) {
        validateFileInfo(originalFileName, storedFileName, filePath, fileSize);
        ContractDocument contractDocument = new ContractDocument(contract, originalFileName, storedFileName, filePath, fileSize, contentType);

        if (description != null && !description.trim().isEmpty()) {
            contractDocument.updateDescription(description);
        }

        return contractDocumentRepository.save(contractDocument);
    }

    public ContractDocument findById(Long documentId) {
        return contractDocumentRepository.findById(documentId)
                .orElseThrow(() -> ResourceNotFoundException.document(documentId));
    }

    public List<ContractDocument> findByContractId(Long contractId) {
        return contractDocumentRepository.findByContractId(contractId);
    }

    @Transactional
    public void deleteById(Long documentId) {
        contractDocumentRepository.deleteById(documentId);
    }

    private void validateFileInfo(String originalFileName, String storedFileName,
                                  String filePath, Long fileSize) {
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("원본 파일명은 필수입니다.");
        }
        if (storedFileName == null || storedFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("저장된 파일명은 필수입니다.");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("파일 경로는 필수입니다.");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("파일 크기는 0보다 커야 합니다.");
        }
    }
}
