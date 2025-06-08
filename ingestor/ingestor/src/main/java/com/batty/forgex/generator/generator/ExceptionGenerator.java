package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.model.Entity;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ExceptionGenerator {
    
    public void generateExceptions(Entity entity, String outputPath) throws IOException {
        generateNotFoundException(entity, outputPath);
        generateAlreadyExistsException(entity, outputPath);
    }
    
    private void generateNotFoundException(Entity entity, String outputPath) throws IOException {
        String fileName = outputPath + "/" + entity.getName() + "NotFoundException.java";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Write package and imports
            writer.println("package com.batty.forgex.exception;");
            writer.println();
            writer.println("import org.springframework.http.HttpStatus;");
            writer.println("import org.springframework.web.bind.annotation.ResponseStatus;");
            writer.println();
            
            // Write exception class
            writer.println("@ResponseStatus(HttpStatus.NOT_FOUND)");
            writer.println("public class " + entity.getName() + "NotFoundException extends RuntimeException {");
            writer.println();
            writer.println("    public " + entity.getName() + "NotFoundException(" + 
                          getPrimaryKeyType(entity) + " id) {");
            writer.println("        super(\"" + entity.getName() + " not found with id: \" + id);");
            writer.println("    }");
            writer.println("}");
        }
    }
    
    private void generateAlreadyExistsException(Entity entity, String outputPath) throws IOException {
        String fileName = outputPath + "/" + entity.getName() + "AlreadyExistsException.java";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Write package and imports
            writer.println("package com.batty.forgex.exception;");
            writer.println();
            writer.println("import org.springframework.http.HttpStatus;");
            writer.println("import org.springframework.web.bind.annotation.ResponseStatus;");
            writer.println();
            
            // Write exception class
            writer.println("@ResponseStatus(HttpStatus.CONFLICT)");
            writer.println("public class " + entity.getName() + "AlreadyExistsException extends RuntimeException {");
            writer.println();
            writer.println("    public " + entity.getName() + "AlreadyExistsException(" + 
                          getPrimaryKeyType(entity) + " id) {");
            writer.println("        super(\"" + entity.getName() + " already exists with id: \" + id);");
            writer.println("    }");
            writer.println("}");
        }
    }
    
    private String getPrimaryKeyType(Entity entity) {
        return entity.getFields().stream()
                .filter(field -> field.isPrimaryKey())
                .findFirst()
                .map(field -> field.getType())
                .orElse("Long");
    }
} 