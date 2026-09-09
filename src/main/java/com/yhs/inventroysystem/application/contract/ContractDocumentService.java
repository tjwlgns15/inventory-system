package com.yhs.inventroysystem.application.contract;

import com.yhs.inventroysystem.domain.contract.entity.Contract;
import com.yhs.inventroysystem.domain.contract.entity.ContractDocument;
import com.yhs.inventroysystem.domain.contract.service.ContractDocumentDomainService;
import com.yhs.inventroysystem.domain.contract.service.ContractDomainService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageFactory;
import com.yhs.inventroysystem.infrastructure.file.FileStorageService;
import com.yhs.inventroysystem.infrastructure.file.FileStorageType;
import com.yhs.inventroysystem.infrastructure.file.FileUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

import static com.yhs.inventroysystem.application.contract.ContractDocumentCommands.*;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ContractDocumentService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx",
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    );

    private final ContractDomainService contractDomainService;
    private final ContractDocumentDomainService contractDocumentDomainService;

    private final FileStorageService fileStorageService;

    public ContractDocumentService(ContractDomainService contractDomainService,
                                   ContractDocumentDomainService contractDocumentDomainService,
                                   FileStorageFactory fileStorageFactory) {
        this.contractDomainService = contractDomainService;
        this.contractDocumentDomainService = contractDocumentDomainService;
        this.fileStorageService = fileStorageFactory.getStorageService(FileStorageType.CONTRACT_DOCUMENT);
    }


    @Transactional
    public ContractDocument uploadDocument(ContractDocumentUploadCommand command) {
        Contract contract = contractDomainService.findById(command.contractId());

        MultipartFile uploadFile = command.uploadFile();
        validateDocumentFile(uploadFile);

        FileUploadResult result = fileStorageService.store(
                uploadFile,
                FileStorageType.CONTRACT_DOCUMENT.getDirectory()
        );

        ContractDocument document = contractDocumentDomainService.createDocument(
                contract,
                result.getOriginalFileName(),
                result.getStoredFileName(),
                result.getFilePath(),
                result.getFileSize(),
                uploadFile.getContentType(),
                command.description()
        );

        contract.addDocument(document);

        log.info("계약서 관련 문서 업로드 완료 - contract: {}, fileName: {}",
                contract.getId(), result.getOriginalFileName());

        return document;
    }

    public List<ContractDocument> getDocumentsByContractId(Long contractId) {
        return contractDocumentDomainService.findByContractId(contractId);
    }

    @Transactional
    public ContractDocument updateDocumentDescription(ContractDocumentUpdateCommand command) {
        ContractDocument document = contractDocumentDomainService.findById(command.documentId());

        document.updateDescription(command.description());
        log.info("계약서 문서 설명 업데이트 - Document ID: {}", document.getId());

        return document;
    }

    @Transactional
    public void deleteDocument(ContractDocumentDeleteCommand command) {
        Contract contract = contractDomainService.findById(command.contractId());
        ContractDocument document = contractDocumentDomainService.findById(command.documentId());

        fileStorageService.delete(document.getFilePath());

        contract.removeDocument(document);
        contractDocumentDomainService.deleteById(document.getId());

        log.info("계약서 문서 삭제 완료 - contractId: {}, documentId: {}", command.contractId(), command.documentId());
    }

    public ContractDocument getDocument(Long documentId) {
        return contractDocumentDomainService.findById(documentId);
    }

    public InputStream getDocumentFileStream(Long documentId) {
        ContractDocument document = contractDocumentDomainService.findById(documentId);
        return fileStorageService.loadAsStream(document.getFilePath());
    }

    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        String contentType = file.getContentType();
        boolean isValidType = isValidContentType(contentType);

        if (!isValidType) {
            // 브라우저가 신뢰할 수 없는 contentType(application/octet-stream 등)을 보낸 경우
            // 확장자로 한 번 더 검증
            isValidType = isValidExtension(file.getOriginalFilename());
        }

        if (!isValidType) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다");
        }

        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 50MB를 초과할 수 없습니다");
        }
    }

    private boolean isValidContentType(String contentType) {
        if (contentType == null) {
            return false;
        }

        return contentType.equals("application/pdf")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || contentType.equals("application/vnd.ms-excel")
                || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || contentType.startsWith("image/");
    }

    private boolean isValidExtension(String originalFileName) {
        if (originalFileName == null) {
            return false;
        }

        String lowerFileName = originalFileName.toLowerCase();
        return ALLOWED_EXTENSIONS.stream().anyMatch(lowerFileName::endsWith);
    }
}