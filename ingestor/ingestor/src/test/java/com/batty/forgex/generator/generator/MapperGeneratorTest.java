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

class MapperGeneratorTest {

    private MapperGenerator mapperGenerator;
    private Entity testEntity;
    private Path outputPath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        mapperGenerator = new MapperGenerator();
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
    void generateMapper_ShouldCreateValidMapperFile() throws IOException {
        // When
        mapperGenerator.generateMapper(testEntity, outputPath.toString());

        // Then
        Path mapperFile = outputPath.resolve("UserMapper.java");
        assertTrue(Files.exists(mapperFile), "Mapper file should be created");
        
        String content = Files.readString(mapperFile);
        assertTrue(content.contains("@Mapper"), "Should contain Mapper annotation");
        assertTrue(content.contains("interface UserMapper"), "Should contain correct interface name");
    }

    @Test
    void generateMapper_ShouldIncludeAllMappingMethods() throws IOException {
        // When
        mapperGenerator.generateMapper(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserMapper.java"));
        assertTrue(content.contains("UserDTO toDTO(User user)"), "Should contain toDTO method");
        assertTrue(content.contains("User toEntity(UserDTO dto)"), "Should contain toEntity method");
        assertTrue(content.contains("List<UserDTO> toDTOList(List<User> users)"), "Should contain toDTOList method");
        assertTrue(content.contains("List<User> toEntityList(List<UserDTO> dtos)"), "Should contain toEntityList method");
    }

    @Test
    void generateMapper_ShouldHandleEmptyEntity() throws IOException {
        // Given
        Entity emptyEntity = new Entity();
        emptyEntity.setName("Empty");
        emptyEntity.setFields(Arrays.asList());

        // When
        mapperGenerator.generateMapper(emptyEntity, outputPath.toString());

        // Then
        Path mapperFile = outputPath.resolve("EmptyMapper.java");
        assertTrue(Files.exists(mapperFile), "Mapper file should be created for empty entity");
        String content = Files.readString(mapperFile);
        assertTrue(content.contains("interface EmptyMapper"), "Should create valid mapper for empty entity");
    }

    @Test
    void generateMapper_ShouldIncludeProperImports() throws IOException {
        // When
        mapperGenerator.generateMapper(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserMapper.java"));
        assertTrue(content.contains("import org.mapstruct.Mapper"), "Should import MapStruct Mapper");
        assertTrue(content.contains("import com.batty.forgex.model.User"), "Should import entity");
        assertTrue(content.contains("import com.batty.forgex.dto.UserDTO"), "Should import DTO");
    }

    @Test
    void generateMapper_ShouldHandleEntityWithRelationships() throws IOException {
        // Given
        Field roleField = new Field();
        roleField.setName("role");
        roleField.setType("Role");
        testEntity.getFields().add(roleField);

        // When
        mapperGenerator.generateMapper(testEntity, outputPath.toString());

        // Then
        String content = Files.readString(outputPath.resolve("UserMapper.java"));
        assertTrue(content.contains("RoleDTO roleToRoleDTO(Role role)"), "Should include relationship mapping method");
        assertTrue(content.contains("Role roleDTOToRole(RoleDTO roleDTO)"), "Should include relationship mapping method");
    }
} 