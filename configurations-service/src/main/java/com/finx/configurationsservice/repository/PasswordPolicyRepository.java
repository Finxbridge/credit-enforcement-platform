package com.finx.configurationsservice.repository;

import com.finx.configurationsservice.domain.entity.PasswordPolicy;
import com.finx.configurationsservice.domain.enums.PolicyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordPolicyRepository extends JpaRepository<PasswordPolicy, Long> {

    Optional<PasswordPolicy> findByPolicyCode(String policyCode);

    List<PasswordPolicy> findByIsActiveTrue();

    Optional<PasswordPolicy> findByIsDefaultTrue();

    List<PasswordPolicy> findByPolicyLevel(PolicyLevel policyLevel);

    boolean existsByPolicyCode(String policyCode);
}
