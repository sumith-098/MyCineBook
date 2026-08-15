package com.cinebook.catalog.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.cinebook.catalog.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Handles owner-uploaded movie poster photos. Security notes:
 *  - Content is validated by SNIFFING THE ACTUAL BYTES (magic numbers), not by trusting the
 *    client-supplied filename extension or Content-Type header — either of those can be
 *    spoofed to smuggle in a non-image file.
 *  - The stored filename is always a fresh random UUID, never derived from the client's
 *    original filename — this closes off path traversal (e.g. "../../etc/passwd") and
 *    filename-collision attacks entirely, since the input string never touches the filesystem path.
 *  - Only JPEG/PNG/WEBP are accepted; size capped at 5MB (also enforced at the Spring
 *    multipart layer in application.yml as a second line of defense).
 */
@Service
public class PosterUploadService {

    // private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    // @Value("${app.uploads.dir}")
    // private String uploadsDir;

    // @Value("${app.uploads.public-base-url}")
    // private String publicBaseUrl;

    // @Value("${app.uploads.max-file-size-mb:5}")
    // private long maxFileSizeMb;

    // public String store(MultipartFile file) {
    //     if (file == null || file.isEmpty()) {
    //         throw new ApiException("No file uploaded.", HttpStatus.BAD_REQUEST);
    //     }
    //     if (file.getSize() > maxFileSizeMb * 1024 * 1024) {
    //         throw new ApiException("File too large — max " + maxFileSizeMb + "MB.", HttpStatus.BAD_REQUEST);
    //     }

    //     String extension = sniffImageExtension(file);
    //     String storedFilename = UUID.randomUUID() + "." + extension;

    //     try {
    //         Path posterDir = Paths.get(uploadsDir, "posters");
    //         Files.createDirectories(posterDir);
    //         Path target = posterDir.resolve(storedFilename).normalize();
    //         if (!target.getParent().equals(posterDir.normalize())) {
    //             // Defense in depth: should be unreachable since storedFilename is a UUID we
    //             // generated ourselves, but never trust a path join without verifying it stayed
    //             // inside the intended directory.
    //             throw new ApiException("Invalid upload path.", HttpStatus.BAD_REQUEST);
    //         }
    //         try (InputStream in = file.getInputStream()) {
    //             Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    //         }
    //     } catch (IOException e) {
    //         throw new ApiException("Could not save the uploaded file.", HttpStatus.INTERNAL_SERVER_ERROR);
    //     }

    //     return publicBaseUrl + "/uploads/posters/" + storedFilename;
    // }

    // /** Reads the first few bytes to identify the real image format — ignores filename/Content-Type entirely. */
    // private String sniffImageExtension(MultipartFile file) {
    //     byte[] header;
    //     try (InputStream in = file.getInputStream()) {
    //         header = in.readNBytes(12);
    //     } catch (IOException e) {
    //         throw new ApiException("Could not read the uploaded file.", HttpStatus.BAD_REQUEST);
    //     }

    //     if (header.length >= 3 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
    //         return "jpg"; // JPEG magic number: FF D8 FF
    //     }
    //     if (header.length >= 8 && (header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') {
    //         return "png"; // PNG magic number: 89 50 4E 47 0D 0A 1A 0A
    //     }
    //     if (header.length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
    //             && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
    //         return "webp"; // WEBP: "RIFF"....'WEBP'
    //     }
    //     throw new ApiException("Only JPEG, PNG, or WEBP images are allowed.", HttpStatus.BAD_REQUEST);
    // }


    private final BlobContainerClient containerClient;
    private final String cdnDomain;
    private final String containerName;
    private final long maxFileSizeMb;

    public PosterUploadService(
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name:movie-posters}") String containerName,
            @Value("${azure.cdn.domain}") String cdnDomain,
            @Value("${app.uploads.max-file-size-mb:10}") long maxFileSizeMb) {

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
        this.cdnDomain = cdnDomain;
        this.containerName = containerName;
        this.maxFileSizeMb = maxFileSizeMb;
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("No file uploaded.", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > maxFileSizeMb * 1024 * 1024) {
            throw new ApiException("File too large — max " + maxFileSizeMb + "MB.", HttpStatus.BAD_REQUEST);
        }

        // 1. Keep your safe byte sniffing check!
        String extension = sniffImageExtension(file);
        String mimeType = getMimeType(extension);
        
        // 2. Generate unique filename
        String storedFilename = UUID.randomUUID() + "." + extension;

        try {
            BlobClient blobClient = containerClient.getBlobClient(storedFilename);

            // 3. Upload memory stream straight to Azure Blob Storage
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            // Set content-type header so browsers render the image directly instead of downloading it
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(mimeType));

        } catch (IOException e) {
            throw new ApiException("Could not upload file to cloud storage.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 4. Return CDN URL (e.g., https://cinebook-cdn.azureedge.net/movie-posters/uuid.jpg)
        return String.format("%s/%s/%s", cdnDomain, containerName, storedFilename);
    }

    private String sniffImageExtension(MultipartFile file) {
        byte[] header;
        try (InputStream in = file.getInputStream()) {
            header = in.readNBytes(12);
        } catch (IOException e) {
            throw new ApiException("Could not read the uploaded file.", HttpStatus.BAD_REQUEST);
        }

        if (header.length >= 3 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (header.length >= 8 && (header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') {
            return "png";
        }
        if (header.length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "webp";
        }
        throw new ApiException("Only JPEG, PNG, or WEBP images are allowed.", HttpStatus.BAD_REQUEST);
    }

    private String getMimeType(String extension) {
        switch (extension) {
            case "jpg": return "image/jpeg";
            case "png": return "image/png";
            case "webp": return "image/webp";
            default: return "application/octet-stream";
        }
    }
}
