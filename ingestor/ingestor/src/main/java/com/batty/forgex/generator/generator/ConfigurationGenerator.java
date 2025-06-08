package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.model.Entity;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ConfigurationGenerator {
    
    public void generateConfiguration(Entity entity, String outputPath) throws IOException {
        String fileName = outputPath + "/" + entity.getName() + "Configuration.java";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Write package and imports
            writer.println("package com.batty.forgex.config;");
            writer.println();
            writer.println("import org.springframework.context.annotation.Bean;");
            writer.println("import org.springframework.context.annotation.Configuration;");
            writer.println("import com.batty.forgex.service." + entity.getName() + "Service;");
            writer.println("import com.batty.forgex.service.impl." + entity.getName() + "ServiceImpl;");
            writer.println("import com.batty.forgex.repository." + entity.getName() + "Repository;");
            writer.println("import com.batty.forgex.mapper." + entity.getName() + "Mapper;");
            writer.println();
            
            // Write configuration class
            writer.println("@Configuration");
            writer.println("public class " + entity.getName() + "Configuration {");
            writer.println();
            
            // Write service bean
            writer.println("    @Bean");
            writer.println("    public " + entity.getName() + "Service " + 
                          entity.getName().toLowerCase() + "Service(" + 
                          entity.getName() + "Repository " + 
                          entity.getName().toLowerCase() + "Repository, " +
                          entity.getName() + "Mapper " + 
                          entity.getName().toLowerCase() + "Mapper) {");
            writer.println("        return new " + entity.getName() + "ServiceImpl(" + 
                          entity.getName().toLowerCase() + "Repository, " +
                          entity.getName().toLowerCase() + "Mapper);");
            writer.println("    }");
            writer.println();
            
            // Write repository bean
            writer.println("    @Bean");
            writer.println("    public " + entity.getName() + "Repository " + 
                          entity.getName().toLowerCase() + "Repository() {");
            writer.println("        return new " + entity.getName() + "RepositoryImpl();");
            writer.println("    }");
            writer.println();
            
            // Write mapper bean
            writer.println("    @Bean");
            writer.println("    public " + entity.getName() + "Mapper " + 
                          entity.getName().toLowerCase() + "Mapper() {");
            writer.println("        return new " + entity.getName() + "MapperImpl();");
            writer.println("    }");
            writer.println();
            
            writer.println("}");
        }
    }
} 