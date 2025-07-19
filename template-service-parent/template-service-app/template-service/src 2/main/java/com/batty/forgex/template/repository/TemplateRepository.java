package com.batty.forgex.template.repository;

import com.batty.forgex.template.model.Template;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TemplateRepository extends MongoRepository<Template, String> {
    Optional<Template> findByName(String name);
    boolean existsByName(String name);
} 