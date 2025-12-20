package com.finx.configurationsservice.domain.entity;

import com.finx.configurationsservice.domain.enums.TestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "provider_test_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderTestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(name = "test_type", nullable = false, length = 30)
    private String testType; // CONNECTIVITY, SEND_TEST, WEBHOOK

    @Enumerated(EnumType.STRING)
    @Column(name = "test_status", nullable = false, length = 20)
    private TestStatus testStatus;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "response_message", columnDefinition = "TEXT")
    private String responseMessage;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @Column(name = "tested_at")
    private LocalDateTime testedAt;

    @Column(name = "tested_by")
    private Long testedBy;

    @PrePersist
    protected void onCreate() {
        if (testedAt == null) {
            testedAt = LocalDateTime.now();
        }
    }
}
