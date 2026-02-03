package org.akimoved.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Запрос на сокращение ссылки.
 */
public record ShortenUrlRequest(
        @NotBlank(message = "URL cannot be blank")
        @Size(max = 2048, message = "URL is too long")
        @Pattern(regexp = "^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
                message = "Invalid URL format")
        String url
) {}

