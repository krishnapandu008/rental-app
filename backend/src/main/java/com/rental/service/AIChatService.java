package com.rental.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.dto.SearchFilters;
import com.rental.dto.VoiceSearchResponse;
import com.rental.dto.PropertyResponseDto;
import com.rental.entity.Property;
import com.rental.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIChatService {

    // ✅ Valid cities for validation
    private static final List<String> VALID_CITIES = List.of(
        "bangalore", "mumbai", "hyderabad", "chennai", "delhi", 
        "kuppam", "pune", "kolkata", "ahmedabad", "noida", 
        "gurgaon", "indore", "bhopal", "jaipur", "lucknow",
        "santhipuram"  // ✅ Note: Correct spelling is "Santhipuram"
    );

    // ✅ IMPROVED: Better location regex pattern
    private static final Pattern LOCATION_PATTERN = Pattern.compile(
        "(?:in|near|at|for)\\s+([A-Za-z\\s]+?)(?:\\s+(?:under|above|below|with|\\d)|$)|\\b([A-Za-z]+)\\b(?=\\s*(?:under|above|below|with|\\d|$))",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?:under|below|<|less than)\\s*[₹Rs.]?\\s*(\\d+[,.]?\\d*)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern MIN_PRICE_PATTERN = Pattern.compile(
        "(?:above|over|>|more than)\\s*[₹Rs.]?\\s*(\\d+[,.]?\\d*)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern PRICE_RANGE_PATTERN = Pattern.compile(
        "(?:between)\\s*[₹Rs.]?\\s*(\\d+[,.]?\\d*)\\s*(?:and|to|\\-)\\s*[₹Rs.]?\\s*(\\d+[,.]?\\d*)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern BEDROOM_PATTERN = Pattern.compile(
        "(\\d+)\\s*(?:BHK|bedroom|bed|bhk)",
        Pattern.CASE_INSENSITIVE
    );

    private final OllamaService ollamaService;
    private final PropertyService propertyService;
    private final PropertyRepository propertyRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.voice-search.fallback-enabled:true}")
    private boolean fallbackEnabled;

    @Value("${app.voice-search.ai-timeout-ms:300}")
    private int aiTimeoutMs;

    public VoiceSearchResponse processVoiceSearch(String query) {
        Instant start = Instant.now();
        log.info("🔍 ===== START Voice Search ===== ");
        log.info("📝 Query: {}", query);

        long regexStart = System.currentTimeMillis();
        SearchFilters regexFilters = extractFiltersWithRegex(query);
        long regexDuration = System.currentTimeMillis() - regexStart;
        log.info("⏱️ Step 1 - Regex extraction: {}ms", regexDuration);
        log.info("📊 Regex filters: {}", regexFilters);

        SearchFilters finalFilters = regexFilters;
        String explanation = null;
        boolean aiAvailable = false;
        boolean aiUsed = false;
        long aiDuration = 0;

        long aiCheckStart = System.currentTimeMillis();
        try {
            aiAvailable = ollamaService.isOllamaRunning();
            long aiCheckDuration = System.currentTimeMillis() - aiCheckStart;
            log.info("⏱️ Step 2 - AI availability check: {}ms (available: {})", aiCheckDuration, aiAvailable);
            
            if (aiAvailable && fallbackEnabled) {
                log.info("🤖 AI available, attempting extraction with {}ms timeout", aiTimeoutMs);
                
                long aiExtractStart = System.currentTimeMillis();
                
                CompletableFuture<SearchFilters> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        log.debug("🧠 AI extraction started...");
                        return extractFiltersWithAI(query);
                    } catch (Exception e) {
                        log.warn("⚠️ AI extraction failed: {}", e.getMessage());
                        return null;
                    }
                });
                
                SearchFilters aiFilters = null;
                try {
                    aiFilters = future.get(aiTimeoutMs, TimeUnit.MILLISECONDS);
                    log.info("✅ AI responded within {}ms", aiTimeoutMs);
                } catch (TimeoutException e) {
                    log.info("⏱️ AI TIMED OUT after {}ms - using regex results", aiTimeoutMs);
                    future.cancel(true);
                } catch (Exception e) {
                    log.warn("⚠️ AI extraction error: {}", e.getMessage());
                }
                
                aiDuration = System.currentTimeMillis() - aiExtractStart;
                log.info("⏱️ Step 3 - AI extraction: {}ms (timeout: {}ms)", aiDuration, aiTimeoutMs);
                
                if (aiFilters != null && !hasNoValidFilters(aiFilters)) {
                    log.info("✅ AI extracted filters successfully: {}", aiFilters);
                    finalFilters = aiFilters;
                    aiUsed = true;
                    explanation = "AI understood your search";
                } else {
                    log.info("⚡ Using regex filters (AI: {})", 
                        aiFilters == null ? "timed out after " + aiTimeoutMs + "ms" : "invalid response");
                    if (explanation == null) {
                        explanation = "AI couldn't fully understand, using basic search.";
                    }
                }
            } else if (!aiAvailable) {
                explanation = "AI service is not available. Using basic search.";
            }
        } catch (Exception e) {
            log.error("❌ AI processing error: {}", e.getMessage());
            if (explanation == null) {
                explanation = "Search service encountered an issue. Using basic search.";
            }
        }

        long validateStart = System.currentTimeMillis();
        SearchFilters validatedFilters = validateAndCleanFilters(finalFilters);
        long validateDuration = System.currentTimeMillis() - validateStart;
        log.info("⏱️ Step 4 - Validate & clean filters: {}ms", validateDuration);
        log.info("📊 Final filters: {}", validatedFilters);
        
        if (explanation == null && validatedFilters.getExplanation() != null) {
            explanation = validatedFilters.getExplanation();
        }

        long searchStart = System.currentTimeMillis();
        List<Property> properties = searchProperties(validatedFilters);
        long searchDuration = System.currentTimeMillis() - searchStart;
        log.info("⏱️ Step 5 - Property search: {}ms (found {} properties)", searchDuration, properties.size());

        if (!properties.isEmpty()) {
            log.info("📋 Found properties:");
            for (Property p : properties) {
                log.info("   - {} | ₹{} | {} | {} BHK", p.getTitle(), p.getRent(), p.getLocation(), p.getBedrooms());
            }
        }

        long dtoStart = System.currentTimeMillis();
        List<PropertyResponseDto> propertyDtos = properties.stream()
                .limit(50)
                .map(property -> propertyService.toDto(property))
                .collect(Collectors.toList());
        long dtoDuration = System.currentTimeMillis() - dtoStart;
        log.info("⏱️ Step 6 - DTO conversion: {}ms", dtoDuration);

        if (explanation == null) {
            explanation = generateExplanation(properties, validatedFilters);
        }

        long totalDuration = System.currentTimeMillis() - regexStart;
        
        log.info("⏱️ ===== VOICE SEARCH SUMMARY =====");
        log.info("📊 Total: {}ms", totalDuration);
        log.info("📊 Regex: {}ms, AI: {}ms, Validate: {}ms, Search: {}ms, DTO: {}ms", 
            regexDuration, aiDuration, validateDuration, searchDuration, dtoDuration);
        log.info("🤖 AI Used: {}, AI Available: {}, Results: {}", aiUsed, aiAvailable, properties.size());
        log.info("📝 Query: {}", query);
        log.info("🔍 ===== END Voice Search ===== ");

        return VoiceSearchResponse.builder()
                .transcript(query)
                .explanation(explanation)
                .filters(validatedFilters)
                .properties(propertyDtos)
                .totalResults(properties.size())
                .aiAvailable(aiAvailable)
                .build();
    }

    private SearchFilters extractFiltersWithAI(String query) {
        long start = System.currentTimeMillis();
        
        String prompt = 
            "Extract rental search filters from this query: \"" + query + "\"\n\n" +
            "Return ONLY valid JSON with these fields (use null if not mentioned):\n" +
            "- location: city name (Bangalore, Mumbai, Hyderabad, Chennai, Delhi, Kuppam, Pune, Kolkata, Ahmedabad, Noida, Gurgaon, Indore, Bhopal, Jaipur, Lucknow, Santhipuram)\n" +
            "- minRent: number (minimum rent)\n" +
            "- maxRent: number (maximum rent)\n" +
            "- bedrooms: number (1, 2, 3, etc.)\n" +
            "- amenities: array of strings (parking, metro, gym, pool, garden, furnished, ac)\n\n" +
            "Examples:\n" +
            "Query: '2 BHK in Bangalore under 25000'\n" +
            "Response: {\"location\":\"Bangalore\",\"minRent\":null,\"maxRent\":25000,\"bedrooms\":2,\"amenities\":null}\n\n" +
            "Query: 'apartment in Chennai between 15000 and 30000'\n" +
            "Response: {\"location\":\"Chennai\",\"minRent\":15000,\"maxRent\":30000,\"bedrooms\":null,\"amenities\":null}\n\n" +
            "Query: '1 BHK with parking near metro in Hyderabad'\n" +
            "Response: {\"location\":\"Hyderabad\",\"minRent\":null,\"maxRent\":null,\"bedrooms\":1,\"amenities\":[\"parking\",\"metro\"]}\n\n" +
            "Query: '3 BHK furnished apartment in Chennai'\n" +
            "Response: {\"location\":\"Chennai\",\"minRent\":null,\"maxRent\":null,\"bedrooms\":3,\"amenities\":[\"furnished\"]}\n\n" +
            "Query: 'modern apartment with good view'\n" +
            "Response: {\"location\":null,\"minRent\":null,\"maxRent\":null,\"bedrooms\":null,\"amenities\":null}\n\n" +
            "NOW extract from: \"" + query + "\"\n" +
            "Return ONLY JSON, no other text.";

        try {
            String aiResponse = ollamaService.generateResponse(prompt);
            
            String cleanResponse = aiResponse
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .replaceAll("^\\s*", "")
                .replaceAll("\\s*$", "");

            SearchFilters filters = objectMapper.readValue(cleanResponse, SearchFilters.class);
            
            String explanation = generateAIExplanation(query, filters);
            filters.setExplanation(explanation);
            
            long totalDuration = System.currentTimeMillis() - start;
            log.info("⏱️ AI extraction successful in {}ms", totalDuration);
            
            return filters;
            
        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - start;
            log.warn("⚠️ AI extraction failed after {}ms: {}", totalDuration, e.getMessage());
            return SearchFilters.builder()
                    .explanation("Sorry, I couldn't understand your query. Please try again.")
                    .build();
        }
    }

    private String generateAIExplanation(String query, SearchFilters filters) {
        StringBuilder explanation = new StringBuilder("Searching for ");
        
        if (filters.getBedrooms() != null) {
            explanation.append(filters.getBedrooms()).append(" BHK ");
        }
        
        if (filters.getLocation() != null) {
            explanation.append("in ").append(filters.getLocation()).append(" ");
        }
        
        if (filters.getMinRent() != null && filters.getMaxRent() != null) {
            explanation.append("between ₹").append(String.format("%.0f", filters.getMinRent()))
                     .append(" and ₹").append(String.format("%.0f", filters.getMaxRent()));
        } else if (filters.getMaxRent() != null) {
            explanation.append("under ₹").append(String.format("%.0f", filters.getMaxRent()));
        } else if (filters.getMinRent() != null) {
            explanation.append("above ₹").append(String.format("%.0f", filters.getMinRent()));
        }
        
        if (filters.getAmenities() != null && !filters.getAmenities().isEmpty()) {
            explanation.append(" with ").append(String.join(", ", filters.getAmenities()));
        }
        
        return explanation.toString();
    }

    /**
     * ✅ IMPROVED: Fallback regex extraction with better location detection
     */
    private SearchFilters extractFiltersWithRegex(String query) {
        String lowerQuery = query.toLowerCase();
        SearchFilters.SearchFiltersBuilder builder = SearchFilters.builder();

        log.info("🔍 Extracting from: {}", lowerQuery);

        // ✅ Extract location using multiple patterns
        String extractedLocation = null;
        
        // Pattern 1: "in X", "near X", "at X", "for X"
        Matcher locationMatcher1 = Pattern.compile(
            "(?:in|near|at|for)\\s+([A-Za-z\\s]+?)(?:\\s+(?:under|above|below|with|\\d)|$)",
            Pattern.CASE_INSENSITIVE
        ).matcher(lowerQuery);
        
        if (locationMatcher1.find()) {
            String location = locationMatcher1.group(1).trim();
            if (isValidCity(location)) {
                extractedLocation = location;
                log.info("📍 Extracted location (pattern 1): {}", extractedLocation);
            }
        }
        
        // Pattern 2: Standalone city name
        if (extractedLocation == null) {
            Matcher locationMatcher2 = Pattern.compile(
                "\\b([A-Za-z]+)\\b(?=\\s*(?:under|above|below|with|\\d|$))",
                Pattern.CASE_INSENSITIVE
            ).matcher(lowerQuery);
            
            while (locationMatcher2.find()) {
                String location = locationMatcher2.group(1).trim();
                if (isValidCity(location)) {
                    extractedLocation = location;
                    log.info("📍 Extracted location (pattern 2): {}", extractedLocation);
                    break;
                }
            }
        }
        
        // Pattern 3: Direct city name match (standalone word)
        if (extractedLocation == null) {
            // Check if any word in the query matches a valid city (with fuzzy matching)
            String[] words = lowerQuery.split("\\s+");
            for (String word : words) {
                if (isValidCity(word)) {
                    extractedLocation = word;
                    log.info("📍 Extracted location (pattern 3): {}", extractedLocation);
                    break;
                }
            }
        }
        
        // Pattern 4: "location X" format
        if (extractedLocation == null) {
            Matcher locationMatcher4 = Pattern.compile(
                "location\\s+([A-Za-z\\s]+)",
                Pattern.CASE_INSENSITIVE
            ).matcher(lowerQuery);
            
            if (locationMatcher4.find()) {
                String location = locationMatcher4.group(1).trim();
                if (isValidCity(location)) {
                    extractedLocation = location;
                    log.info("📍 Extracted location (pattern 4): {}", extractedLocation);
                }
            }
        }
        
        if (extractedLocation != null) {
            // ✅ Normalize location to match database
            String normalizedLocation = normalizeLocation(extractedLocation);
            builder.location(normalizedLocation);
            log.info("📍 Normalized location: {}", normalizedLocation);
        }

        // Extract price range "between X and Y"
        Matcher rangeMatcher = PRICE_RANGE_PATTERN.matcher(lowerQuery);
        if (rangeMatcher.find()) {
            String minPriceStr = rangeMatcher.group(1).replace(",", "");
            String maxPriceStr = rangeMatcher.group(2).replace(",", "");
            try {
                double minPrice = Double.parseDouble(minPriceStr);
                double maxPrice = Double.parseDouble(maxPriceStr);
                builder.minRent(minPrice);
                builder.maxRent(maxPrice);
                log.info("💰 Extracted price range: {} - {}", minPrice, maxPrice);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        // Extract max price (under/below)
        if (builder.build().getMaxRent() == null) {
            Matcher priceMatcher = PRICE_PATTERN.matcher(lowerQuery);
            if (priceMatcher.find()) {
                String priceStr = priceMatcher.group(1).replace(",", "");
                try {
                    double price = Double.parseDouble(priceStr);
                    builder.maxRent(price);
                    log.info("💰 Extracted max rent: {}", price);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        // Extract min price (above/over)
        if (builder.build().getMinRent() == null) {
            Matcher minPriceMatcher = MIN_PRICE_PATTERN.matcher(lowerQuery);
            if (minPriceMatcher.find()) {
                String priceStr = minPriceMatcher.group(1).replace(",", "");
                try {
                    double price = Double.parseDouble(priceStr);
                    builder.minRent(price);
                    log.info("💰 Extracted min rent: {}", price);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        // Extract bedrooms
        Matcher bedroomMatcher = BEDROOM_PATTERN.matcher(lowerQuery);
        if (bedroomMatcher.find()) {
            try {
                int bedrooms = Integer.parseInt(bedroomMatcher.group(1));
                builder.bedrooms(bedrooms);
                log.info("🛏️ Extracted bedrooms: {}", bedrooms);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        // Extract amenities
        List<String> amenities = extractAmenities(lowerQuery);
        if (!amenities.isEmpty()) {
            builder.amenities(amenities);
            log.info("✨ Extracted amenities: {}", amenities);
        }

        builder.explanation("Extracted from: " + query);
        return builder.build();
    }

    /**
     * ✅ Normalize location spelling variations
     */
    private String normalizeLocation(String location) {
        if (location == null) return null;
        String lowerLoc = location.toLowerCase().trim();
        
        // Handle "shanti" vs "santhi" variations
        if (lowerLoc.contains("shantipuram") || lowerLoc.contains("santhipuram")) {
            return "Santhipuram";
        }
        
        // Handle "bangalore" vs "bengaluru"
        if (lowerLoc.contains("bengaluru") || lowerLoc.contains("bangalore")) {
            return "Bangalore";
        }
        
        // Check if it exactly matches any valid city
        for (String city : VALID_CITIES) {
            if (lowerLoc.equals(city) || lowerLoc.contains(city) || city.contains(lowerLoc)) {
                // Return the properly capitalized version from the list
                return city.substring(0, 1).toUpperCase() + city.substring(1);
            }
        }
        
        return location;
    }

    /**
     * ✅ IMPROVED: Fuzzy matching for location detection
     */
    private boolean isValidCity(String city) {
        if (city == null) return false;
        String lowerCity = city.toLowerCase().trim();
        
        // ✅ Check for exact match or contains
        for (String validCity : VALID_CITIES) {
            String lowerValid = validCity.toLowerCase();
            
            // Exact match
            if (lowerCity.equals(lowerValid)) {
                return true;
            }
            
            // Contains match
            if (lowerValid.contains(lowerCity) || lowerCity.contains(lowerValid)) {
                return true;
            }
            
            // ✅ Fuzzy match for common spelling variations
            // Handle "shanti" vs "santhi" variations
            String normalizedCity = lowerCity
                .replaceAll("sh", "s")
                .replaceAll("h", "")
                .replaceAll("\\s+", "");
                
            String normalizedValid = lowerValid
                .replaceAll("sh", "s")
                .replaceAll("h", "")
                .replaceAll("\\s+", "");
                
            if (normalizedCity.equals(normalizedValid) || 
                normalizedCity.contains(normalizedValid) || 
                normalizedValid.contains(normalizedCity)) {
                return true;
            }
        }
        return false;
    }

    private List<String> extractAmenities(String query) {
        List<String> foundAmenities = new java.util.ArrayList<>();
        String[] commonAmenities = {"parking", "metro", "gym", "pool", "garden", "furnished", "ac", "air conditioning"};
        
        for (String amenity : commonAmenities) {
            if (query.contains(amenity)) {
                foundAmenities.add(amenity);
            }
        }
        return foundAmenities;
    }

    private boolean hasNoValidFilters(SearchFilters filters) {
        return filters == null || 
               (filters.getLocation() == null && 
                filters.getMinRent() == null && 
                filters.getMaxRent() == null && 
                filters.getBedrooms() == null && 
                (filters.getAmenities() == null || filters.getAmenities().isEmpty()));
    }

    private VoiceSearchResponse createEmptyResponse(String query, String explanation) {
        return VoiceSearchResponse.builder()
                .transcript(query)
                .explanation(explanation)
                .properties(List.of())
                .totalResults(0)
                .aiAvailable(false)
                .build();
    }

    private SearchFilters validateAndCleanFilters(SearchFilters filters) {
        // 1. Validate location
        if (filters.getLocation() != null && !filters.getLocation().isEmpty()) {
            String location = filters.getLocation().toLowerCase().trim();
            boolean isValidCity = VALID_CITIES.stream().anyMatch(city -> 
                location.contains(city) || city.contains(location) || location.equals(city)
            );
            if (!isValidCity) {
                log.warn("⚠️ Invalid location detected: {}, setting to null", filters.getLocation());
                filters.setLocation(null);
            } else {
                String matchedCity = VALID_CITIES.stream()
                        .filter(city -> location.contains(city) || city.contains(location) || location.equals(city))
                        .findFirst()
                        .orElse(location);
                filters.setLocation(matchedCity);
                log.info("✅ Validated location: {}", matchedCity);
            }
        }

        // 2. Validate rent ranges
        if (filters.getMinRent() != null && filters.getMaxRent() != null) {
            if (filters.getMinRent() > filters.getMaxRent()) {
                log.warn("⚠️ Invalid rent range: minRent({}) > maxRent({}), swapping values", 
                    filters.getMinRent(), filters.getMaxRent());
                Double temp = filters.getMinRent();
                filters.setMinRent(filters.getMaxRent());
                filters.setMaxRent(temp);
            }
        }

        // 3. Validate amenities
        if (filters.getAmenities() != null) {
            List<String> cleanedAmenities = filters.getAmenities().stream()
                    .filter(a -> a != null && !a.isBlank() && !a.equalsIgnoreCase("null"))
                    .collect(Collectors.toList());
            filters.setAmenities(cleanedAmenities.isEmpty() ? null : cleanedAmenities);
        }

        // 4. If no valid filters found
        if (filters.getLocation() == null && filters.getMinRent() == null && 
            filters.getMaxRent() == null && filters.getBedrooms() == null && 
            filters.getAmenities() == null) {
            filters.setExplanation("No valid search filters found. Please try a different query.");
        }

        return filters;
    }

    private List<Property> searchProperties(SearchFilters filters) {
        try {
            log.info("🔍 Searching properties with filters: {}", filters);
            
            List<Property> results = propertyRepository.findByAvailableTrue();
            log.info("📊 Total available properties before filtering: {}", results.size());
            
            List<Property> filtered = results.stream()
                    .filter(p -> {
                        // ✅ Location filter with fuzzy matching
                        if (filters.getLocation() != null && !filters.getLocation().isEmpty()) {
                            String filterLocation = filters.getLocation().toLowerCase().trim();
                            String propLocation = p.getLocation() != null ? p.getLocation().toLowerCase().trim() : "";
                            
                            // Check for fuzzy match
                            boolean matches = propLocation.contains(filterLocation) || 
                                             filterLocation.contains(propLocation) ||
                                             // Handle "shanti" vs "santhi" variations
                                             propLocation.replaceAll("sh", "s").contains(filterLocation.replaceAll("sh", "s"));
                            
                            if (!matches) {
                                log.debug("❌ Location filter: '{}' not in '{}'", filterLocation, propLocation);
                            }
                            return matches;
                        }
                        return true;
                    })
                    .filter(p -> {
                        if (filters.getMinRent() != null && p.getRent() < filters.getMinRent()) {
                            log.debug("❌ Min rent filter: {} < {}", p.getRent(), filters.getMinRent());
                            return false;
                        }
                        if (filters.getMaxRent() != null && p.getRent() > filters.getMaxRent()) {
                            log.debug("❌ Max rent filter: {} > {}", p.getRent(), filters.getMaxRent());
                            return false;
                        }
                        return true;
                    })
                    .filter(p -> {
                        if (filters.getBedrooms() != null) {
                            boolean matches = p.getBedrooms() != null && p.getBedrooms().equals(filters.getBedrooms());
                            if (!matches) {
                                log.debug("❌ Bedrooms filter: {} != {}", p.getBedrooms(), filters.getBedrooms());
                            }
                            return matches;
                        }
                        return true;
                    })
                    .filter(p -> {
                        if (filters.getAmenities() != null && !filters.getAmenities().isEmpty()) {
                            String text = (p.getDescription() != null ? p.getDescription().toLowerCase() : "") + 
                                         " " + (p.getLocation() != null ? p.getLocation().toLowerCase() : "");
                            for (String amenity : filters.getAmenities()) {
                                if (!text.contains(amenity.toLowerCase())) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
            
            log.info("✅ Found {} properties after filtering", filtered.size());
            
            for (Property p : filtered) {
                log.info("   ✅ Matched: {} | ₹{} | {} | {} BHK", 
                    p.getTitle(), p.getRent(), p.getLocation(), p.getBedrooms());
            }
            
            return filtered;
            
        } catch (Exception e) {
            log.error("❌ Error searching properties: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private String generateExplanation(List<Property> properties, SearchFilters filters) {
        if (properties.isEmpty()) {
            if (filters.getLocation() != null) {
                return "No properties found in " + filters.getLocation() + ". Try expanding your search.";
            }
            return "No properties found matching your criteria. Try adjusting your search.";
        }
        
        String location = filters.getLocation() != null ? " in " + filters.getLocation() : "";
        return String.format("Found %d properties%s that match your needs.", properties.size(), location);
    }

    public boolean isAvailable() {
        return ollamaService.isOllamaRunning();
    }
}