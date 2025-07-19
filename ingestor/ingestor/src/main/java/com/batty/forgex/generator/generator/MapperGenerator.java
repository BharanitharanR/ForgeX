package com.batty.forgex.generator.generator;

import com.batty.forgex.generator.model.Entity;
import com.batty.forgex.generator.model.Field;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class MapperGenerator {
    
    public void generateMapper(Entity entity, String outputPath) throws IOException {
        String fileName = outputPath + "/" + entity.getName() + "Mapper.java";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Write package and imports
            writer.println("package com.batty.forgex.mapper;");
            writer.println();
            writer.println("import org.mapstruct.Mapper;");
            writer.println("import org.mapstruct.Mapping;");
            writer.println("import com.batty.forgex.model." + entity.getName() + ";");
            writer.println("import com.batty.forgex.dto." + entity.getName() + "DTO;");
            writer.println("import java.util.List;");
            writer.println();
            
            // Write mapper interface
            writer.println("@Mapper(componentModel = \"spring\")");
            writer.println("public interface " + entity.getName() + "Mapper {");
            writer.println();
            
            // Write mapping methods
            writer.println("    " + entity.getName() + "DTO toDTO(" + entity.getName() + " " + 
                          entity.getName().toLowerCase() + ");");
            writer.println();
            
            writer.println("    " + entity.getName() + " toEntity(" + entity.getName() + "DTO dto);");
            writer.println();
            
            writer.println("    List<" + entity.getName() + "DTO> toDTOList(List<" + entity.getName() + 
                          "> " + entity.getName().toLowerCase() + "s);");
            writer.println();
            
            writer.println("    List<" + entity.getName() + "> toEntityList(List<" + entity.getName() + 
                          "DTO> dtos);");
            writer.println();
            
            // Add relationship mapping methods
            for (Field field : entity.getFields()) {
                if (!field.isPrimaryKey() && !isBasicType(field.getType())) {
                    String relatedType = field.getType();
                    writer.println("    " + relatedType + "DTO " + field.getName() + "To" + 
                                 relatedType + "DTO(" + relatedType + " " + field.getName() + ");");
                    writer.println();
                    
                    writer.println("    " + relatedType + " " + field.getName() + "DTOTo" + 
                                 relatedType + "(" + relatedType + "DTO " + field.getName() + "DTO);");
                    writer.println();
                }
            }
            
            writer.println("}");
        }
    }
    
    private boolean isBasicType(String type) {
        return type.equals("String") || type.equals("Integer") || type.equals("Long") || 
               type.equals("Double") || type.equals("Float") || type.equals("Boolean") || 
               type.equals("Date") || type.equals("LocalDate") || type.equals("LocalDateTime");
    }
} 