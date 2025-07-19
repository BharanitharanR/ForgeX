package com.batty.forgex.template.service;

import com.batty.forgex.template.model.Template;
import com.batty.forgex.template.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TemplateService {
    private final TemplateRepository templateRepository;

    @Autowired
    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional
    public Template createTemplate(Template template) {
        if (templateRepository.existsByName(template.getName())) {
            throw new IllegalArgumentException("Template with name " + template.getName() + " already exists");
        }

        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Optional<Template> getTemplateById(String id) {
        return templateRepository.findById(id);
    }

    public Optional<Template> getTemplateByName(String name) {
        return templateRepository.findByName(name);
    }

    @Transactional
    public Template updateTemplate(String id, Template updatedTemplate) {
        return templateRepository.findById(id)
            .map(existingTemplate -> {
                existingTemplate.setName(updatedTemplate.getName());
                existingTemplate.setDescription(updatedTemplate.getDescription());
                existingTemplate.setContent(updatedTemplate.getContent());
                existingTemplate.setMetadata(updatedTemplate.getMetadata());
                existingTemplate.setUpdatedAt(LocalDateTime.now());
                return templateRepository.save(existingTemplate);
            })
            .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
    }

    @Transactional
    public void deleteTemplate(String id) {
        if (!templateRepository.existsById(id)) {
            throw new IllegalArgumentException("Template not found with id: " + id);
        }
        templateRepository.deleteById(id);
    }

    public String processTemplate(String templateName, Map<String, Object> variables) {
        return getTemplateByName(templateName)
            .map(template -> {
                String content = template.getContent();
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    content = content.replace("${" + entry.getKey() + "}", 
                        String.valueOf(entry.getValue()));
                }
                return content;
            })
            .orElseThrow(() -> new IllegalArgumentException("Template not found with name: " + templateName));
    }
} 