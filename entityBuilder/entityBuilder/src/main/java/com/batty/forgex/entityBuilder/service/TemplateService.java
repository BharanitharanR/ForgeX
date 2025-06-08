package com.batty.forgex.entityBuilder.service;

import com.batty.forgex.template.client.api.DefaultApi;
import com.batty.forgex.template.client.model.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {
    private final DefaultApi templateApi;

    public Optional<Template> getTemplate(String name, String type, String language) {
        try {
            return Optional.ofNullable(templateApi.getTemplate(name, type, language));
        } catch (Exception e) {
            log.error("Error fetching template: name={}, type={}, language={}", name, type, language, e);
            return Optional.empty();
        }
    }

    public List<Template> getTemplatesByTypeAndLanguage(String type, String language) {
        try {
            return templateApi.getTemplatesByTypeAndLanguage(type, language);
        } catch (Exception e) {
            log.error("Error fetching templates: type={}, language={}", type, language, e);
            return List.of();
        }
    }

    public List<Template> getTemplatesByTag(String tag) {
        try {
            return templateApi.getTemplatesByTag(tag);
        } catch (Exception e) {
            log.error("Error fetching templates by tag: tag={}", tag, e);
            return List.of();
        }
    }

    public Optional<Template> getTemplateById(String id) {
        try {
            return Optional.ofNullable(templateApi.getTemplateById(id));
        } catch (Exception e) {
            log.error("Error fetching template by id: id={}", id, e);
            return Optional.empty();
        }
    }

    public List<Template> getAllTemplates() {
        try {
            return templateApi.getAllTemplates();
        } catch (Exception e) {
            log.error("Error fetching all templates", e);
            return List.of();
        }
    }
} 