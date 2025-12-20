package com.finx.configurationsservice.domain.entity;

import com.finx.configurationsservice.domain.enums.RescheduleStrategy;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Entity
@Table(name = "work_calendars")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calendar_code", unique = true, nullable = false, length = 50)
    private String calendarCode;

    @Column(name = "calendar_name", nullable = false, length = 200)
    private String calendarName;

    @Type(JsonType.class)
    @Column(name = "working_days", columnDefinition = "jsonb", nullable = false)
    private Map<String, Boolean> workingDays;

    @Column(name = "work_start_time")
    private LocalTime workStartTime;

    @Column(name = "work_end_time")
    private LocalTime workEndTime;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "non_working_day_behavior", length = 30)
    private RescheduleStrategy nonWorkingDayBehavior;

    @Column(name = "is_default")
    private Boolean isDefault;

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
        if (isDefault == null) {
            isDefault = false;
        }
        if (workStartTime == null) {
            workStartTime = LocalTime.of(9, 0);
        }
        if (workEndTime == null) {
            workEndTime = LocalTime.of(18, 0);
        }
        if (nonWorkingDayBehavior == null) {
            nonWorkingDayBehavior = RescheduleStrategy.NEXT_WORKING_DAY;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
