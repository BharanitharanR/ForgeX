package com.batty.forgex.generator.context;

import java.util.List;
import java.util.Map;

public class GenerationContext {
    private final String serviceName;
    private final List<EntityDefinition> entities;
    private final Map<String, Object> properties;

    private GenerationContext(Builder builder) {
        this.serviceName = builder.serviceName;
        this.entities = builder.entities;
        this.properties = builder.properties;
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<EntityDefinition> getEntities() {
        return entities;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public static class Builder {
        private String serviceName;
        private List<EntityDefinition> entities;
        private Map<String, Object> properties;

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder entities(List<EntityDefinition> entities) {
            this.entities = entities;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public GenerationContext build() {
            return new GenerationContext(this);
        }
    }

    public static class EntityDefinition {
        private String name;
        private List<FieldDefinition> fields;
        private List<RelationshipDefinition> relationships;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<FieldDefinition> getFields() {
            return fields;
        }

        public void setFields(List<FieldDefinition> fields) {
            this.fields = fields;
        }

        public List<RelationshipDefinition> getRelationships() {
            return relationships;
        }

        public void setRelationships(List<RelationshipDefinition> relationships) {
            this.relationships = relationships;
        }
    }

    public static class FieldDefinition {
        private String name;
        private String type;
        private boolean required;
        private Object defaultValue;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    public static class RelationshipDefinition {
        private String targetEntity;
        private String type;
        private String fieldName;

        public String getTargetEntity() {
            return targetEntity;
        }

        public void setTargetEntity(String targetEntity) {
            this.targetEntity = targetEntity;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
    }
} 