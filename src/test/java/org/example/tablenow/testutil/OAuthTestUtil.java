package org.example.tablenow.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;

public class OAuthTestUtil {

    public static String createAccessTokenJson(String token) {
        return "{\"access_token\":\"" + token + "\"}";
    }

    public static void stubJsonParsing(ObjectMapper objectMapper, String json) throws Exception {
        when(objectMapper.readTree(json)).thenReturn(new ObjectMapper().readTree(json));
    }
}
