package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.context.GenerationContext;
import com.batty.forgex.generator.template.TemplateManager;
import com.batty.forgex.generator.template.MustacheTemplateManager;
import com.github.mustachejava.Mustache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class EntityCodeGenerator implements CodeGenerator {
    private static final Logger log = LoggerFactory.getLogger(EntityCodeGenerator.class);
    private final TemplateManager templateManager;

    @Autowired
    public EntityCodeGenerator(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void generate(String outputDir, GenerationContext context) {
        try {
            for (GenerationContext.EntityDefinition entity : context.getEntities()) {
                generateEntityClass(outputDir, entity, context);
            }
        } catch (IOException e) {
            log.error("Failed to generate entity code", e);
            throw new RuntimeException("Failed to generate entity code", e);
        }
    }

    private void generateEntityClass(String outputDir, GenerationContext.EntityDefinition entity, GenerationContext context) throws IOException {
        String packageName = context.getProperties().getOrDefault("packageName", "com.example").toString();
        Path outputDirPath = Paths.get(outputDir);
        Path packageDir = outputDirPath.resolve(packageName.replace('.', '/'));
        Files.createDirectories(packageDir);

        Map<String, Object> templateContext = new HashMap<>();
        templateContext.put("packageName", packageName);
        templateContext.put("entityName", entity.getName());
        templateContext.put("fields", entity.getFields());

        Mustache mustache = ((MustacheTemplateManager) templateManager).compileTemplate("entity");
        Path outputFile = packageDir.resolve(entity.getName() + ".java");
        try (FileWriter writer = new FileWriter(outputFile.toFile())) {
            mustache.execute(writer, templateContext).flush();
        }
    }
} 