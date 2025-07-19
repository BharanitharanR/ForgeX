package com.batty.forgex.template.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.jetbrains.annotations.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "templates")
public class Template {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private String content;

    private Map<String, String> metadata;

    @NotNull
    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
} 