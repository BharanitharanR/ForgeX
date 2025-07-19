package com.batty.forgex.ingestor.serviceGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenApiSpecBuilder {
    private StringBuilder spec;

    public OpenApiSpecBuilder() {
        spec = new StringBuilder();
    }

    public OpenApiSpecBuilder startSpec() {
        spec.append("openapi: 3.0.0\ninfo:\n  title: Generated API\n  version: 1.0.0\npaths:\n");
        return this;
    }

    public OpenApiSpecBuilder addEntity(JsonNode entity) {
        String entityName = entity.get("name").asText();
        spec.append(String.format("  /%s:\n", entityName.toLowerCase()));
        spec.append("    get:\n      summary: Get all " + entityName + "\n");
        spec.append("      responses:\n        '200':\n          description: Successful response\n");
        spec.append("    post:\n      summary: Create a new " + entityName + "\n");
        spec.append("      requestBody:\n        required: true\n        content:\n          application/json:\n            schema:\n              $ref: '#/components/schemas/" + entityName + "'\n");
        spec.append("      responses:\n        '201':\n          description: Created\n");
        return this;
    }

    public OpenApiSpecBuilder addSchemas(JsonNode entity) {
        String entityName = entity.get("name").asText();
        spec.append(String.format("  %s:\n    type: object\n    properties:\n", entityName));
        for (JsonNode field : entity.get("fields")) {
            String fieldName = field.get("name").asText();
            String fieldType = convertToOpenApiType(field.get("type").asText());
            spec.append(String.format("      %s:\n        type: %s\n", fieldName, fieldType));
        }
        return this;
    }

    public OpenApiSpecBuilder endSpec() {
        spec.append("components:\n  schemas:\n");
        return this;
    }

    public String build() {
        return spec.toString();
    }

    private String convertToOpenApiType(String jsonType) {
        return switch (jsonType) {
            case "string" -> "string";
            case "number" -> "number";
            case "boolean" -> "boolean";
            case "array" -> "array";
            case "date" -> "string";
            default -> "object";
        };
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonPayload = "{\"nodes\":[{\"type\":\"ENTITY\",\"name\":\"Sale\",\"fields\":[{\"name\":\"saleId\",\"type\":\"string\",\"required\":true}]}]}";
        JsonNode rootNode = mapper.readTree(jsonPayload);
        OpenApiSpecBuilder builder = new OpenApiSpecBuilder();
        builder.startSpec();
        for (JsonNode entity : rootNode.get("nodes")) {
            builder.addEntity(entity).addSchemas(entity);
        }
        builder.endSpec();
        System.out.println(builder.build());
    }
}
