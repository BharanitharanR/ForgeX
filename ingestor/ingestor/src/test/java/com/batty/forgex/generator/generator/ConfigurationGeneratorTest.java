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

class ConfigurationGeneratorTest {

    private ConfigurationGenerator configurationGenerator;
    private Entity testEntity;
    private Path outputPath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        configurationGenerator = new ConfigurationGenerator();
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
    void generateConfiguration_ShouldCreateValidConfigurationFile() throws IOException {
        // When
        configurationGenerator.generateConfiguration(testEntity, outputPath.toString());

        // Then
        Path configFile = outputPath.resolve("UserConfiguration.java");
        assertTrue(Files.exists(configFile), "Configuration file should be created");
        
        String content = Files.readString(configFile);
        assertTrue(content.contains("@Configuration"), "Should contain Configuration annotation");
        assertTrue(content.contains("class UserConfiguration"), "Should contain correct class name");
    }

    @Test
    void generateConfiguration_ShouldIncludeAllRequiredBeans() throws IOException {
        // When
        configurationGenerator.generateConfiguration(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserConfiguration.java"));
        assertTrue(content.contains("@Bean"), "Should contain Bean annotation");
        assertTrue(content.contains("UserService"), "Should contain service bean");
        assertTrue(content.contains("UserRepository"), "Should contain repository bean");
        assertTrue(content.contains("UserMapper"), "Should contain mapper bean");
    }

    @Test
    void generateConfiguration_ShouldHandleEmptyEntity() throws IOException {
        // Given
        Entity emptyEntity = new Entity();
        emptyEntity.setName("Empty");
        emptyEntity.setFields(Arrays.asList());

        // When
        configurationGenerator.generateConfiguration(emptyEntity, outputPath.toString());

        // Then
        Path configFile = outputPath.resolve("EmptyConfiguration.java");
        assertTrue(Files.exists(configFile), "Configuration file should be created for empty entity");
        String content = Files.readString(configFile);
        assertTrue(content.contains("class EmptyConfiguration"), "Should create valid configuration for empty entity");
    }

    @Test
    void generateConfiguration_ShouldIncludeProperImports() throws IOException {
        // When
        configurationGenerator.generateConfiguration(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserConfiguration.java"));
        assertTrue(content.contains("import org.springframework.context.annotation.Bean;"), 
                  "Should import Bean annotation");
        assertTrue(content.contains("import org.springframework.context.annotation.Configuration;"), 
                  "Should import Configuration annotation");
        assertTrue(content.contains("import com.batty.forgex.service"), 
                  "Should import service package");
        assertTrue(content.contains("import com.batty.forgex.repository"), 
                  "Should import repository package");
        assertTrue(content.contains("import com.batty.forgex.mapper"), 
                  "Should import mapper package");
    }

    @Test
    void generateConfiguration_ShouldIncludeProperBeanDefinitions() throws IOException {
        // When
        configurationGenerator.generateConfiguration(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserConfiguration.java"));
        assertTrue(content.contains("public UserService userService"), "Should define service bean");
        assertTrue(content.contains("public UserRepository userRepository"), "Should define repository bean");
        assertTrue(content.contains("public UserMapper userMapper"), "Should define mapper bean");
    }
} 