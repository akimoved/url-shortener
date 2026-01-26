package org.akimoved.urlshortener.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.akimoved.urlshortener.dto.ShortenUrlRequest;
import org.akimoved.urlshortener.dto.ShortenUrlResponse;
import org.akimoved.urlshortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * REST контроллер для операций по сокращению URL.
 */
@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    /**
     * Эндпоинт для сокращения URL.
     */
    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@Valid @RequestBody ShortenUrlRequest request) {
        ShortenUrlResponse response = urlService.shortenUrl(request.url());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Эндпоинт для редиректа на оригинальную URL.
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
