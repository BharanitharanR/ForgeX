package com.forgex.template.repository;

import com.forgex.template.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    Optional<Template> findByName(String name);
    
    List<Template> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT t FROM Template t WHERE t.metadata['type'] = :type")
    List<Template> findByType(@Param("type") String type);
    
    @Query("SELECT t FROM Template t WHERE t.metadata['category'] = :category")
    List<Template> findByCategory(@Param("category") String category);
    
    boolean existsByName(String name);
} 