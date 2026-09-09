package com.yhs.inventroysystem.domain.contract.entity;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contracts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contract extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate recordDate; // 날짜 (대장 등록일)

    @Column(nullable = false, length = 200)
    private String documentName; // 문서명

    @Column(nullable = false, length = 200)
    private String companyName; // 고객사 (Client 미연동, 자유 입력)

    @Column(nullable = false)
    private LocalDate contractDate; // 계약일

    private LocalDate validityStartDate; // 유효기간 시작일
    private LocalDate validityEndDate;   // 유효기간 종료일

    private LocalDate signedDate; // 서명일

    @Column(length = 100)
    private String signerName; // 서명자

    @Column(length = 500)
    private String note; // 비고

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<ContractDocument> documents = new ArrayList<>();

    // ========== 정적 팩토리 메서드 ==========

    public static Contract create(LocalDate recordDate, String documentName, String companyName,
                                  LocalDate contractDate, LocalDate validityStartDate,
                                  LocalDate validityEndDate, LocalDate signedDate,
                                  String signerName, String note) {
        validate(documentName, companyName, contractDate);

        Contract contract = new Contract();
        contract.recordDate = recordDate;
        contract.documentName = documentName;
        contract.companyName = companyName;
        contract.contractDate = contractDate;
        contract.validityStartDate = validityStartDate;
        contract.validityEndDate = validityEndDate;
        contract.signedDate = signedDate;
        contract.signerName = signerName;
        contract.note = note;

        return contract;
    }

    // ========== 비즈니스 메서드 ==========

    public void updateInfo(LocalDate recordDate, String documentName, String companyName,
                           LocalDate contractDate, LocalDate validityStartDate,
                           LocalDate validityEndDate, LocalDate signedDate,
                           String signerName, String note) {
        ensureNotDeleted();
        this.recordDate = recordDate;
        this.documentName = documentName;
        this.companyName = companyName;
        this.contractDate = contractDate;
        this.validityStartDate = validityStartDate;
        this.validityEndDate = validityEndDate;
        this.signedDate = signedDate;
        this.signerName = signerName;
        this.note = note;
    }

    public void addDocument(ContractDocument document) {
        ensureNotDeleted();
        this.documents.add(document);
    }

    public boolean isSigned() {
        return this.signedDate != null;
    }

    public boolean isValidOn(LocalDate date) {
        if (validityStartDate == null || validityEndDate == null) {
            return false;
        }
        return !date.isBefore(validityStartDate) && !date.isAfter(validityEndDate);
    }

    public void removeDocument(ContractDocument document) {
        ensureNotDeleted();
        this.documents.remove(document);
    }

    private static void validate(String documentName, String companyName, LocalDate contractDate) {
        if (documentName == null || documentName.isBlank()) {
            throw new IllegalArgumentException("문서명은 필수입니다.");
        }
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("고객사는 필수입니다.");
        }
        if (contractDate == null) {
            throw new IllegalArgumentException("계약일은 필수입니다.");
        }
    }
}