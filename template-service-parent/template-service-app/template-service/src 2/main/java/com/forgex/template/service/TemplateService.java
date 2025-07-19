package com.forgex.template.service;

import com.forgex.template.model.Template;
import com.forgex.template.repository.TemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TemplateService {
    private final TemplateRepository templateRepository;

    @Autowired
    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public Template createTemplate(Template template) {
        if (templateRepository.existsByName(template.getName())) {
            throw new IllegalArgumentException("Template with name '" + template.getName() + "' already exists");
        }
        return templateRepository.save(template);
    }

    public Template getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + id));
    }

    public Template getTemplateByName(String name) {
        return templateRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Template not found with name: " + name));
    }

    public List<Template> searchTemplates(String name) {
        return templateRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Template> getTemplatesByType(String type) {
        return templateRepository.findByType(type);
    }

    public List<Template> getTemplatesByCategory(String category) {
        return templateRepository.findByCategory(category);
    }

    public Template updateTemplate(Long id, Template updatedTemplate) {
        Template existingTemplate = getTemplateById(id);
        
        // Update fields
        existingTemplate.setName(updatedTemplate.getName());
        existingTemplate.setContent(updatedTemplate.getContent());
        existingTemplate.setInputSchema(updatedTemplate.getInputSchema());
        existingTemplate.setOutputSchema(updatedTemplate.getOutputSchema());
        existingTemplate.setMetadata(updatedTemplate.getMetadata());
        
        return templateRepository.save(existingTemplate);
    }

    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new EntityNotFoundException("Template not found with id: " + id);
        }
        templateRepository.deleteById(id);
    }

    public void updateTemplateMetadata(Long id, Map<String, String> metadata) {
        Template template = getTemplateById(id);
        template.setMetadata(metadata);
        templateRepository.save(template);
    }
} 