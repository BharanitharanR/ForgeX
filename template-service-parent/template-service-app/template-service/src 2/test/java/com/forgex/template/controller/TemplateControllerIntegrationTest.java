package com.forgex.template.controller;

import com.forgex.template.model.Template;
import com.forgex.template.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TemplateControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TemplateRepository templateRepository;

    @BeforeEach
    void setUp() {
        templateRepository.deleteAll();
    }

    @Test
    void shouldCreateTemplate() throws Exception {
        Template template = createSampleTemplate();
        String templateJson = objectMapper.writeValueAsString(template);

        mockMvc.perform(post("/api/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(templateJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(template.getName()))
                .andExpect(jsonPath("$.content").value(template.getContent()));
    }

    @Test
    void shouldGetTemplateById() throws Exception {
        Template template = templateRepository.save(createSampleTemplate());

        mockMvc.perform(get("/api/templates/{id}", template.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(template.getId()))
                .andExpect(jsonPath("$.name").value(template.getName()));
    }

    @Test
    void shouldReturn404WhenTemplateNotFound() throws Exception {
        mockMvc.perform(get("/api/templates/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateTemplate() throws Exception {
        Template template = templateRepository.save(createSampleTemplate());
        template.setName("Updated Name");
        String templateJson = objectMapper.writeValueAsString(template);

        mockMvc.perform(put("/api/templates/{id}", template.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(templateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void shouldDeleteTemplate() throws Exception {
        Template template = templateRepository.save(createSampleTemplate());

        mockMvc.perform(delete("/api/templates/{id}", template.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/templates/{id}", template.getId()))
                .andExpect(status().isNotFound());
    }

    private Template createSampleTemplate() {
        Template template = new Template();
        template.setName("Test Template");
        template.setContent("Template content");
        template.setInputSchema("{\"type\": \"object\"}");
        template.setOutputSchema("{\"type\": \"string\"}");
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("type", "code");
        metadata.put("category", "java");
        template.setMetadata(metadata);
        
        return template;
    }
} 