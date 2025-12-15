package com.finx.dmsservice.domain.entity;

import com.finx.dmsservice.domain.enums.AccessType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "document_access_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 30)
    private AccessType accessType;

    @Column(name = "access_ip", length = 45)
    private String accessIp;

    @Column(name = "access_user_agent", length = 255)
    private String accessUserAgent;

    @Column(name = "access_reason", length = 255)
    private String accessReason;

    @Type(JsonType.class)
    @Column(name = "shared_with", columnDefinition = "jsonb")
    private List<Long> sharedWith;

    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;

    @PrePersist
    protected void onCreate() {
        if (accessedAt == null) {
            accessedAt = LocalDateTime.now();
        }
    }
}
