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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Value("${ollama.timeout:15000}")
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
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.restTemplate = new RestTemplate(factory);
        
        if (ollamaEnabled) {
            try {
                Thread.sleep(100);
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

    public List<String> getLocationSuggestions(String query) {
        if (!ollamaEnabled) {
            log.debug("⚠️ Ollama is disabled, returning empty suggestions");
            return List.of();
        }

        if (query == null || query.length() < 2) {
            return List.of();
        }

        try {
            log.info("📍 Getting location suggestions for: '{}'", query);

            String prompt = buildLocationPrompt(query);
            
            log.debug("📝 Prompt: {}", prompt);
            
            Map<String, Object> request = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false,
                    "options", Map.of(
                        "temperature", 0.2,
                        "num_predict", 64    // ✅ Reduced from 128 to 64 for faster responses
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
            log.info("✅ Location suggestions received in {}ms", duration);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String result = jsonNode.path("response").asText();
                log.info("📝 Ollama raw response: '{}'", result);
                List<String> suggestions = parseLocationSuggestions(result);
                log.info("📍 Found {} suggestions for '{}'", suggestions.size(), query);
                return suggestions;
            }

            log.warn("⚠️ Ollama returned non-2xx status: {}", response.getStatusCode());
            return List.of();

        } catch (ResourceAccessException e) {
            log.warn("⚠️ Ollama connection timeout for location suggestions: {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("❌ Error getting location suggestions: {}", e.getMessage(), e);
            return List.of();
        }
    }
    /**
     * Build prompt for location suggestions
     */
    private String buildLocationPrompt(String query) {
        return String.format(
            "List 5 Indian location names matching '%s'. Return ONLY the names separated by commas. Example: 'Mumbai Central, Bandra, Andheri'",
            query
        );
    }

    /**
     * Parse location suggestions from AI response
     */
    private List<String> parseLocationSuggestions(String response) {
        if (response == null || response.isEmpty()) {
            log.warn("⚠️ Empty response from Ollama");
            return List.of();
        }

        List<String> suggestions = new ArrayList<>();

        log.debug("📝 Raw response: {}", response);

        // Try to extract locations from the response
        String cleaned = response.trim();

        // Remove markdown code blocks if present
        cleaned = cleaned.replaceAll("```[a-z]*\\n?", "");
        cleaned = cleaned.replaceAll("```", "");

        // Remove "here is", "example", "format" phrases
        cleaned = cleaned.replaceAll("(?i)here is an example.*?:", "");
        cleaned = cleaned.replaceAll("(?i)example output format.*?:", "");
        cleaned = cleaned.replaceAll("(?i)for example.*?:", "");

        // Remove quoted text
        cleaned = cleaned.replaceAll("\"[^\"]*\"", "");

        // Split by common delimiters: comma, newline, or bullet points
        String[] parts = cleaned.split("[,\\n\\r]");

        for (String part : parts) {
            String trimmed = part.trim();
            // Remove bullet points, numbering, and special characters
            trimmed = trimmed.replaceAll("^[-•*\\d+][\\.\\)]?\\s*", "");
            trimmed = trimmed.replaceAll("^\\s*[-•*]\\s*", "");
            // Remove any remaining quotes
            trimmed = trimmed.replaceAll("^\"|\"$", "");
            
            // Filter: must be a valid location name (at least 2 chars, not a common word)
            if (!trimmed.isEmpty() && trimmed.length() > 2) {
                // Skip common phrases
                if (trimmed.matches("(?i).*(access|api|app|application|example|format|list|locations|output|property|rental|scenario|specific|suggest).*")) {
                    continue;
                }
                suggestions.add(trimmed);
            }
        }

        // If no suggestions found, try to extract from the raw response
        if (suggestions.isEmpty()) {
            // Try to find quoted locations
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("'([^']+)'");
            java.util.regex.Matcher matcher = pattern.matcher(response);
            while (matcher.find()) {
                String match = matcher.group(1).trim();
                if (!match.isEmpty() && match.length() > 2) {
                    suggestions.add(match);
                }
            }
        }

        log.info("📊 Found {} suggestions: {}", suggestions.size(), suggestions);
        return suggestions.stream().limit(10).collect(Collectors.toList());
    }

    /**
     * ✅ FAST health check with aggressive timeout
     */
    /* public boolean isOllamaRunning() {
        if (!ollamaEnabled) {
            log.debug("⚠️ Ollama is disabled");
            return false;
        }

        try {
            log.debug("🔍 Quick Ollama health check at: {}", ollamaUrl);
            
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
    } */
    public boolean isOllamaRunning() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                ollamaUrl + "/api/tags", String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}