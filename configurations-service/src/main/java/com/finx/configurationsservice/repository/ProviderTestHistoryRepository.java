package com.finx.configurationsservice.repository;

import com.finx.configurationsservice.domain.entity.ProviderTestHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderTestHistoryRepository extends JpaRepository<ProviderTestHistory, Long> {

    List<ProviderTestHistory> findByProviderIdOrderByTestedAtDesc(Long providerId);

    Page<ProviderTestHistory> findByProviderId(Long providerId, Pageable pageable);

    List<ProviderTestHistory> findTop10ByProviderIdOrderByTestedAtDesc(Long providerId);
}
