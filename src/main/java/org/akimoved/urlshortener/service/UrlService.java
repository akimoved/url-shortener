package org.akimoved.urlshortener.service;

import lombok.RequiredArgsConstructor;
import org.akimoved.urlshortener.dto.ShortenUrlResponse;
import org.akimoved.urlshortener.exception.UrlNotFoundException;
import org.akimoved.urlshortener.model.Url;
import org.akimoved.urlshortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Сервис для работы с сокращенными URL.
 */
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    @Value("${app.short-url.base-url}")
    private String baseUrl;

    @Value("${app.short-url.code-length}")
    private int codeLength;

    /**
     * Сокращает URL или возвращает уже сокращенную имеющуюся.
     */
    @Transactional
    public ShortenUrlResponse shortenUrl(String originalUrl) {
        return urlRepository.findByOriginalUrl(originalUrl)
                .map(this::toResponse)
                .orElseGet(() -> createShortUrl(originalUrl));
    }

    /**
     * Получает исходную ссылку по коду.
     */
    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .map(Url::getOriginalUrl)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: " + shortCode));
    }

    private ShortenUrlResponse createShortUrl(String originalUrl) {
        String shortCode;
        int attempts = 0;
        int maxAttempts = 5;

        do {
            shortCode = generateShortCode(originalUrl + attempts);
            attempts++;
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Failed to generate unique short code after " + maxAttempts + " attempts");
            }
        } while (urlRepository.findByShortCode(shortCode).isPresent());

        Url url = Url.builder()
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .build();

        urlRepository.save(url);
        return toResponse(url);
    }

    private String generateShortCode(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8)); // Явное указание кодировки
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return encoded.substring(0, codeLength);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private ShortenUrlResponse toResponse(Url url) {
        return new ShortenUrlResponse(
                baseUrl + "/" + url.getShortCode(),
                url.getOriginalUrl()
        );
    }
}
