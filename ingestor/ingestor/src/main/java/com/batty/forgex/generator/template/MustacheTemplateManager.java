package com.batty.forgex.generator.template;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class MustacheTemplateManager implements TemplateManager {
    private static final Logger log = LoggerFactory.getLogger(MustacheTemplateManager.class);
    private final MustacheFactory mustacheFactory;
    private final Map<String, String> templates;

    public MustacheTemplateManager() {
        this.mustacheFactory = new DefaultMustacheFactory();
        this.templates = new HashMap<>();
        initializeDefaultTemplates();
    }

    private void initializeDefaultTemplates() {
        templates.put("entity", """
            package {{packageName}};
            
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            import lombok.Data;
            
            @Data
            @Document(collection = "{{collectionName}}")
            public class {{className}} {
                @Id
                private String id;
                {{#fields}}
                private {{type}} {{name}};
                {{/fields}}
            }
            """);
            
        templates.put("controller", """
            package {{packageName}};
            
            import org.springframework.web.bind.annotation.*;
            import org.springframework.beans.factory.annotation.Autowired;
            import java.util.List;
            
            @RestController
            @RequestMapping("/api/{{basePath}}")
            public class {{className}}Controller {
                @Autowired
                private {{className}}Service service;
                
                @GetMapping
                public List<{{className}}> getAll() {
                    return service.findAll();
                }
                
                @GetMapping("/{id}")
                public {{className}} getById(@PathVariable String id) {
                    return service.findById(id);
                }
                
                @PostMapping
                public {{className}} create(@RequestBody {{className}} entity) {
                    return service.save(entity);
                }
                
                @PutMapping("/{id}")
                public {{className}} update(@PathVariable String id, @RequestBody {{className}} entity) {
                    return service.update(id, entity);
                }
                
                @DeleteMapping("/{id}")
                public void delete(@PathVariable String id) {
                    service.delete(id);
                }
            }
            """);
    }

    @Override
    public String processTemplate(String templateName, Map<String, Object> context) {
        try {
            String template = getTemplate(templateName);
            Mustache mustache = mustacheFactory.compile(new StringReader(template), templateName);
            StringWriter writer = new StringWriter();
            mustache.execute(writer, context);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error processing template: {}", templateName, e);
            throw new RuntimeException("Failed to process template: " + templateName, e);
        }
    }

    @Override
    public String getTemplate(String templateName) {
        String template = templates.get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        return template;
    }

    @Override
    public void registerTemplate(String name, String content) {
        templates.put(name, content);
    }

    @Override
    public Map<String, String> getAllTemplates() {
        return new HashMap<>(templates);
    }

    public Mustache compileTemplate(String templateName) {
        String template = getTemplate(templateName);
        return mustacheFactory.compile(new StringReader(template), templateName);
    }
} 