package com.finx.templatemanagementservice.repository;

import com.finx.templatemanagementservice.domain.entity.TemplateContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for TemplateContent entity
 */
@Repository
public interface TemplateContentRepository extends JpaRepository<TemplateContent, Long> {

    /**
     * Find content by template and language
     */
    Optional<TemplateContent> findByTemplateIdAndLanguageCode(Long templateId, String languageCode);

    /**
     * Delete all content for a template
     */
    void deleteByTemplateId(Long templateId);
}
