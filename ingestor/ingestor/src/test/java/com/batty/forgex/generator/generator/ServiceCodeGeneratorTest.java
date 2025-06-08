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

class ServiceCodeGeneratorTest {

    private ServiceCodeGenerator serviceCodeGenerator;
    private Entity testEntity;
    private Path outputPath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        serviceCodeGenerator = new ServiceCodeGenerator();
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
    void generateService_ShouldCreateValidServiceFile() throws IOException {
        // When
        serviceCodeGenerator.generateService(testEntity, outputPath.toString());

        // Then
        Path serviceFile = outputPath.resolve("UserService.java");
        assertTrue(Files.exists(serviceFile), "Service file should be created");
        
        String content = Files.readString(serviceFile);
        assertTrue(content.contains("@Service"), "Should contain Service annotation");
        assertTrue(content.contains("class UserService"), "Should contain correct class name");
        assertTrue(content.contains("implements UserService"), "Should implement service interface");
    }

    @Test
    void generateService_ShouldIncludeAllCRUDMethods() throws IOException {
        // When
        serviceCodeGenerator.generateService(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserService.java"));
        assertTrue(content.contains("findAll()"), "Should contain findAll method");
        assertTrue(content.contains("findById"), "Should contain findById method");
        assertTrue(content.contains("save"), "Should contain save method");
        assertTrue(content.contains("delete"), "Should contain delete method");
    }

    @Test
    void generateService_ShouldHandleEmptyEntity() throws IOException {
        // Given
        Entity emptyEntity = new Entity();
        emptyEntity.setName("Empty");
        emptyEntity.setFields(Arrays.asList());

        // When
        serviceCodeGenerator.generateService(emptyEntity, outputPath.toString());

        // Then
        Path serviceFile = outputPath.resolve("EmptyService.java");
        assertTrue(Files.exists(serviceFile), "Service file should be created for empty entity");
        String content = Files.readString(serviceFile);
        assertTrue(content.contains("class EmptyService"), "Should create valid service for empty entity");
    }

    @Test
    void generateService_ShouldIncludeCustomMethods() throws IOException {
        // Given
        Field emailField = new Field();
        emailField.setName("email");
        emailField.setType("String");
        testEntity.getFields().add(emailField);

        // When
        serviceCodeGenerator.generateService(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserService.java"));
        assertTrue(content.contains("findByEmail"), "Should include custom query method");
    }

    @Test
    void generateService_ShouldIncludeProperDependencies() throws IOException {
        // When
        serviceCodeGenerator.generateService(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserService.java"));
        assertTrue(content.contains("@Autowired"), "Should include Autowired annotation");
        assertTrue(content.contains("UserRepository"), "Should include repository dependency");
    }
} 