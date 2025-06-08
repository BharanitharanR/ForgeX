package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.model.Entity;
import com.batty.forgex.generator.model.Field;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class RepositoryCodeGenerator {
    
    public void generateRepository(Entity entity, String outputPath) throws IOException {
        String fileName = outputPath + "/" + entity.getName() + "Repository.java";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Write package and imports
            writer.println("package com.batty.forgex.repository;");
            writer.println();
            writer.println("import org.springframework.data.jpa.repository.JpaRepository;");
            writer.println("import org.springframework.stereotype.Repository;");
            writer.println("import com.batty.forgex.model." + entity.getName() + ";");
            writer.println();
            
            // Write repository interface
            writer.println("@Repository");
            writer.println("public interface " + entity.getName() + "Repository extends JpaRepository<" + 
                          entity.getName() + ", " + getPrimaryKeyType(entity) + "> {");
            writer.println();
            
            // Add custom query methods based on fields
            for (Field field : entity.getFields()) {
                if (!field.isPrimaryKey()) {
                    writer.println("    " + entity.getName() + " findBy" + 
                                 capitalize(field.getName()) + "(" + field.getType() + " " + 
                                 field.getName() + ");");
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