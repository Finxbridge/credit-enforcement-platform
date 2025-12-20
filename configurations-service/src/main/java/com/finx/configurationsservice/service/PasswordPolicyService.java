package com.finx.configurationsservice.service;

import com.finx.configurationsservice.domain.dto.CreatePasswordPolicyRequest;
import com.finx.configurationsservice.domain.dto.PasswordPolicyDTO;
import com.finx.configurationsservice.domain.enums.PolicyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PasswordPolicyService {

    PasswordPolicyDTO createPolicy(CreatePasswordPolicyRequest request);

    PasswordPolicyDTO getPolicyById(Long id);

    PasswordPolicyDTO getPolicyByCode(String policyCode);

    PasswordPolicyDTO getDefaultPolicy();

    List<PasswordPolicyDTO> getActivePolicies();

    List<PasswordPolicyDTO> getPoliciesByLevel(PolicyLevel level);

    Page<PasswordPolicyDTO> getAllPolicies(Pageable pageable);

    PasswordPolicyDTO updatePolicy(Long id, CreatePasswordPolicyRequest request);

    PasswordPolicyDTO setAsDefault(Long id);

    PasswordPolicyDTO activatePolicy(Long id);

    PasswordPolicyDTO deactivatePolicy(Long id);

    void deletePolicy(Long id);

    boolean validatePassword(String password, Long policyId);
}
