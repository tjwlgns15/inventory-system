package com.yhs.inventroysystem.infrastructure.file;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Component("localFileStorage")
public class LocalFileStorageService implements FileStorageService{

    private static final String BASE_UPLOAD_DIR = System.getProperty("user.dir");


    @Override
    public FileUploadResult store(MultipartFile file, String directory) {
        try {
            String originalFileName = file.getOriginalFilename();
            String storedFileName = generateUniqueFileName(Objects.requireNonNull(originalFileName));

            Path directoryPath = Paths.get(BASE_UPLOAD_DIR, directory);
            Files.createDirectories(directoryPath);

            Path filePath = directoryPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return new FileUploadResult(
                    originalFileName,
                    storedFileName,
                    filePath.toString(),
                    file.getSize()
            );
        } catch (IOException e) {
            throw new FileStorageException("파일 저장 실패: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileStorageException("파일 삭제 실패: " + filePath, e);
        }
    }

    @Override
    public byte[] load(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new FileStorageException("파일 로드 실패: " + filePath, e);
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + getExtension(originalFileName);
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }

}
