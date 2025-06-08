package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.model.Entity;
import com.batty.forgex.generator.model.Field;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ValidationGenerator {
    
    public void generateValidation(Entity entity, String outputPath) throws IOException {
        String fileName = outputPath + "/" + entity.getName() + "Validation.java";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Write package and imports
            writer.println("package com.batty.forgex.validation;");
            writer.println();
            writer.println("import org.springframework.util.StringUtils;");
            writer.println("import com.batty.forgex.exception." + entity.getName() + "ValidationException;");
            writer.println();
            
            // Write validation class
            writer.println("public class " + entity.getName() + "Validation {");
            writer.println();
            
            // Write main validation method
            writer.println("    public static void validate(" + entity.getName() + " " + 
                          entity.getName().toLowerCase() + ") {");
            writer.println("        if (" + entity.getName().toLowerCase() + " == null) {");
            writer.println("            throw new " + entity.getName() + 
                          "ValidationException(\"" + entity.getName() + " cannot be null\");");
            writer.println("        }");
            writer.println();
            
            // Write field validations
            for (Field field : entity.getFields()) {
                if (!field.isPrimaryKey()) {
                    writer.println("        validate" + capitalize(field.getName()) + "(" + 
                                 entity.getName().toLowerCase() + ".get" + 
                                 capitalize(field.getName()) + "());");
                }
            }
            
            writer.println("    }");
            writer.println();
            
            // Write individual field validation methods
            for (Field field : entity.getFields()) {
                if (!field.isPrimaryKey()) {
                    generateFieldValidation(writer, field);
                }
            }
            
            writer.println("}");
        }
    }
    
    private void generateFieldValidation(PrintWriter writer, Field field) {
        writer.println("    private static void validate" + capitalize(field.getName()) + 
                      "(" + field.getType() + " " + field.getName() + ") {");
        
        if (field.getType().equals("String")) {
            writer.println("        if (!StringUtils.hasText(" + field.getName() + ")) {");
            writer.println("            throw new " + field.getName() + 
                          "ValidationException(\"" + capitalize(field.getName()) + 
                          " cannot be empty\");");
            writer.println("        }");
        } else {
            writer.println("        if (" + field.getName() + " == null) {");
            writer.println("            throw new " + field.getName() + 
                          "ValidationException(\"" + capitalize(field.getName()) + 
                          " cannot be null\");");
            writer.println("        }");
        }
        
        // Add custom validations based on annotations
        if (field.getAnnotations() != null) {
            for (String annotation : field.getAnnotations()) {
                if (annotation.contains("@Email")) {
                    writer.println("        if (!isValidEmail(" + field.getName() + ")) {");
                    writer.println("            throw new " + field.getName() + 
                                  "ValidationException(\"Invalid email format\");");
                    writer.println("        }");
                }
            }
        }
        
        writer.println("    }");
        writer.println();
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
} 