package com.finx.strategyengineservice.repository;

import com.finx.strategyengineservice.domain.entity.FilterField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilterFieldRepository extends JpaRepository<FilterField, Long> {

    List<FilterField> findByIsActiveTrueOrderBySortOrder();

    List<FilterField> findByFieldTypeAndIsActiveTrueOrderBySortOrder(String fieldType);

    Optional<FilterField> findByFieldCode(String fieldCode);

    @Query("SELECT f FROM FilterField f LEFT JOIN FETCH f.options WHERE f.isActive = true AND f.fieldType = 'TEXT' ORDER BY f.sortOrder")
    List<FilterField> findTextFiltersWithOptions();

    boolean existsByFieldCode(String fieldCode);
}
