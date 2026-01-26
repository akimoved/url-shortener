package org.akimoved.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос на сокращение ссылки.
 */
public record ShortenUrlRequest(
        @NotBlank(message = "URL cannot be blank")
        @Size(max = 2048, message = "URL is too long")
        String url
) {}

