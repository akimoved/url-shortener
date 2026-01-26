package org.akimoved.urlshortener.service;

import org.akimoved.urlshortener.dto.ShortenUrlResponse;
import org.akimoved.urlshortener.exception.UrlNotFoundException;
import org.akimoved.urlshortener.model.Url;
import org.akimoved.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(urlService, "codeLength", 6);
    }

    @Test
    void shortenUrl_createsNewShortUrl() {
        String originalUrl = "https://example.com/very/long/url";

        when(urlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.empty());
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> {
            Url url = invocation.getArgument(0);
            url.setId(1L);
            return url;
        });

        ShortenUrlResponse response = urlService.shortenUrl(originalUrl);

        assertNotNull(response);
        assertEquals(originalUrl, response.originalUrl());
        assertTrue(response.shortUrl().startsWith("http://localhost:8080/"));
        verify(urlRepository).save(any(Url.class));
    }

    @Test
    void shortenUrl_returnsExistingShortUrl() {
        String originalUrl = "https://example.com";
        Url existingUrl = Url.builder()
                .id(1L)
                .shortCode("abc123")
                .originalUrl(originalUrl)
                .build();

        when(urlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(existingUrl));

        ShortenUrlResponse response = urlService.shortenUrl(originalUrl);

        assertEquals("http://localhost:8080/abc123", response.shortUrl());
        assertEquals(originalUrl, response.originalUrl());
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    void getOriginalUrl_returnsUrl() {
        String shortCode = "abc123";
        String originalUrl = "https://example.com";
        Url url = Url.builder()
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .build();

        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(url));

        String result = urlService.getOriginalUrl(shortCode);

        assertEquals(originalUrl, result);
    }

    @Test
    void getOriginalUrl_throwsExceptionWhenNotFound() {
        String shortCode = "notfound";

        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> urlService.getOriginalUrl(shortCode));
    }
}