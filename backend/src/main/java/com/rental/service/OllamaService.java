package com.rental.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaService {

    private final ObjectMapper objectMapper;
    private RestTemplate restTemplate;

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:phi3:mini}")
    private String model;

    @Value("${ollama.timeout:2000}")  // ✅ REDUCED to 2 seconds
    private int timeoutMs;

    @Value("${ollama.enabled:true}")
    private boolean ollamaEnabled;

    @PostConstruct
    public void init() {
        log.info("🚀 Initializing OllamaService...");
        log.info("📡 Ollama URL: {}", ollamaUrl);
        log.info("🔧 Timeout: {}ms", timeoutMs);
        log.info("🎯 Model: {}", model);
        log.info("⚙️  Enabled: {}", ollamaEnabled);
        
        // ✅ Configure RestTemplate with aggressive timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.restTemplate = new RestTemplate(factory);
        
        // ✅ Quick connection test (non-blocking)
        if (ollamaEnabled) {
            try {
                Thread.sleep(100); // Give time for connection
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public String generateResponse(String prompt) {
        if (!ollamaEnabled) {
            log.debug("⚠️ Ollama is disabled");
            return "Error: Ollama service is disabled";
        }

        try {
            log.debug("🔄 Sending prompt to Ollama (length: {} chars)", prompt.length());

            Map<String, Object> request = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false,
                    "options", Map.of(
                        "temperature", 0.3,
                        "num_predict", 512
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(
                    ollamaUrl + "/api/generate",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            long duration = System.currentTimeMillis() - startTime;
            log.debug("✅ Ollama response received in {}ms", duration);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String result = jsonNode.path("response").asText();
                return result;
            } else {
                log.error("❌ Ollama error: {}", response.getStatusCode());
                return "Error: Unable to get response from AI";
            }

        } catch (ResourceAccessException e) {
            log.warn("⚠️ Ollama connection timeout: {}", e.getMessage());
            return "Error: AI service connection timeout";
        } catch (Exception e) {
            log.error("❌ Ollama service error: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * ✅ FAST health check with aggressive timeout
     */
    public boolean isOllamaRunning() {
        if (!ollamaEnabled) {
            log.debug("⚠️ Ollama is disabled");
            return false;
        }

        try {
            log.debug("🔍 Quick Ollama health check at: {}", ollamaUrl);
            
            // ✅ Use HEAD request or minimal generate
            Map<String, Object> request = Map.of(
                    "model", model,
                    "prompt", "ping",
                    "stream", false,
                    "options", Map.of("num_predict", 1)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(
                    ollamaUrl + "/api/generate",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            long duration = System.currentTimeMillis() - startTime;
            
            boolean isRunning = response.getStatusCode().is2xxSuccessful();
            log.info("✅ Ollama health check: {} ({}ms)", isRunning ? "OK" : "FAILED", duration);
            return isRunning;

        } catch (ResourceAccessException e) {
            log.warn("⚠️ Ollama connection timeout ({}ms): {}", timeoutMs, e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("⚠️ Ollama is not running: {}", e.getMessage());
            return false;
        }
    }
}