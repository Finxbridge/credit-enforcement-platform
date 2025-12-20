package com.finx.configurationsservice.repository;

import com.finx.configurationsservice.domain.entity.Holiday;
import com.finx.configurationsservice.domain.enums.HolidayType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    Optional<Holiday> findByHolidayCode(String holidayCode);

    List<Holiday> findByIsActiveTrue();

    List<Holiday> findByHolidayType(HolidayType holidayType);

    @Query("SELECT h FROM Holiday h WHERE h.startDate <= :date AND h.endDate >= :date AND h.isActive = true")
    List<Holiday> findHolidaysForDate(@Param("date") LocalDate date);

    @Query("SELECT h FROM Holiday h WHERE h.startDate >= :startDate AND h.endDate <= :endDate AND h.isActive = true")
    List<Holiday> findHolidaysBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT h FROM Holiday h WHERE YEAR(h.startDate) = :year AND h.isActive = true")
    List<Holiday> findHolidaysByYear(@Param("year") int year);

    Page<Holiday> findByIsActive(Boolean isActive, Pageable pageable);

    boolean existsByHolidayCode(String holidayCode);
}
