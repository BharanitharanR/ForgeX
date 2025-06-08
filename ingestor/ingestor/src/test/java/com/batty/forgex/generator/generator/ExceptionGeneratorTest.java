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

class ExceptionGeneratorTest {

    private ExceptionGenerator exceptionGenerator;
    private Entity testEntity;
    private Path outputPath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        exceptionGenerator = new ExceptionGenerator();
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
    void generateExceptions_ShouldCreateValidExceptionFiles() throws IOException {
        // When
        exceptionGenerator.generateExceptions(testEntity, outputPath.toString());

        // Then
        Path notFoundFile = outputPath.resolve("UserNotFoundException.java");
        Path alreadyExistsFile = outputPath.resolve("UserAlreadyExistsException.java");
        
        assertTrue(Files.exists(notFoundFile), "NotFoundException file should be created");
        assertTrue(Files.exists(alreadyExistsFile), "AlreadyExistsException file should be created");
    }

    @Test
    void generateExceptions_ShouldCreateValidNotFoundException() throws IOException {
        // When
        exceptionGenerator.generateExceptions(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserNotFoundException.java"));
        assertTrue(content.contains("class UserNotFoundException"), "Should contain correct class name");
        assertTrue(content.contains("extends RuntimeException"), "Should extend RuntimeException");
        assertTrue(content.contains("public UserNotFoundException"), "Should contain constructor");
        assertTrue(content.contains("super(\"User not found with id: \" + id);"), "Should contain error message");
    }

    @Test
    void generateExceptions_ShouldCreateValidAlreadyExistsException() throws IOException {
        // When
        exceptionGenerator.generateExceptions(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserAlreadyExistsException.java"));
        assertTrue(content.contains("class UserAlreadyExistsException"), "Should contain correct class name");
        assertTrue(content.contains("extends RuntimeException"), "Should extend RuntimeException");
        assertTrue(content.contains("public UserAlreadyExistsException"), "Should contain constructor");
        assertTrue(content.contains("super(\"User already exists with id: \" + id);"), "Should contain error message");
    }

    @Test
    void generateExceptions_ShouldHandleEmptyEntity() throws IOException {
        // Given
        Entity emptyEntity = new Entity();
        emptyEntity.setName("Empty");
        emptyEntity.setFields(Arrays.asList());

        // When
        exceptionGenerator.generateExceptions(emptyEntity, outputPath.toString());

        // Then
        Path notFoundFile = outputPath.resolve("EmptyNotFoundException.java");
        Path alreadyExistsFile = outputPath.resolve("EmptyAlreadyExistsException.java");
        
        assertTrue(Files.exists(notFoundFile), "NotFoundException file should be created for empty entity");
        assertTrue(Files.exists(alreadyExistsFile), "AlreadyExistsException file should be created for empty entity");
    }

    @Test
    void generateExceptions_ShouldIncludeProperImports() throws IOException {
        // When
        exceptionGenerator.generateExceptions(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserNotFoundException.java"));
        assertTrue(content.contains("package com.batty.forgex.exception;"), "Should have correct package");
        assertTrue(content.contains("import org.springframework.http.HttpStatus;"), "Should import HttpStatus");
        assertTrue(content.contains("import org.springframework.web.bind.annotation.ResponseStatus;"), 
                  "Should import ResponseStatus");
    }
} 