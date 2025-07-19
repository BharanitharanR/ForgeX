package com.batty.forgex.generator.template;

import java.util.Map;

public interface TemplateManager {
    String getTemplate(String templateName);
    void registerTemplate(String name, String content);
    Map<String, String> getAllTemplates();
    String processTemplate(String templateName, Map<String, Object> context);
} 