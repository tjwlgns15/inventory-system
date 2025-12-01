package com.yhs.inventroysystem.infrastructure.file;

public enum FileStorageType {
    PART_IMAGE("uploads/parts/images", "localFileStorage"),
    DELIVERY_DOCUMENT("uploads/delivery/documents", "localFileStorage");

    private final String directory;
    private final String storageServiceName;

    FileStorageType(String directory, String storageServiceName) {
        this.directory = directory;
        this.storageServiceName = storageServiceName;
    }

    public String getDirectory() {
        return directory;
    }

    public String getStorageServiceName() {
        return storageServiceName;
    }
}