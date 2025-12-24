package com.finx.allocationreallocationservice.repository;

import com.finx.allocationreallocationservice.domain.entity.CaseEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseEventRepository extends JpaRepository<CaseEvent, Long> {

    Optional<CaseEvent> findByEventId(String eventId);

    List<CaseEvent> findByCaseIdOrderByEventTimestampDesc(Long caseId);

    List<CaseEvent> findByCaseIdAndEventCategoryOrderByEventTimestampDesc(Long caseId, String eventCategory);
}
