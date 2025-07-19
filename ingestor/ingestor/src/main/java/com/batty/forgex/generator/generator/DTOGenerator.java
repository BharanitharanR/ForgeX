package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.model.Entity;
import com.batty.forgex.generator.model.Field;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class DTOGenerator {
    
    public void generateDTO(Entity entity, String outputPath) throws IOException {
        String fileName = outputPath + "/" + entity.getName() + "DTO.java";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Write package and imports
            writer.println("package com.batty.forgex.dto;");
            writer.println();
            writer.println("import lombok.Builder;");
            writer.println("import lombok.Data;");
            writer.println();
            
            // Write DTO class
            writer.println("@Data");
            writer.println("@Builder");
            writer.println("public class " + entity.getName() + "DTO {");
            writer.println();
            
            // Write fields
            for (Field field : entity.getFields()) {
                // Write field annotations
                if (field.getAnnotations() != null) {
                    for (String annotation : field.getAnnotations()) {
                        writer.println("    " + annotation);
                    }
                }
                writer.println("    private " + field.getType() + " " + field.getName() + ";");
                writer.println();
            }
            
            writer.println("}");
        }
    }
} 