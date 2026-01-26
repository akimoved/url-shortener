package org.akimoved.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.akimoved.urlshortener.dto.ShortenUrlRequest;
import org.akimoved.urlshortener.dto.ShortenUrlResponse;
import org.akimoved.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UrlRepository urlRepository;

    @Test
    void shortenUrl_createsAndReturnsShortUrl() throws Exception {
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com/test");

        MvcResult result = mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/test"))
                .andReturn();

        ShortenUrlResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ShortenUrlResponse.class
        );

        assertNotNull(response.shortUrl());
        assertTrue(response.shortUrl().startsWith("http://localhost:8080/"));
    }

    @Test
    void shortenUrl_returnsSameShortUrlForDuplicateRequest() throws Exception {
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com/duplicate");

        MvcResult firstResult = mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult secondResult = mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        ShortenUrlResponse first = objectMapper.readValue(
                firstResult.getResponse().getContentAsString(),
                ShortenUrlResponse.class
        );

        ShortenUrlResponse second = objectMapper.readValue(
                secondResult.getResponse().getContentAsString(),
                ShortenUrlResponse.class
        );

        assertEquals(first.shortUrl(), second.shortUrl());
    }

    @Test
    void shortenUrl_returnsValidationError() throws Exception {
        ShortenUrlRequest request = new ShortenUrlRequest("");

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.url").exists());
    }

    @Test
    void redirect_redirectsToOriginalUrl() throws Exception {
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com/redirect");

        MvcResult result = mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        ShortenUrlResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ShortenUrlResponse.class
        );

        String shortCode = response.shortUrl().substring(response.shortUrl().lastIndexOf('/') + 1);

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/redirect"));
    }

    @Test
    void redirect_returnsNotFoundForInvalidShortCode() throws Exception {
        mockMvc.perform(get("/invalid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}