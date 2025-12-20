package com.finx.configurationsservice.repository;

import com.finx.configurationsservice.domain.entity.LmsConfiguration;
import com.finx.configurationsservice.domain.enums.LmsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LmsConfigurationRepository extends JpaRepository<LmsConfiguration, Long> {

    Optional<LmsConfiguration> findByLmsCode(String lmsCode);

    boolean existsByLmsCode(String lmsCode);

    List<LmsConfiguration> findByLmsType(LmsType lmsType);

    List<LmsConfiguration> findByIsActiveTrue();

    List<LmsConfiguration> findByLmsTypeAndIsActiveTrue(LmsType lmsType);

    @Query("SELECT l FROM LmsConfiguration l WHERE " +
           "(:type IS NULL OR l.lmsType = :type) AND " +
           "(:isActive IS NULL OR l.isActive = :isActive) AND " +
           "(:search IS NULL OR LOWER(l.lmsName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(l.lmsCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<LmsConfiguration> findWithFilters(
            @Param("type") LmsType type,
            @Param("isActive") Boolean isActive,
            @Param("search") String search,
            Pageable pageable);
}
