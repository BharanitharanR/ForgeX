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

class RepositoryCodeGeneratorTest {

    private RepositoryCodeGenerator repositoryCodeGenerator;
    private Entity testEntity;
    private Path outputPath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        repositoryCodeGenerator = new RepositoryCodeGenerator();
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
    void generateRepository_ShouldCreateValidRepositoryFile() throws IOException {
        // When
        repositoryCodeGenerator.generateRepository(testEntity, outputPath.toString());

        // Then
        Path repositoryFile = outputPath.resolve("UserRepository.java");
        assertTrue(Files.exists(repositoryFile), "Repository file should be created");
        
        String content = Files.readString(repositoryFile);
        assertTrue(content.contains("@Repository"), "Should contain Repository annotation");
        assertTrue(content.contains("interface UserRepository"), "Should contain correct interface name");
        assertTrue(content.contains("extends JpaRepository"), "Should extend JpaRepository");
    }

    @Test
    void generateRepository_ShouldIncludeCorrectGenericTypes() throws IOException {
        // When
        repositoryCodeGenerator.generateRepository(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserRepository.java"));
        assertTrue(content.contains("JpaRepository<User, Long>"), "Should have correct generic types");
    }

    @Test
    void generateRepository_ShouldHandleEmptyEntity() throws IOException {
        // Given
        Entity emptyEntity = new Entity();
        emptyEntity.setName("Empty");
        emptyEntity.setFields(new ArrayList<>());

        // When
        repositoryCodeGenerator.generateRepository(emptyEntity, outputPath.toString());

        // Then
        Path repositoryFile = outputPath.resolve("EmptyRepository.java");
        assertTrue(Files.exists(repositoryFile), "Repository file should be created for empty entity");
        String content = Files.readString(repositoryFile);
        assertTrue(content.contains("interface EmptyRepository"), "Should create valid repository for empty entity");
    }

    @Test
    void generateRepository_ShouldIncludeCustomQueryMethods() throws IOException {
        // Given
        Field emailField = new Field();
        emailField.setName("email");
        emailField.setType("String");
        testEntity.getFields().add(emailField);

        // When
        repositoryCodeGenerator.generateRepository(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserRepository.java"));
        assertTrue(content.contains("findByEmail"), "Should include custom query method");
    }
} 