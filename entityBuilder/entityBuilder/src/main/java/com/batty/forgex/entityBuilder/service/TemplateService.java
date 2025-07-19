package com.batty.forgex.entityBuilder.service;
import com.batty.forgex.entityBuilder.model.InlineResponse200;
import com.batty.forgex.templateService.api.*;
import com.batty.forgex.templateService.api.client.ApiClient;
import com.batty.forgex.templateService.api.model.Template;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TemplateService {


    ObjectMapper mapper = new ObjectMapper();
    @Value("${template.service.hostname}")
    public String templateServiceHostname;
    protected Logger log = LoggerFactory.getLogger(TemplateService.class);
    public Optional<Template> getTemplate(String name, String type, String language) {
        try {
            return Optional.ofNullable(getTemplateServiceClient().getTemplate(name, type, language));
        } catch (Exception e) {
            log.error("Error fetching template: name={}, type={}, language={}", name, type, language, e);
            return Optional.empty();
        }
    }

    public List<Template> getTemplatesByTypeAndLanguage(String type, String language) {
        try {


            return getTemplateServiceClient().getTemplatesByTypeAndLanguage(type, language);
        } catch (Exception e) {
            log.error("Error fetching templates: type={}, language={}", type, language, e);
            return List.of();
        }
    }

    public List<Template> getTemplatesByTag(String tag) {
        try {
            return getTemplateServiceClient().getTemplatesByTag(tag);
        } catch (Exception e) {
            log.error("Error fetching templates by tag: tag={}", tag, e);
            return List.of();
        }
    }

    public Optional<Template> getTemplateById(String id) {
        try {
            return Optional.ofNullable(getTemplateServiceClient().getTemplateById(id));
        } catch (Exception e) {
            log.error("Error fetching template by id: id={}", id, e);
            return Optional.empty();
        }
    }

    public List<Template> getAllTemplates() {
        try {
            return getTemplateServiceClient().getAllTemplates();
        } catch (Exception e) {
            log.error("Error fetching all templates", e);
            return List.of();
        }
    }

    public DefaultApi getTemplateServiceClient()
    {
        // Set up the API client
        com.batty.forgex.templateService.api.client.Configuration.setDefaultApiClient(
                new ApiClient().setBasePath(templateServiceHostname)
        );
        return new DefaultApi();
    }
} 