package org.example.tablenow.global.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.global.constant.OAuthConstants;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthResponseParser {

    private final ObjectMapper objectMapper;

    public String extractAccessToken(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get(OAuthConstants.ACCESS_TOKEN).asText();
        } catch (Exception e) {
            throw new HandledException(ErrorCode.OAUTH_RESPONSE_PARSING_FAILED);
        }
    }
}
