package com.atl.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageWebConfig implements WebMvcConfigurer {
    private final String publicBaseUrl;
    private final String storageRoot;

    public StorageWebConfig(AppStorageProperties properties) {
        this.storageRoot = properties.getPath();
        this.publicBaseUrl = normalizeBaseUrl(properties.getPublicBaseUrl());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path rootPath = Paths.get(storageRoot).toAbsolutePath().normalize();
        String resourceLocation = "file:" + rootPath + "/";
        // Serve only the public folder under the configured public base URL
        String publicLocation = resourceLocation + "public/";
        registry.addResourceHandler(publicBaseUrl + "/public/**")
            .addResourceLocations(publicLocation);
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
