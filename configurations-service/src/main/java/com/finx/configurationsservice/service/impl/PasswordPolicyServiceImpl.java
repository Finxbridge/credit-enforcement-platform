package com.finx.configurationsservice.service.impl;

import com.finx.configurationsservice.domain.dto.CreatePasswordPolicyRequest;
import com.finx.configurationsservice.domain.dto.PasswordPolicyDTO;
import com.finx.configurationsservice.domain.entity.PasswordPolicy;
import com.finx.configurationsservice.domain.enums.PolicyLevel;
import com.finx.configurationsservice.exception.BusinessException;
import com.finx.configurationsservice.exception.ResourceNotFoundException;
import com.finx.configurationsservice.mapper.ConfigurationsMapper;
import com.finx.configurationsservice.repository.PasswordPolicyRepository;
import com.finx.configurationsservice.service.PasswordPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordPolicyServiceImpl implements PasswordPolicyService {

    private final PasswordPolicyRepository policyRepository;
    private final ConfigurationsMapper mapper;

    @Override
    @CacheEvict(value = "passwordPolicies", allEntries = true)
    public PasswordPolicyDTO createPolicy(CreatePasswordPolicyRequest request) {
        if (policyRepository.existsByPolicyCode(request.getPolicyCode())) {
            throw new BusinessException("Password policy with code " + request.getPolicyCode() + " already exists");
        }

        PasswordPolicy policy = PasswordPolicy.builder()
                .policyCode(request.getPolicyCode())
                .policyName(request.getPolicyName())
                .policyLevel(request.getPolicyLevel())
                .minLength(request.getMinLength())
                .maxLength(request.getMaxLength())
                .requireUppercase(request.getRequireUppercase())
                .requireLowercase(request.getRequireLowercase())
                .requireNumber(request.getRequireNumber())
                .requireSpecialChar(request.getRequireSpecialChar())
                .specialCharsAllowed(request.getSpecialCharsAllowed())
                .passwordHistoryCount(request.getPasswordHistoryCount())
                .preventReuseDays(request.getPreventReuseDays())
                .passwordExpiryDays(request.getPasswordExpiryDays())
                .warnBeforeExpiryDays(request.getWarnBeforeExpiryDays())
                .maxFailedAttempts(request.getMaxFailedAttempts())
                .lockoutDurationMinutes(request.getLockoutDurationMinutes())
                .isDefault(request.getIsDefault() != null && request.getIsDefault())
                .isActive(true)
                .build();

        if (policy.getIsDefault()) {
            clearExistingDefault();
        }

        policy = policyRepository.save(policy);
        log.info("Created password policy: {}", policy.getPolicyCode());
        return mapper.toDto(policy);
    }

    @Override
    @Cacheable(value = "passwordPolicies", key = "#id")
    @Transactional(readOnly = true)
    public PasswordPolicyDTO getPolicyById(Long id) {
        PasswordPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password policy not found with id: " + id));
        return mapper.toDto(policy);
    }

    @Override
    @Cacheable(value = "passwordPolicies", key = "'code-' + #policyCode")
    @Transactional(readOnly = true)
    public PasswordPolicyDTO getPolicyByCode(String policyCode) {
        PasswordPolicy policy = policyRepository.findByPolicyCode(policyCode)
                .orElseThrow(() -> new ResourceNotFoundException("Password policy not found with code: " + policyCode));
        return mapper.toDto(policy);
    }

    @Override
    @Cacheable(value = "passwordPolicies", key = "'default'")
    @Transactional(readOnly = true)
    public PasswordPolicyDTO getDefaultPolicy() {
        PasswordPolicy policy = policyRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No default password policy configured"));
        return mapper.toDto(policy);
    }

    @Override
    @Cacheable(value = "passwordPolicies", key = "'active'")
    @Transactional(readOnly = true)
    public List<PasswordPolicyDTO> getActivePolicies() {
        return mapper.toPasswordPolicyDtoList(policyRepository.findByIsActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PasswordPolicyDTO> getPoliciesByLevel(PolicyLevel level) {
        return mapper.toPasswordPolicyDtoList(policyRepository.findByPolicyLevel(level));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PasswordPolicyDTO> getAllPolicies(Pageable pageable) {
        return policyRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @CacheEvict(value = "passwordPolicies", allEntries = true)
    public PasswordPolicyDTO updatePolicy(Long id, CreatePasswordPolicyRequest request) {
        PasswordPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password policy not found with id: " + id));

        if (!policy.getPolicyCode().equals(request.getPolicyCode()) &&
                policyRepository.existsByPolicyCode(request.getPolicyCode())) {
            throw new BusinessException("Password policy with code " + request.getPolicyCode() + " already exists");
        }

        policy.setPolicyCode(request.getPolicyCode());
        policy.setPolicyName(request.getPolicyName());
        policy.setPolicyLevel(request.getPolicyLevel());
        if (request.getMinLength() != null) policy.setMinLength(request.getMinLength());
        if (request.getMaxLength() != null) policy.setMaxLength(request.getMaxLength());
        if (request.getRequireUppercase() != null) policy.setRequireUppercase(request.getRequireUppercase());
        if (request.getRequireLowercase() != null) policy.setRequireLowercase(request.getRequireLowercase());
        if (request.getRequireNumber() != null) policy.setRequireNumber(request.getRequireNumber());
        if (request.getRequireSpecialChar() != null) policy.setRequireSpecialChar(request.getRequireSpecialChar());
        if (request.getSpecialCharsAllowed() != null) policy.setSpecialCharsAllowed(request.getSpecialCharsAllowed());
        if (request.getPasswordHistoryCount() != null) policy.setPasswordHistoryCount(request.getPasswordHistoryCount());
        if (request.getPreventReuseDays() != null) policy.setPreventReuseDays(request.getPreventReuseDays());
        if (request.getPasswordExpiryDays() != null) policy.setPasswordExpiryDays(request.getPasswordExpiryDays());
        if (request.getWarnBeforeExpiryDays() != null) policy.setWarnBeforeExpiryDays(request.getWarnBeforeExpiryDays());
        if (request.getMaxFailedAttempts() != null) policy.setMaxFailedAttempts(request.getMaxFailedAttempts());
        if (request.getLockoutDurationMinutes() != null) policy.setLockoutDurationMinutes(request.getLockoutDurationMinutes());

        if (request.getIsDefault() != null && request.getIsDefault() && !policy.getIsDefault()) {
            clearExistingDefault();
            policy.setIsDefault(true);
        }

        policy = policyRepository.save(policy);
        log.info("Updated password policy: {}", policy.getPolicyCode());
        return mapper.toDto(policy);
    }

    @Override
    @CacheEvict(value = "passwordPolicies", allEntries = true)
    public PasswordPolicyDTO setAsDefault(Long id) {
        PasswordPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password policy not found with id: " + id));

        clearExistingDefault();
        policy.setIsDefault(true);

        policy = policyRepository.save(policy);
        log.info("Set {} as default password policy", policy.getPolicyCode());
        return mapper.toDto(policy);
    }

    @Override
    @CacheEvict(value = "passwordPolicies", allEntries = true)
    public PasswordPolicyDTO activatePolicy(Long id) {
        PasswordPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password policy not found with id: " + id));

        policy.setIsActive(true);
        policy = policyRepository.save(policy);
        log.info("Activated password policy: {}", policy.getPolicyCode());
        return mapper.toDto(policy);
    }

    @Override
    @CacheEvict(value = "passwordPolicies", allEntries = true)
    public PasswordPolicyDTO deactivatePolicy(Long id) {
        PasswordPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password policy not found with id: " + id));

        if (policy.getIsDefault()) {
            throw new BusinessException("Cannot deactivate default password policy");
        }

        policy.setIsActive(false);
        policy = policyRepository.save(policy);
        log.info("Deactivated password policy: {}", policy.getPolicyCode());
        return mapper.toDto(policy);
    }

    @Override
    @CacheEvict(value = "passwordPolicies", allEntries = true)
    public void deletePolicy(Long id) {
        PasswordPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password policy not found with id: " + id));

        if (policy.getIsDefault()) {
            throw new BusinessException("Cannot delete default password policy");
        }

        policyRepository.delete(policy);
        log.info("Deleted password policy: {}", policy.getPolicyCode());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePassword(String password, Long policyId) {
        PasswordPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Password policy not found with id: " + policyId));

        if (password.length() < policy.getMinLength()) {
            return false;
        }
        if (policy.getMaxLength() != null && password.length() > policy.getMaxLength()) {
            return false;
        }
        if (Boolean.TRUE.equals(policy.getRequireUppercase()) && !Pattern.compile("[A-Z]").matcher(password).find()) {
            return false;
        }
        if (Boolean.TRUE.equals(policy.getRequireLowercase()) && !Pattern.compile("[a-z]").matcher(password).find()) {
            return false;
        }
        if (Boolean.TRUE.equals(policy.getRequireNumber()) && !Pattern.compile("[0-9]").matcher(password).find()) {
            return false;
        }
        if (Boolean.TRUE.equals(policy.getRequireSpecialChar())) {
            String specialChars = policy.getSpecialCharsAllowed() != null ?
                    Pattern.quote(policy.getSpecialCharsAllowed()) : "!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?";
            if (!Pattern.compile("[" + specialChars + "]").matcher(password).find()) {
                return false;
            }
        }

        return true;
    }

    private void clearExistingDefault() {
        policyRepository.findByIsDefaultTrue().ifPresent(defaultPolicy -> {
            defaultPolicy.setIsDefault(false);
            policyRepository.save(defaultPolicy);
        });
    }
}
