package com.finx.configurationsservice.service;

import com.finx.configurationsservice.domain.dto.CreateCurrencyRequest;
import com.finx.configurationsservice.domain.dto.CurrencyDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CurrencyService {

    CurrencyDTO createCurrency(CreateCurrencyRequest request);

    CurrencyDTO getCurrencyById(Long id);

    CurrencyDTO getCurrencyByCode(String currencyCode);

    List<CurrencyDTO> getActiveCurrencies();

    CurrencyDTO getBaseCurrency();

    Page<CurrencyDTO> getAllCurrencies(Pageable pageable);

    CurrencyDTO updateCurrency(Long id, CreateCurrencyRequest request);

    CurrencyDTO updateExchangeRate(Long id, BigDecimal exchangeRate);

    CurrencyDTO setAsBaseCurrency(Long id);

    CurrencyDTO activateCurrency(Long id);

    CurrencyDTO deactivateCurrency(Long id);

    void deleteCurrency(Long id);
}
