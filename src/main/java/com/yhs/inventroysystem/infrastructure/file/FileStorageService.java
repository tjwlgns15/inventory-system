package com.yhs.inventroysystem.infrastructure.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorageService {
    FileUploadResult store(MultipartFile file, String directory);
    void delete(String filePath);
    byte[] load(String filePath);
    InputStream loadAsStream(String filePath);
}