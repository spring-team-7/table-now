package org.example.tablenow.global.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tablenow.global.constant.OAuthConstants;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;

public class OAuthResponseParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String extractAccessToken(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get(OAuthConstants.ACCESS_TOKEN).asText();
        } catch (Exception e) {
            throw new HandledException(ErrorCode.OAUTH_RESPONSE_PARSING_FAILED);
        }
    }
}
