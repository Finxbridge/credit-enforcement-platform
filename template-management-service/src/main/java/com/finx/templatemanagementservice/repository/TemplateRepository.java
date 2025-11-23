package com.finx.templatemanagementservice.repository;

import com.finx.templatemanagementservice.domain.entity.Template;
import com.finx.templatemanagementservice.domain.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Template entity
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    /**
     * Find template by template code
     */
    Optional<Template> findByTemplateCode(String templateCode);

    /**
     * Check if template code exists
     */
    boolean existsByTemplateCode(String templateCode);

    /**
     * Find all active templates
     */
    List<Template> findByIsActiveTrue();

    /**
     * Find templates by channel
     */
    List<Template> findByChannelAndIsActiveTrue(ChannelType channel);

    /**
     * Find all templates by channel
     */
    List<Template> findByChannel(ChannelType channel);

    /**
     * Find template with variables
     */
    @Query("SELECT t FROM Template t LEFT JOIN FETCH t.variables WHERE t.id = :id")
    Optional<Template> findByIdWithVariables(@Param("id") Long id);

    /**
     * Find template with variables and content
     */
    @Query("SELECT t FROM Template t " +
           "LEFT JOIN FETCH t.variables " +
           "LEFT JOIN FETCH t.contents " +
           "WHERE t.id = :id")
    Optional<Template> findByIdWithDetails(@Param("id") Long id);

    /**
     * Search templates by name or code
     */
    @Query("SELECT t FROM Template t WHERE " +
           "LOWER(t.templateName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.templateCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Template> searchTemplates(@Param("keyword") String keyword);
}
