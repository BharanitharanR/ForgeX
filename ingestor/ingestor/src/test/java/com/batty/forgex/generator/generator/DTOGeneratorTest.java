package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.model.Entity;
import com.batty.forgex.generator.model.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DTOGeneratorTest {

    private DTOGenerator dtoGenerator;
    private Entity testEntity;
    private Path outputPath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        dtoGenerator = new DTOGenerator();
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
        
        testEntity.setFields(new ArrayList<>(Arrays.asList(idField, nameField)));
    }

    @Test
    void generateDTO_ShouldCreateValidDTOFile() throws IOException {
        // When
        dtoGenerator.generateDTO(testEntity, outputPath.toString());

        // Then
        Path dtoFile = outputPath.resolve("UserDTO.java");
        assertTrue(Files.exists(dtoFile), "DTO file should be created");
        
        String content = Files.readString(dtoFile);
        assertTrue(content.contains("class UserDTO"), "Should contain correct class name");
        assertTrue(content.contains("private Long id"), "Should contain id field");
        assertTrue(content.contains("private String name"), "Should contain name field");
    }

    @Test
    void generateDTO_ShouldIncludeAllFields() throws IOException {
        // When
        dtoGenerator.generateDTO(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserDTO.java"));
        assertTrue(content.contains("private Long id"), "Should contain id field");
        assertTrue(content.contains("private String name"), "Should contain name field");
        assertTrue(content.contains("public Long getId()"), "Should contain getter for id");
        assertTrue(content.contains("public void setId(Long id)"), "Should contain setter for id");
        assertTrue(content.contains("public String getName()"), "Should contain getter for name");
        assertTrue(content.contains("public void setName(String name)"), "Should contain setter for name");
    }

    @Test
    void generateDTO_ShouldHandleEmptyEntity() throws IOException {
        // Given
        Entity emptyEntity = new Entity();
        emptyEntity.setName("Empty");
        emptyEntity.setFields(new ArrayList<>());

        // When
        dtoGenerator.generateDTO(emptyEntity, outputPath.toString());

        // Then
        Path dtoFile = outputPath.resolve("EmptyDTO.java");
        assertTrue(Files.exists(dtoFile), "DTO file should be created for empty entity");
        String content = Files.readString(dtoFile);
        assertTrue(content.contains("class EmptyDTO"), "Should create valid DTO for empty entity");
    }

    @Test
    void generateDTO_ShouldHandleEntityWithCustomAnnotations() throws IOException {
        // Given
        Field emailField = new Field();
        emailField.setName("email");
        emailField.setType("String");
        emailField.setAnnotations(new ArrayList<>(Arrays.asList("@Email", "@NotBlank")));
        testEntity.getFields().add(emailField);

        // When
        dtoGenerator.generateDTO(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserDTO.java"));
        assertTrue(content.contains("@Email"), "Should include custom annotation");
        assertTrue(content.contains("@NotBlank"), "Should include custom annotation");
        assertTrue(content.contains("private String email"), "Should include custom field");
    }

    @Test
    void generateDTO_ShouldIncludeBuilderPattern() throws IOException {
        // When
        dtoGenerator.generateDTO(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserDTO.java"));
        assertTrue(content.contains("@Builder"), "Should include Builder annotation");
        assertTrue(content.contains("public static class UserDTOBuilder"), "Should include builder class");
    }
} 