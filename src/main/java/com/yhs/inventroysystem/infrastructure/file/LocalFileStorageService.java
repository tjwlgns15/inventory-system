package com.yhs.inventroysystem.infrastructure.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Component("localFileStorage")
@Slf4j
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
        // 작은 파일만 사용하도록 경고 로그 추가
        try {
            Path path = Paths.get(filePath);
            long fileSize = Files.size(path);
            if (fileSize > 10 * 1024 * 1024) { // 10MB 초과
                throw new FileStorageException("파일이 너무 큽니다. 스트리밍 방식을 사용하세요: " + filePath);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new FileStorageException("파일 로드 실패: " + filePath, e);
        }
    }

    @Override
    public InputStream loadAsStream(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.newInputStream(path);
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
