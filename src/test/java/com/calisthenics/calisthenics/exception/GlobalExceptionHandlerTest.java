package com.calisthenics.calisthenics.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @RestController
    static class ThrowingController {
        @GetMapping("/forbidden-operation")
        void forbiddenOperation() {
            throw new ForbiddenOperationException("No permitido");
        }

        @GetMapping("/access-denied")
        void accessDenied() {
            throw new AccessDeniedException("No permitido");
        }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void forbiddenOperationException_devuelve403Json() throws Exception {
        mockMvc.perform(get("/forbidden-operation"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("No permitido"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void accessDeniedException_devuelve403JsonConElMismoFormato() throws Exception {
        mockMvc.perform(get("/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("No permitido"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
