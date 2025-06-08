package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.model.Entity;
import com.batty.forgex.generator.model.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ValidationGeneratorTest {

    private ValidationGenerator validationGenerator;
    private Entity testEntity;
    private Path outputPath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        validationGenerator = new ValidationGenerator();
        outputPath = tempDir;
        
        // Create a test entity
        testEntity = new Entity();
        testEntity.setName("User");
        testEntity.setTableName("users");
        
        // Add fields
        Field idField = new Field();
        idField.setName("id");
        idField.setType("Long");
        idField.setPrimaryKey(true);
        
        Field nameField = new Field();
        nameField.setName("name");
        nameField.setType("String");
        
        testEntity.setFields(Arrays.asList(idField, nameField));
    }

    @Test
    void generateValidation_ShouldCreateValidValidationFile() throws IOException {
        // When
        validationGenerator.generateValidation(testEntity, outputPath.toString());

        // Then
        Path validationFile = outputPath.resolve("UserValidation.java");
        assertTrue(Files.exists(validationFile), "Validation file should be created");
        
        String content = Files.readString(validationFile);
        assertTrue(content.contains("class UserValidation"), "Should contain correct class name");
        assertTrue(content.contains("public static void validate"), "Should contain validation method");
    }

    @Test
    void generateValidation_ShouldIncludeAllFieldValidations() throws IOException {
        // When
        validationGenerator.generateValidation(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserValidation.java"));
        assertTrue(content.contains("validateName"), "Should contain name validation");
        assertTrue(content.contains("notNull"), "Should contain not null validation");
        assertTrue(content.contains("notBlank"), "Should contain not blank validation");
    }

    @Test
    void generateValidation_ShouldHandleEmptyEntity() throws IOException {
        // Given
        Entity emptyEntity = new Entity();
        emptyEntity.setName("Empty");
        emptyEntity.setFields(Arrays.asList());

        // When
        validationGenerator.generateValidation(emptyEntity, outputPath.toString());

        // Then
        Path validationFile = outputPath.resolve("EmptyValidation.java");
        assertTrue(Files.exists(validationFile), "Validation file should be created for empty entity");
        String content = Files.readString(validationFile);
        assertTrue(content.contains("class EmptyValidation"), "Should create valid validation for empty entity");
    }

    @Test
    void generateValidation_ShouldHandleEntityWithCustomValidations() throws IOException {
        // Given
        Field emailField = new Field();
        emailField.setName("email");
        emailField.setType("String");
        emailField.setAnnotations(Arrays.asList("@Email", "@NotBlank"));
        testEntity.getFields().add(emailField);

        // When
        validationGenerator.generateValidation(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserValidation.java"));
        assertTrue(content.contains("validateEmail"), "Should include email validation");
        assertTrue(content.contains("isValidEmail"), "Should include email format validation");
    }

    @Test
    void generateValidation_ShouldIncludeProperImports() throws IOException {
        // When
        validationGenerator.generateValidation(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserValidation.java"));
        assertTrue(content.contains("import org.springframework.util.StringUtils;"), 
                  "Should import StringUtils");
        assertTrue(content.contains("import com.batty.forgex.exception"), 
                  "Should import exception package");
    }
} 