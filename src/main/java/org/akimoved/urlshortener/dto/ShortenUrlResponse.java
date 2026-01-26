package org.akimoved.urlshortener.dto;

/**
 * Ответ, содержащий сокращенную ссылку
 */
public record ShortenUrlResponse(
        String shortUrl,
        String originalUrl
) {}
