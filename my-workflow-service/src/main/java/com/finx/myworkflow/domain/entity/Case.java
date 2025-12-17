package com.finx.myworkflow.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Read-only Case entity for workflow case listing
 * Only includes fields needed for workflow display
 */
@Entity
@Table(name = "cases")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", unique = true, nullable = false, length = 50)
    private String caseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private LoanDetails loan;

    @Column(name = "case_status", length = 20)
    private String caseStatus;

    @Column(name = "allocated_to_user_id")
    private Long allocatedToUserId;

    @Column(name = "primary_agent", length = 100)
    private String primaryAgent;

    @Column(name = "allocated_at")
    private LocalDateTime allocatedAt;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "city_code", length = 50)
    private String cityCode;

    @Column(name = "state_code", length = 50)
    private String stateCode;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
