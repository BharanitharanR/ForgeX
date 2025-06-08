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

class ControllerCodeGeneratorTest {

    private ControllerCodeGenerator controllerCodeGenerator;
    private MustacheTemplateManager templateManager;
    private GenerationContext context;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        templateManager = new MustacheTemplateManager();
        controllerCodeGenerator = new ControllerCodeGenerator(templateManager);
        
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
        properties.put("basePath", "users");
        
        GenerationContext.Builder builder = new GenerationContext.Builder();
        builder.serviceName("user-service")
               .entities(Arrays.asList(entity))
               .properties(properties);
        context = builder.build();
    }

    @Test
    void generate_ShouldCreateValidControllerFile() throws IOException {
        String outputPath = tempDir.toString();
        controllerCodeGenerator.generate(outputPath, context);
        
        Path controllerFile = tempDir.resolve("com/example/UserController.java");
        assertTrue(controllerFile.toFile().exists(), "Controller file should be created");
    }

    @Test
    void generate_ShouldIncludeAllCRUDEndpoints() throws IOException {
        String outputPath = tempDir.toString();
        controllerCodeGenerator.generate(outputPath, context);
        
        String content = new String(java.nio.file.Files.readAllBytes(tempDir.resolve("com/example/UserController.java")));
        assertTrue(content.contains("@GetMapping"), "Should contain GET endpoint");
        assertTrue(content.contains("@PostMapping"), "Should contain POST endpoint");
        assertTrue(content.contains("@PutMapping"), "Should contain PUT endpoint");
        assertTrue(content.contains("@DeleteMapping"), "Should contain DELETE endpoint");
    }

    @Test
    void generate_ShouldIncludeProperImports() throws IOException {
        String outputPath = tempDir.toString();
        controllerCodeGenerator.generate(outputPath, context);
        
        String content = new String(java.nio.file.Files.readAllBytes(tempDir.resolve("com/example/UserController.java")));
        assertTrue(content.contains("import org.springframework.web.bind.annotation"), "Should contain Spring Web imports");
        assertTrue(content.contains("import org.springframework.beans.factory.annotation"), "Should contain Spring annotations");
    }

    @Test
    void generate_ShouldIncludeServiceDependency() throws IOException {
        String outputPath = tempDir.toString();
        controllerCodeGenerator.generate(outputPath, context);
        
        String content = new String(java.nio.file.Files.readAllBytes(tempDir.resolve("com/example/UserController.java")));
        assertTrue(content.contains("@Autowired"), "Should contain Autowired annotation");
        assertTrue(content.contains("UserService"), "Should contain service dependency");
    }
} 