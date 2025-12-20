package com.finx.configurationsservice.service.impl;

import com.finx.configurationsservice.domain.dto.CreateCurrencyRequest;
import com.finx.configurationsservice.domain.dto.CurrencyDTO;
import com.finx.configurationsservice.domain.entity.Currency;
import com.finx.configurationsservice.exception.BusinessException;
import com.finx.configurationsservice.exception.ResourceNotFoundException;
import com.finx.configurationsservice.mapper.ConfigurationsMapper;
import com.finx.configurationsservice.repository.CurrencyRepository;
import com.finx.configurationsservice.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final ConfigurationsMapper mapper;

    @Override
    @CacheEvict(value = "currencies", allEntries = true)
    public CurrencyDTO createCurrency(CreateCurrencyRequest request) {
        if (currencyRepository.existsByCurrencyCode(request.getCurrencyCode())) {
            throw new BusinessException("Currency with code " + request.getCurrencyCode() + " already exists");
        }

        Currency currency = Currency.builder()
                .currencyCode(request.getCurrencyCode().toUpperCase())
                .currencyName(request.getCurrencyName())
                .currencySymbol(request.getCurrencySymbol())
                .decimalPlaces(request.getDecimalPlaces() != null ? request.getDecimalPlaces() : 2)
                .exchangeRate(request.getExchangeRate() != null ? request.getExchangeRate() : BigDecimal.ONE)
                .isBaseCurrency(request.getIsBaseCurrency() != null && request.getIsBaseCurrency())
                .isActive(true)
                .build();

        if (currency.getIsBaseCurrency()) {
            clearExistingBaseCurrency();
        }

        currency = currencyRepository.save(currency);
        log.info("Created currency: {}", currency.getCurrencyCode());
        return mapper.toDto(currency);
    }

    @Override
    @Cacheable(value = "currencies", key = "#id")
    @Transactional(readOnly = true)
    public CurrencyDTO getCurrencyById(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));
        return mapper.toDto(currency);
    }

    @Override
    @Cacheable(value = "currencies", key = "'code-' + #currencyCode")
    @Transactional(readOnly = true)
    public CurrencyDTO getCurrencyByCode(String currencyCode) {
        Currency currency = currencyRepository.findByCurrencyCode(currencyCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with code: " + currencyCode));
        return mapper.toDto(currency);
    }

    @Override
    @Cacheable(value = "currencies", key = "'active'")
    @Transactional(readOnly = true)
    public List<CurrencyDTO> getActiveCurrencies() {
        return mapper.toCurrencyDtoList(currencyRepository.findByIsActiveTrue());
    }

    @Override
    @Cacheable(value = "currencies", key = "'base'")
    @Transactional(readOnly = true)
    public CurrencyDTO getBaseCurrency() {
        Currency currency = currencyRepository.findByIsBaseCurrencyTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No base currency configured"));
        return mapper.toDto(currency);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CurrencyDTO> getAllCurrencies(Pageable pageable) {
        return currencyRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @CacheEvict(value = "currencies", allEntries = true)
    public CurrencyDTO updateCurrency(Long id, CreateCurrencyRequest request) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));

        if (!currency.getCurrencyCode().equals(request.getCurrencyCode().toUpperCase()) &&
                currencyRepository.existsByCurrencyCode(request.getCurrencyCode())) {
            throw new BusinessException("Currency with code " + request.getCurrencyCode() + " already exists");
        }

        currency.setCurrencyCode(request.getCurrencyCode().toUpperCase());
        currency.setCurrencyName(request.getCurrencyName());
        currency.setCurrencySymbol(request.getCurrencySymbol());
        if (request.getDecimalPlaces() != null) {
            currency.setDecimalPlaces(request.getDecimalPlaces());
        }
        if (request.getExchangeRate() != null) {
            currency.setExchangeRate(request.getExchangeRate());
            currency.setExchangeRateUpdatedAt(LocalDateTime.now());
        }

        if (request.getIsBaseCurrency() != null && request.getIsBaseCurrency() && !currency.getIsBaseCurrency()) {
            clearExistingBaseCurrency();
            currency.setIsBaseCurrency(true);
        }

        currency = currencyRepository.save(currency);
        log.info("Updated currency: {}", currency.getCurrencyCode());
        return mapper.toDto(currency);
    }

    @Override
    @CacheEvict(value = "currencies", allEntries = true)
    public CurrencyDTO updateExchangeRate(Long id, BigDecimal exchangeRate) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));

        currency.setExchangeRate(exchangeRate);
        currency.setExchangeRateUpdatedAt(LocalDateTime.now());

        currency = currencyRepository.save(currency);
        log.info("Updated exchange rate for currency {}: {}", currency.getCurrencyCode(), exchangeRate);
        return mapper.toDto(currency);
    }

    @Override
    @CacheEvict(value = "currencies", allEntries = true)
    public CurrencyDTO setAsBaseCurrency(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));

        clearExistingBaseCurrency();
        currency.setIsBaseCurrency(true);
        currency.setExchangeRate(BigDecimal.ONE);

        currency = currencyRepository.save(currency);
        log.info("Set {} as base currency", currency.getCurrencyCode());
        return mapper.toDto(currency);
    }

    @Override
    @CacheEvict(value = "currencies", allEntries = true)
    public CurrencyDTO activateCurrency(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));

        currency.setIsActive(true);
        currency = currencyRepository.save(currency);
        log.info("Activated currency: {}", currency.getCurrencyCode());
        return mapper.toDto(currency);
    }

    @Override
    @CacheEvict(value = "currencies", allEntries = true)
    public CurrencyDTO deactivateCurrency(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));

        if (currency.getIsBaseCurrency()) {
            throw new BusinessException("Cannot deactivate base currency");
        }

        currency.setIsActive(false);
        currency = currencyRepository.save(currency);
        log.info("Deactivated currency: {}", currency.getCurrencyCode());
        return mapper.toDto(currency);
    }

    @Override
    @CacheEvict(value = "currencies", allEntries = true)
    public void deleteCurrency(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));

        if (currency.getIsBaseCurrency()) {
            throw new BusinessException("Cannot delete base currency");
        }

        currencyRepository.delete(currency);
        log.info("Deleted currency: {}", currency.getCurrencyCode());
    }

    private void clearExistingBaseCurrency() {
        currencyRepository.findByIsBaseCurrencyTrue().ifPresent(baseCurrency -> {
            baseCurrency.setIsBaseCurrency(false);
            currencyRepository.save(baseCurrency);
        });
    }
}
