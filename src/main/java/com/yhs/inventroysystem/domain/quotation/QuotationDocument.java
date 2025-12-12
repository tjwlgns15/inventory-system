package com.yhs.inventroysystem.domain.quotation;

import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quotation_documents")
@Getter
@NoArgsConstructor
public class QuotationDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_Id", nullable = false)
    private Quotation quotation;

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

    public QuotationDocument(Quotation quotation, String originalFileName,
                             String storedFileName, String filePath, Long fileSize,
                             String contentType) {
        this.quotation = quotation;
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
