package com.batty.forgex.ingestor.serviceGenerator;

import com.batty.forgex.ingestor.model.Field;
import com.batty.forgex.ingestor.model.GraphInput;
import com.batty.forgex.ingestor.model.Node;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class OpenApiSpecGenerator {

    protected Logger log = LoggerFactory.getLogger(OpenApiSpecGenerator.class);
     private final ObjectMapper openapiYamlMapper = Yaml.mapper();

     // private final ObjectMapper openapiYamlMapper = new ObjectMapper(new YAMLFactory());


    public void generateOpenApiSpecs(GraphInput graphInput,String outputDir) {

        try {
            log.info("OPENAPI Generator: data");
            for (Node node : graphInput.getNodes())
            {

                if ("ENTITY".equals(node.getType().getValue())) {
                    log.info("name : {}",node.getName());
                    OpenAPI openApi = createBaseApi(node.getName());
                    addEntityComponents(openApi, node);
                    addEntityPaths(openApi, node);
                    writeSpecToFile(openApi, outputDir, node.getName());
                }
            }
        }
        catch (Exception e)
        {
            log.error("Exception", e);
        }
    }

    private OpenAPI createBaseApi(String serviceName) {
        return new OpenAPI()
                .info(new Info()
                        .title(serviceName + " Service API")
                        .version("1.0.0")
                        .description("Microservice for managing " + serviceName + " entities"))
                .addTagsItem(new Tag().name(serviceName).description(serviceName + " operations"));
    }

    private void addEntityComponents(OpenAPI openApi, Node node) {
        try {
            Schema<?> schema = new Schema<>().type("object");
            log.info("name : {}", node.getName());
            Components components = new Components();
            for (Field field : node.getFields()) {
                Schema<?> fieldSchema = createFieldSchema(field);
                log.info("field schema : {}", fieldSchema.toString());
                schema.addProperty(field.getName(), fieldSchema);

                if (field.getRequired()) {
                    schema.addRequiredItem(field.getName());
                }
            }
            components.addSchemas(node.getName(), schema);
            openApi.setComponents(components);
        }
        catch(Exception e)
        {
            log.error("Exception addEntity : {}",e.getMessage());
        }
    }

    private Schema<?> createFieldSchema(Field field) {
        log.info("fieldSchema : {}",field.getType().toLowerCase());
        switch (field.getType().toLowerCase()) {
            case "string":
                return new StringSchema();
            case "number":
                return new NumberSchema().format("double");
            case "date":
                return new DateSchema();
            case "array":
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.items(createFieldSchema(field));
                return arraySchema;
            case "boolean":
                return new BooleanSchema();
            default:
                return new Schema<>().$ref("#/components/schemas/" + field.getType());
        }
    }

    private void addEntityPaths(OpenAPI openApi, Node node) {
        Paths paths = new Paths();
        String path = "/" + node.getName().toLowerCase();

        // POST endpoint
        paths.addPathItem(path, new PathItem().post(
                new Operation()
                        .summary("Create new " + node.getName())
                        .tags(Collections.singletonList(node.getName()))
                        .requestBody(new RequestBody()
                                .content(new Content()
                                        .addMediaType("application/json",
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + node.getName())))))
                        .responses(new ApiResponses()
                                .addApiResponse("201", new ApiResponse().description(node.getName() + " created successfully")))));

        // GET all endpoint
        paths.addPathItem(path, new PathItem().get(
                new Operation()
                        .summary("List all " + node.getName() + "s")
                        .tags(Collections.singletonList(node.getName()))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("List of " + node.getName() + "s")
                                        .content(new Content()
                                                .addMediaType("application/json",
                                                        new MediaType().schema(new ArraySchema()
                                                                .items(new Schema<>().$ref("#/components/schemas/" + node.getName())))))))));

        // GET by ID endpoint
        paths.addPathItem(path + "/{id}", new PathItem().get(
                new Operation()
                        .summary("Get " + node.getName() + " by ID")
                        .tags(Collections.singletonList(node.getName()))
                        .parameters(Collections.singletonList(new Parameter()
                                .name("id")
                                .in("path")
                                .required(true)
                                .description("ID of the " + node.getName() + " to retrieve")
                                .schema(new StringSchema())))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description(node.getName() + " details")
                                        .content(new Content()
                                                .addMediaType("application/json",
                                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + node.getName())))))
                                .addApiResponse("404", new ApiResponse().description(node.getName() + " not found")))
        ));


        openApi.paths(paths);
    }


    private void writeSpecToFile(OpenAPI openApi, String outputDir, String serviceName) throws Exception {
        // Create directories
        Files.createDirectories(java.nio.file.Paths.get(outputDir));
        File outputFile = new File(outputDir, serviceName.toLowerCase() + "-service.yaml");
        openapiYamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        log.info("Data openapi: {}",openapiYamlMapper.writeValueAsString(openApi));
        openapiYamlMapper.writeValue(outputFile, openApi);
    }
}