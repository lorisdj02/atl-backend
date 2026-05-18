package com.atl.backend.util;

import com.atl.backend.config.AppPublicUrlProperties;
import org.springframework.stereotype.Component;

@Component
public class PublicUrlUtil {
    private static String baseUrl;

    public PublicUrlUtil(AppPublicUrlProperties properties) {
        baseUrl = normalizeBaseUrl(properties.getBaseUrl());
    }

    public static String toPublicUrl(String maybeRelativeUrl) {
        if (maybeRelativeUrl == null || maybeRelativeUrl.isBlank()) {
            return maybeRelativeUrl;
        }
        String trimmed = maybeRelativeUrl.trim();
        if (startsWithHttpScheme(trimmed)) {
            return trimmed;
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            return normalizePath(trimmed);
        }
        return baseUrl + normalizePath(trimmed);
    }

    private static boolean startsWithHttpScheme(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private static String normalizeBaseUrl(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String normalizePath(String value) {
        if (value.startsWith("/")) {
            return value;
        }
        return "/" + value;
    }
}
