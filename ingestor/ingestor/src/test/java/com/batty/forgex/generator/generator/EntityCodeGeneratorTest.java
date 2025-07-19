package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.context.GenerationContext;
import com.batty.forgex.generator.template.MustacheTemplateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EntityCodeGeneratorTest {

    private EntityCodeGenerator entityCodeGenerator;
    private MustacheTemplateManager templateManager;
    private GenerationContext context;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        templateManager = new MustacheTemplateManager();
        entityCodeGenerator = new EntityCodeGenerator(templateManager);
        
        // Create a test entity
        GenerationContext.EntityDefinition entity = new GenerationContext.EntityDefinition();
        entity.setName("User");
        
        // Add fields
        GenerationContext.FieldDefinition idField = new GenerationContext.FieldDefinition();
        idField.setName("id");
        idField.setType("String");
        idField.setRequired(true);
        
        GenerationContext.FieldDefinition nameField = new GenerationContext.FieldDefinition();
        nameField.setName("name");
        nameField.setType("String");
        nameField.setRequired(true);
        
        entity.setFields(Arrays.asList(idField, nameField));
        
        // Create context
        Map<String, Object> properties = new HashMap<>();
        properties.put("packageName", "com.example");
        
        GenerationContext.Builder builder = new GenerationContext.Builder();
        builder.serviceName("user-service")
               .entities(Arrays.asList(entity))
               .properties(properties);
        context = builder.build();
    }

    @Test
    void generate_ShouldCreateValidEntityFile() throws IOException {
        String outputPath = tempDir.toString();
        entityCodeGenerator.generate(outputPath, context);
        
        Path entityFile = tempDir.resolve("com/example/User.java");
        assertTrue(entityFile.toFile().exists(), "Entity file should be created");
    }

    @Test
    void generate_ShouldIncludeAllFields() throws IOException {
        String outputPath = tempDir.toString();
        entityCodeGenerator.generate(outputPath, context);
        
        String content = new String(java.nio.file.Files.readAllBytes(tempDir.resolve("com/example/User.java")));
        assertTrue(content.contains("private String id"), "Should contain id field");
        assertTrue(content.contains("private String name"), "Should contain name field");
    }

    @Test
    void generate_ShouldIncludeGettersAndSetters() throws IOException {
        String outputPath = tempDir.toString();
        entityCodeGenerator.generate(outputPath, context);
        
        String content = new String(java.nio.file.Files.readAllBytes(tempDir.resolve("com/example/User.java")));
        assertTrue(content.contains("@Data"), "Should include Lombok @Data annotation");
    }

    @Test
    void generate_ShouldIncludeProperImports() throws IOException {
        String outputPath = tempDir.toString();
        entityCodeGenerator.generate(outputPath, context);
        
        String content = new String(java.nio.file.Files.readAllBytes(tempDir.resolve("com/example/User.java")));
        assertTrue(content.contains("import org.springframework.data.annotation.Id"), "Should import Id annotation");
        assertTrue(content.contains("import org.springframework.data.mongodb.core.mapping.Document"), "Should import Document annotation");
        assertTrue(content.contains("import lombok.Data"), "Should import Lombok Data annotation");
    }
} 