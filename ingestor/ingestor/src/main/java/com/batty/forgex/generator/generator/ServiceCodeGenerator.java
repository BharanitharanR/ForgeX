package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.model.Entity;
import com.batty.forgex.generator.model.Field;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ServiceCodeGenerator {
    
    public void generateService(Entity entity, String outputPath) throws IOException {
        String fileName = outputPath + "/" + entity.getName() + "Service.java";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Write package and imports
            writer.println("package com.batty.forgex.service;");
            writer.println();
            writer.println("import org.springframework.beans.factory.annotation.Autowired;");
            writer.println("import org.springframework.stereotype.Service;");
            writer.println("import com.batty.forgex.model." + entity.getName() + ";");
            writer.println("import com.batty.forgex.repository." + entity.getName() + "Repository;");
            writer.println("import java.util.List;");
            writer.println("import java.util.Optional;");
            writer.println();
            
            // Write service interface
            writer.println("public interface " + entity.getName() + "Service {");
            writer.println("    List<" + entity.getName() + "> findAll();");
            writer.println("    Optional<" + entity.getName() + "> findById(" + getPrimaryKeyType(entity) + " id);");
            writer.println("    " + entity.getName() + " save(" + entity.getName() + " " + 
                          entity.getName().toLowerCase() + ");");
            writer.println("    void deleteById(" + getPrimaryKeyType(entity) + " id);");
            
            // Add custom methods based on fields
            for (Field field : entity.getFields()) {
                if (!field.isPrimaryKey()) {
                    writer.println("    Optional<" + entity.getName() + "> findBy" + 
                                 capitalize(field.getName()) + "(" + field.getType() + " " + 
                                 field.getName() + ");");
                }
            }
            
            writer.println("}");
            writer.println();
            
            // Write service implementation
            writer.println("@Service");
            writer.println("public class " + entity.getName() + "ServiceImpl implements " + 
                          entity.getName() + "Service {");
            writer.println();
            writer.println("    @Autowired");
            writer.println("    private " + entity.getName() + "Repository " + 
                          entity.getName().toLowerCase() + "Repository;");
            writer.println();
            
            // Implement interface methods
            writer.println("    @Override");
            writer.println("    public List<" + entity.getName() + "> findAll() {");
            writer.println("        return " + entity.getName().toLowerCase() + "Repository.findAll();");
            writer.println("    }");
            writer.println();
            
            writer.println("    @Override");
            writer.println("    public Optional<" + entity.getName() + "> findById(" + 
                          getPrimaryKeyType(entity) + " id) {");
            writer.println("        return " + entity.getName().toLowerCase() + "Repository.findById(id);");
            writer.println();
            
            writer.println("    @Override");
            writer.println("    public " + entity.getName() + " save(" + entity.getName() + " " + 
                          entity.getName().toLowerCase() + ") {");
            writer.println("        return " + entity.getName().toLowerCase() + 
                          "Repository.save(" + entity.getName().toLowerCase() + ");");
            writer.println("    }");
            writer.println();
            
            writer.println("    @Override");
            writer.println("    public void deleteById(" + getPrimaryKeyType(entity) + " id) {");
            writer.println("        " + entity.getName().toLowerCase() + "Repository.deleteById(id);");
            writer.println();
            
            // Implement custom methods
            for (Field field : entity.getFields()) {
                if (!field.isPrimaryKey()) {
                    writer.println("    @Override");
                    writer.println("    public Optional<" + entity.getName() + "> findBy" + 
                                 capitalize(field.getName()) + "(" + field.getType() + " " + 
                                 field.getName() + ") {");
                    writer.println("        return Optional.ofNullable(" + 
                                 entity.getName().toLowerCase() + "Repository.findBy" + 
                                 capitalize(field.getName()) + "(" + field.getName() + "));");
                    writer.println("    }");
                    writer.println();
                }
            }
            
            writer.println("}");
        }
    }
    
    private String getPrimaryKeyType(Entity entity) {
        return entity.getFields().stream()
                .filter(Field::isPrimaryKey)
                .findFirst()
                .map(Field::getType)
                .orElse("Long");
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
} 