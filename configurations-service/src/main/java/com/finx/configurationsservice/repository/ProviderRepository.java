package com.finx.configurationsservice.repository;

import com.finx.configurationsservice.domain.entity.Provider;
import com.finx.configurationsservice.domain.enums.ProviderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByProviderCode(String providerCode);

    boolean existsByProviderCode(String providerCode);

    List<Provider> findByProviderType(ProviderType providerType);

    List<Provider> findByProviderTypeAndIsActiveTrue(ProviderType providerType);

    List<Provider> findByIsActiveTrue();

    Optional<Provider> findByProviderTypeAndIsDefaultTrue(ProviderType providerType);

    @Query("SELECT p FROM Provider p WHERE p.isActive = true ORDER BY p.priorityOrder DESC, p.providerName ASC")
    List<Provider> findAllActiveOrderByPriority();

    @Query("SELECT p FROM Provider p WHERE p.providerType = :type AND p.isActive = true ORDER BY p.priorityOrder DESC")
    List<Provider> findActiveByTypeOrderByPriority(@Param("type") ProviderType type);

    Page<Provider> findByProviderTypeAndIsActive(ProviderType providerType, Boolean isActive, Pageable pageable);

    @Query("SELECT p FROM Provider p WHERE " +
           "(:type IS NULL OR p.providerType = :type) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive) AND " +
           "(:search IS NULL OR LOWER(p.providerName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.providerCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Provider> findWithFilters(
            @Param("type") ProviderType type,
            @Param("isActive") Boolean isActive,
            @Param("search") String search,
            Pageable pageable);
}
