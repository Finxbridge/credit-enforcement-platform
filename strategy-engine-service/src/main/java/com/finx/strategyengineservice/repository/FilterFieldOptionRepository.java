package com.finx.strategyengineservice.repository;

import com.finx.strategyengineservice.domain.entity.FilterFieldOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilterFieldOptionRepository extends JpaRepository<FilterFieldOption, Long> {

    List<FilterFieldOption> findByFilterFieldIdAndIsActiveTrueOrderBySortOrder(Long filterFieldId);

    List<FilterFieldOption> findByFilterFieldIdOrderBySortOrder(Long filterFieldId);
}
