package com.yhs.inventroysystem.infrastructure.file;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FileStorageFactory {

    private final Map<String, FileStorageService> storageServices;

    public FileStorageFactory(Map<String, FileStorageService> storageServices) {
        this.storageServices = storageServices;
    }

    public FileStorageService getStorageService(FileStorageType type) {
        FileStorageService service = storageServices.get(type.getStorageServiceName());
        if (service == null) {
            throw new IllegalArgumentException("저장소 서비스를 찾을 수 없습니다: " + type);
        }
        return service;
    }
}
