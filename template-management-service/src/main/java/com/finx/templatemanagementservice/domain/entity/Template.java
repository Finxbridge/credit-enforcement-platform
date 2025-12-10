package com.finx.templatemanagementservice.domain.entity;

import com.finx.templatemanagementservice.domain.enums.ChannelType;
import com.finx.templatemanagementservice.domain.enums.ProviderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Template entity for communication templates
 */
@Entity
@Table(name = "templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"variables", "contents"})
@ToString(exclude = {"variables", "contents"})
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Column(name = "template_code", nullable = false, unique = true, length = 50)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private ChannelType channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 50)
    private ProviderType provider;

    @Column(name = "provider_template_id", length = 100)
    private String providerTemplateId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Document attachment fields - stored in DMS service
    @Column(name = "dms_document_id", length = 50)
    private String dmsDocumentId; // Reference to DMS service document

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "document_original_name", length = 255)
    private String documentOriginalName;

    @Column(name = "document_type", length = 50)
    private String documentType; // PDF, DOC, DOCX

    @Column(name = "document_size_bytes")
    private Long documentSizeBytes;

    @Builder.Default
    @Column(name = "has_document_variables")
    private Boolean hasDocumentVariables = false;

    @Column(name = "document_placeholders", columnDefinition = "TEXT")
    private String documentPlaceholders; // JSON array of placeholders extracted from document

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TemplateVariable> variables = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TemplateContent> contents = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to add variable
     */
    public void addVariable(TemplateVariable variable) {
        variables.add(variable);
        variable.setTemplate(this);
    }

    /**
     * Helper method to add content
     */
    public void addContent(TemplateContent content) {
        contents.add(content);
        content.setTemplate(this);
    }
}
