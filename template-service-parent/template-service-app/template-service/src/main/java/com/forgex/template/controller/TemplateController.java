package com.forgex.template.controller;

import com.forgex.template.model.Template;
import com.forgex.template.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        Template createdTemplate = templateService.createTemplate(template);
        return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Template> getTemplateById(@PathVariable Long id) {
        Template template = templateService.getTemplateById(id);
        return ResponseEntity.ok(template);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Template> getTemplateByName(@PathVariable String name) {
        Template template = templateService.getTemplateByName(name);
        return ResponseEntity.ok(template);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Template>> searchTemplates(@RequestParam String name) {
        List<Template> templates = templateService.searchTemplates(name);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Template>> getTemplatesByType(@PathVariable String type) {
        List<Template> templates = templateService.getTemplatesByType(type);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Template>> getTemplatesByCategory(@PathVariable String category) {
        List<Template> templates = templateService.getTemplatesByCategory(category);
        return ResponseEntity.ok(templates);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Template> updateTemplate(@PathVariable Long id, @RequestBody Template template) {
        Template updatedTemplate = templateService.updateTemplate(id, template);
        return ResponseEntity.ok(updatedTemplate);
    }

    @PatchMapping("/{id}/metadata")
    public ResponseEntity<Void> updateTemplateMetadata(@PathVariable Long id, @RequestBody Map<String, String> metadata) {
        templateService.updateTemplateMetadata(id, metadata);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
} 