package org.example.tablenow.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tablenow.global.constant.OAuthConstants;

import static org.mockito.Mockito.when;

public class OAuthTestUtil {

    public static String createAccessTokenJson(String token) {
        return "{\"" + OAuthConstants.ACCESS_TOKEN + "\":\"" + token + "\"}";
    }

    public static void stubJsonParsing(ObjectMapper objectMapper, String json) throws Exception {
        when(objectMapper.readTree(json)).thenReturn(new ObjectMapper().readTree(json));
    }
}
