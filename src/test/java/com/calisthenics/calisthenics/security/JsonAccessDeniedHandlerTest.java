package com.calisthenics.calisthenics.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class JsonAccessDeniedHandlerTest {

    @Test
    void handle_devuelve403ConErrorResponseJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JsonAccessDeniedHandler handler = new JsonAccessDeniedHandler(mapper);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("x"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        JsonNode json = mapper.readTree(response.getContentAsString());
        assertThat(json.get("status").asInt()).isEqualTo(403);
        assertThat(json.get("message").asText()).isEqualTo("Access denied");
        assertThat(json.has("timestamp")).isTrue();
    }
}
