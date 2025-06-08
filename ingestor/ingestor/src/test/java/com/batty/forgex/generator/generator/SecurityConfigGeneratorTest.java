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

class SecurityConfigGeneratorTest {

    private SecurityConfigGenerator securityConfigGenerator;
    private Entity testEntity;
    private Path outputPath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        securityConfigGenerator = new SecurityConfigGenerator();
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
    void generateSecurityConfig_ShouldCreateValidSecurityConfigFile() throws IOException {
        // When
        securityConfigGenerator.generateSecurityConfig(testEntity, outputPath.toString());

        // Then
        Path securityConfigFile = outputPath.resolve("SecurityConfig.java");
        assertTrue(Files.exists(securityConfigFile), "SecurityConfig file should be created");
        
        String content = Files.readString(securityConfigFile);
        assertTrue(content.contains("@Configuration"), "Should contain Configuration annotation");
        assertTrue(content.contains("@EnableWebSecurity"), "Should contain EnableWebSecurity annotation");
        assertTrue(content.contains("class SecurityConfig"), "Should contain correct class name");
    }

    @Test
    void generateSecurityConfig_ShouldIncludeAllSecurityConfigurations() throws IOException {
        // When
        securityConfigGenerator.generateSecurityConfig(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("SecurityConfig.java"));
        assertTrue(content.contains("configure(HttpSecurity http)"), "Should contain HTTP security configuration");
        assertTrue(content.contains("configure(AuthenticationManagerBuilder auth)"), 
                  "Should contain authentication configuration");
        assertTrue(content.contains("passwordEncoder()"), "Should contain password encoder bean");
        assertTrue(content.contains("authenticationManagerBean()"), "Should contain authentication manager bean");
    }

    @Test
    void generateSecurityConfig_ShouldIncludeEntitySpecificSecurity() throws IOException {
        // When
        securityConfigGenerator.generateSecurityConfig(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("SecurityConfig.java"));
        assertTrue(content.contains("/api/users/**"), "Should include entity-specific endpoints");
        assertTrue(content.contains("hasRole('ADMIN')"), "Should include role-based security");
    }

    @Test
    void generateSecurityConfig_ShouldIncludeProperImports() throws IOException {
        // When
        securityConfigGenerator.generateSecurityConfig(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("SecurityConfig.java"));
        assertTrue(content.contains("import org.springframework.security.config.annotation.web.builders.HttpSecurity;"), 
                  "Should import HttpSecurity");
        assertTrue(content.contains("import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;"), 
                  "Should import EnableWebSecurity");
        assertTrue(content.contains("import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;"), 
                  "Should import BCryptPasswordEncoder");
    }

    @Test
    void generateSecurityConfig_ShouldIncludeCorsConfiguration() throws IOException {
        // When
        securityConfigGenerator.generateSecurityConfig(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("SecurityConfig.java"));
        assertTrue(content.contains("cors()"), "Should include CORS configuration");
        assertTrue(content.contains("csrf()"), "Should include CSRF configuration");
    }

    @Test
    void generateSecurityConfig_ShouldIncludeJwtConfiguration() throws IOException {
        // When
        securityConfigGenerator.generateSecurityConfig(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("SecurityConfig.java"));
        assertTrue(content.contains("JwtAuthenticationFilter"), "Should include JWT filter");
        assertTrue(content.contains("JwtAuthorizationFilter"), "Should include JWT authorization");
    }
} 