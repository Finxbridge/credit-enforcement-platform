package com.finx.configurationsservice.domain.entity;

import com.finx.configurationsservice.domain.enums.HolidayType;
import com.finx.configurationsservice.domain.enums.RescheduleStrategy;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "holidays")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "holiday_code", unique = true, nullable = false, length = 50)
    private String holidayCode;

    @Column(name = "holiday_name", nullable = false, length = 200)
    private String holidayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_type", nullable = false, length = 30)
    private HolidayType holidayType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Type(JsonType.class)
    @Column(name = "applicable_states", columnDefinition = "jsonb")
    private List<String> applicableStates;

    @Type(JsonType.class)
    @Column(name = "applicable_offices", columnDefinition = "jsonb")
    private List<Long> applicableOffices;

    @Enumerated(EnumType.STRING)
    @Column(name = "reschedule_strategy", length = 30)
    private RescheduleStrategy rescheduleStrategy;

    @Column(name = "fixed_replacement_date")
    private LocalDate fixedReplacementDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (rescheduleStrategy == null) {
            rescheduleStrategy = RescheduleStrategy.NEXT_WORKING_DAY;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
