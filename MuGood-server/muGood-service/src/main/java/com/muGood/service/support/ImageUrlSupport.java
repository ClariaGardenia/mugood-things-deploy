package com.muGood.service.support;

import java.util.List;
import java.util.Map;

public final class ImageUrlSupport {
    private ImageUrlSupport() {
    }

    public static void normalizeImages(List<Map<String, Object>> rows, String... keys) {
        rows.forEach(row -> normalizeImages(row, keys));
    }

    public static void normalizeImages(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value instanceof String text && !text.isBlank()) {
                row.put(key, normalizeImageUrl(text));
            }
        }
    }

    public static List<String> normalizeImageList(List<String> urls) {
        return urls.stream().map(ImageUrlSupport::normalizeImageUrl).toList();
    }

    public static String normalizeImageUrl(String originalUrl) {
        return originalUrl;
    }
}
