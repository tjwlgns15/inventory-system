package com.yhs.inventroysystem.domain.contract.entity;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contract_documents")
@NoArgsConstructor
@Getter
public class ContractDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(length = 100)
    private String contentType;

    @Column(length = 500)
    private String description;

    public ContractDocument(Contract contract, String originalFileName,
                            String storedFileName, String filePath, Long fileSize,
                            String contentType) {
        this.contract = contract;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}