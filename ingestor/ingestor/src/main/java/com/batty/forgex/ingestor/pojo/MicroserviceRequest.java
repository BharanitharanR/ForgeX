package com.batty.forgex.ingestor.pojo;

import java.util.List;

public class MicroserviceRequest {

    private String name;                // Name of the microservice
    private String entityName;          // Name of the primary entity
    private List<Field> fields;         // List of fields for the entity
    private List<String> dependencies;  // Additional dependencies (e.g., JPA, Lombok)
    private String database;            // Database type (PostgreSQL, MySQL, etc.)

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    // Nested Field class
    public static class Field {
        private String name;
        private String type;
        private boolean required;
        private String defaultValue;

        public Field(String name, String type, boolean required, String defaultValue) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.defaultValue = defaultValue;
        }

        // Getters and Setters
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

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public String toString() {
            return "Field{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", required=" + required +
                    ", defaultValue='" + defaultValue + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MicroserviceRequest{" +
                "name='" + name + '\'' +
                ", entityName='" + entityName + '\'' +
                ", fields=" + fields +
                ", dependencies=" + dependencies +
                ", database='" + database + '\'' +
                '}';
    }
}
