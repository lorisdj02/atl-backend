package com.atl.backend.service.storage;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.Account;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalImageStorageService implements ImageStorageService {
    private final Path storageRoot;
    private final String publicBaseUrl;

    public LocalImageStorageService(
            com.atl.backend.config.AppStorageProperties properties) {
        this.storageRoot = Paths.get(properties.getPath()).toAbsolutePath().normalize();
        this.publicBaseUrl = normalizeBaseUrl(properties.getPublicBaseUrl());
    }

    @Override
    public String storeProfilePicture(Account account, MultipartFile file) {
        System.out.println("Storing profile picture for account ID: " + account.getId());
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(ErrorsString.MISSING_REQUIRED_FIELDS);
        }

        String extension = getExtension(file.getOriginalFilename());
        String fileName = System.currentTimeMillis() + extension;
        Path accountDir = storageRoot
                .resolve("public")
                .resolve("profile-pictures")
                .resolve(String.valueOf(account.getId()));
        Path destination = accountDir.resolve(fileName).normalize();

        try {
            System.out.println("Storing file at: " + destination.toString());
            Files.createDirectories(storageRoot.resolve("public"));
            Files.createDirectories(storageRoot.resolve("private"));
            Files.createDirectories(accountDir);
            file.transferTo(destination);
            System.out.println("File stored successfully at: " + destination.toString());
        } catch (IOException ex) {
            System.out.println("Error occurred while storing file at: " + destination.toString());
            throw new RuntimeException("STORAGE_ERROR", ex);
        }

        return publicBaseUrl + "/public/profile-pictures/" + account.getId() + "/" + fileName;
    }

    private static String getExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dotIndex);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "/storage";
        }
        String normalized = baseUrl.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.endsWith("/")) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
