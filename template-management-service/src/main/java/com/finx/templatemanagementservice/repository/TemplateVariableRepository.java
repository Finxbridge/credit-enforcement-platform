package com.finx.templatemanagementservice.repository;

import com.finx.templatemanagementservice.domain.entity.TemplateVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TemplateVariable entity
 */
@Repository
public interface TemplateVariableRepository extends JpaRepository<TemplateVariable, Long> {

    /**
     * Find all variables for a template
     */
    List<TemplateVariable> findByTemplateIdOrderByDisplayOrder(Long templateId);

    /**
     * Delete all variables for a template
     */
    @Modifying
    @Query("DELETE FROM TemplateVariable tv WHERE tv.template.id = :templateId")
    void deleteByTemplateId(@Param("templateId") Long templateId);
}
