package com.forgex.template.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

class TemplateTest {

    @Test
    void testTemplateCreation() {
        // Given
        Template template = new Template();
        template.setName("Test Template");
        template.setContent("Template content");
        template.setInputSchema("{\"type\": \"object\"}");
        template.setOutputSchema("{\"type\": \"string\"}");
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("type", "code");
        metadata.put("category", "java");
        template.setMetadata(metadata);

        // When
        LocalDateTime now = LocalDateTime.now();
        template.onCreate();

        // Then
        assertThat(template.getName()).isEqualTo("Test Template");
        assertThat(template.getContent()).isEqualTo("Template content");
        assertThat(template.getInputSchema()).isEqualTo("{\"type\": \"object\"}");
        assertThat(template.getOutputSchema()).isEqualTo("{\"type\": \"string\"}");
        assertThat(template.getMetadata()).containsEntry("type", "code");
        assertThat(template.getMetadata()).containsEntry("category", "java");
        assertThat(template.getCreatedAt()).isNotNull();
        assertThat(template.getUpdatedAt()).isNull();
    }

    @Test
    void testTemplateUpdate() {
        // Given
        Template template = new Template();
        template.setName("Original Name");
        template.onCreate();

        // When
        template.setName("Updated Name");
        template.onUpdate();

        // Then
        assertThat(template.getName()).isEqualTo("Updated Name");
        assertThat(template.getUpdatedAt()).isNotNull();
    }
} 