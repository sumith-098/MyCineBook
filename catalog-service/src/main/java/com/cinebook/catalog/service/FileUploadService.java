package com.cinebook.catalog.service;



import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileUploadService {

    private final BlobContainerClient containerClient;
    private final String cdnDomain;
    private final String containerName;

    public FileUploadService(
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name}") String containerName,
            @Value("${azure.cdn.domain}") String cdnDomain) {

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
        this.cdnDomain = cdnDomain;
        this.containerName = containerName;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // Prevent file name conflicts with a unique prefix
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        // Upload stream directly to Azure Blob Container
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        // Construct and return the public CDN URL
        return String.format("%s/%s/%s", cdnDomain, containerName, fileName);
    }
}