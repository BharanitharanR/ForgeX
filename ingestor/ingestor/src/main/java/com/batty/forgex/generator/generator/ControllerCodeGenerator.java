package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.context.GenerationContext;
import com.batty.forgex.generator.template.MustacheTemplateManager;
import com.batty.forgex.generator.template.TemplateManager;
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
public class ControllerCodeGenerator implements CodeGenerator {
    private static final Logger log = LoggerFactory.getLogger(ControllerCodeGenerator.class);
    private final TemplateManager templateManager;

    @Autowired
    public ControllerCodeGenerator(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void generate(String outputDir, GenerationContext context) {
        try {
            for (GenerationContext.EntityDefinition entity : context.getEntities()) {
                generateControllerClass(outputDir, entity, context);
            }
        } catch (IOException e) {
            log.error("Failed to generate controller code", e);
            throw new RuntimeException("Failed to generate controller code", e);
        }
    }

    private void generateControllerClass(String outputDir, GenerationContext.EntityDefinition entity, GenerationContext context) throws IOException {
        String packageName = context.getProperties().getOrDefault("packageName", "com.example").toString();
        Path outputDirPath = Paths.get(outputDir);
        Path packageDir = outputDirPath.resolve(packageName.replace('.', '/'));
        Files.createDirectories(packageDir);

        Map<String, Object> templateContext = new HashMap<>();
        templateContext.put("packageName", packageName);
        templateContext.put("entityName", entity.getName());
        templateContext.put("entityNameLower", entity.getName().toLowerCase());

        Mustache mustache = ((MustacheTemplateManager) templateManager).compileTemplate("controller");
        Path outputFile = packageDir.resolve(entity.getName() + "Controller.java");
        try (FileWriter writer = new FileWriter(outputFile.toFile())) {
            mustache.execute(writer, templateContext).flush();
        }
    }
} 