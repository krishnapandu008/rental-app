package com.rental.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class LocalStorageService {

    @Value("${app.storage.local.upload-dir}")
    private String uploadDir;

    @Value("${app.storage.local.base-url}")
    private String baseUrl; // e.g., http://localhost:8585 (no trailing slash)

    public String saveFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : "";
        String filename = UUID.randomUUID() + extension;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());
        log.info("File saved: {}", filePath);

        // ✅ Build URL correctly: baseUrl + "/images/" + filename
        return baseUrl + "/images/" + filename;
    }

    public void deleteFile(String fileUrl) {
        try {
            // Extract filename from URL (last part after last '/')
            String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir, filename);
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("File deleted: {}", filePath);
            } else {
                log.warn("File not found: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }
}