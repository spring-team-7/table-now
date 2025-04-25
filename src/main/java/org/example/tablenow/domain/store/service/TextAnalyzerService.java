package org.example.tablenow.domain.store.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextAnalyzerService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.elasticsearch.uris}")
    private String elasticUrl;

    // Elastic analyzer 호출
    public Set<String> analyzeText(String indexName, String analyzer, String text) {
        String endpoint = elasticUrl + "/" + indexName + "/_analyze";

        String requestBody = String.format("{\"analyzer\": \"%s\", \"text\": \"%s\"}", analyzer, text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode tokens = root.get("tokens");

                Set<String> result = new HashSet<>();
                for (JsonNode token : tokens) {
                    result.add(token.get("token").asText());
                }
                return result;
            }
        } catch (JsonProcessingException e) {
            log.error("Elasticsearch analyzer 호출 실패", e);
        }

        return Collections.emptySet();
    }
}
