package com.batty.forgex.template.repository;

import com.batty.forgex.template.model.Template;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends MongoRepository<Template, String> {
    Optional<Template> findByNameAndTypeAndLanguage(String name, String type, String language);
    List<Template> findByTypeAndLanguage(String type, String language);
    List<Template> findByTagsContaining(String tag);
    Optional<Template> findByName(String name);
    boolean existsByName(String name);
} 