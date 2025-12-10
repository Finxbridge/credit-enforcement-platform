package com.finx.templatemanagementservice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Template Content entity for storing template content with multilingual support
 */
@Entity
@Table(name = "template_content", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"template_id", "language_code"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "template")
@ToString(exclude = "template")
public class TemplateContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "language_code", length = 10)
    private String languageCode = "en";

    @Column(name = "subject", length = 255)
    private String subject; // For Email templates

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // Template content with placeholders

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
