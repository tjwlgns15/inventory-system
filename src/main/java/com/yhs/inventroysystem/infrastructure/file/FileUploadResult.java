package com.yhs.inventroysystem.infrastructure.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileUploadResult {
    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private Long fileSize;
}
