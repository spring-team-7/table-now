package org.example.tablenow.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        Map<String, Object> responseContent = new LinkedHashMap<>();
        responseContent.put("status", status.name());
        responseContent.put("code", status.value());
        responseContent.put("message", message);

        String jsonResponse = objectMapper.writeValueAsString(responseContent);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status.value());
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
