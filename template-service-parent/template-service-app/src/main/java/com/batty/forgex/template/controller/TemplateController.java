package com.batty.forgex.template.controller;

import com.batty.forgex.template.model.Template;
import com.batty.forgex.template.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    private final TemplateService templateService;

    @Autowired
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    public ResponseEntity<Template> createTemplate(@RequestBody Template template) {
        /*return ResponseEntity.ok(templateService.createTemplate(template));*/
        return null;
    }

    @GetMapping
    public ResponseEntity<List<Template>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Template> getTemplateById(@PathVariable String id) {
    return null;
        /*        return templateService.getAllTemplates()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());*/
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Template> getTemplateByName(@PathVariable String name) {
        return  null;
/*        return templateService.getTemplateByName(name)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());*/
    }

    @PutMapping("/{id}")
    public ResponseEntity<Template> updateTemplate(
            @PathVariable String id,
            @RequestBody Template template) {
        try {
            return ResponseEntity.ok(templateService.updateTemplate(id, template));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{name}/process")
    public ResponseEntity<String> processTemplate(
            @PathVariable String name,
            @RequestBody Map<String, Object> variables) {
        try {
            String processedContent = templateService.processTemplate(name, variables);
            return ResponseEntity.ok(processedContent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 