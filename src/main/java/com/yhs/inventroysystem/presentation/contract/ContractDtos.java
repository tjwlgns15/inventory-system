package com.yhs.inventroysystem.presentation.contract;

import com.yhs.inventroysystem.application.contract.ContractCommands;
import com.yhs.inventroysystem.domain.contract.entity.Contract;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public class ContractDtos {

    public record ContractCreate(
            @NotNull(message = "날짜는 필수입니다.")
            LocalDate recordDate,

            @NotBlank(message = "문서명은 필수입니다.")
            String documentName,

            @NotBlank(message = "고객사는 필수입니다.")
            String companyName,

            @NotNull(message = "계약일은 필수입니다.")
            LocalDate contractDate,

            LocalDate validityStartDate,
            LocalDate validityEndDate,
            LocalDate signedDate,
            String signerName,
            String note
    ) {
        public ContractCommands.CreateCommand toCommand() {
            return new ContractCommands.CreateCommand(
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
        }
    }

    public record ContractUpdate(
            LocalDate recordDate,
            String documentName,
            String companyName,
            LocalDate contractDate,
            LocalDate validityStartDate,
            LocalDate validityEndDate,
            LocalDate signedDate,
            String signerName,
            String note
    ) {
        public ContractCommands.UpdateCommand toCommand() {
            return new ContractCommands.UpdateCommand(
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
        }
    }

    public record ContractResponse(
            Long id,
            LocalDate recordDate,
            String documentName,
            String companyName,
            LocalDate contractDate,
            LocalDate validityStartDate,
            LocalDate validityEndDate,
            LocalDate signedDate,
            String signerName,
            String note,
            boolean signed
    ) {
        public static ContractResponse from(Contract contract) {
            return new ContractResponse(
                    contract.getId(),
                    contract.getRecordDate(),
                    contract.getDocumentName(),
                    contract.getCompanyName(),
                    contract.getContractDate(),
                    contract.getValidityStartDate(),
                    contract.getValidityEndDate(),
                    contract.getSignedDate(),
                    contract.getSignerName(),
                    contract.getNote(),
                    contract.isSigned()
            );
        }
    }

    public record PageContractResponse(
            List<ContractResponse> content,
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean empty
    ) {
        public static PageContractResponse from(Page<Contract> page) {
            List<ContractResponse> content = page.getContent().stream()
                    .map(ContractResponse::from)
                    .toList();

            return new PageContractResponse(
                    content,
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast(),
                    page.isEmpty()
            );
        }
    }
}