package com.batty.forgex.template.service;

import com.batty.forgex.template.datastore.TemplateServiceDatastoreImpl;
import com.batty.forgex.template.model.Template;
import com.batty.forgex.template.repository.TemplateRepository;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TemplateService {
    @Autowired
    private final TemplateServiceDatastoreImpl templateRepository;

    public TemplateService(TemplateServiceDatastoreImpl templateRepository) {
        this.templateRepository = templateRepository;
    }


    @Transactional
    public Optional<InsertOneResult> createTemplate(Template template) {

        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.insertDataResponse(template);
    }

    public List<Template> getAllTemplates() {
        return null;
    }

    @Transactional
    public Template updateTemplate(String id, Template updatedTemplate) {
        return templateRepository.findOne(Document.parse(id)).orElse(null);
    }

    @Transactional
    public void deleteTemplate(String id) {

    }

    public String processTemplate(String templateName, Map<String, Object> variables) {
        return null;
    }
} 