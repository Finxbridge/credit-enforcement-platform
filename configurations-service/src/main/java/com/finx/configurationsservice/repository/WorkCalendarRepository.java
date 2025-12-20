package com.finx.configurationsservice.repository;

import com.finx.configurationsservice.domain.entity.WorkCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkCalendarRepository extends JpaRepository<WorkCalendar, Long> {

    Optional<WorkCalendar> findByCalendarCode(String calendarCode);

    List<WorkCalendar> findByIsActiveTrue();

    Optional<WorkCalendar> findByIsDefaultTrue();

    boolean existsByCalendarCode(String calendarCode);
}
