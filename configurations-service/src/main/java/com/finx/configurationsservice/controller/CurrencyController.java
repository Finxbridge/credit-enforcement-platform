package com.finx.configurationsservice.controller;

import com.finx.configurationsservice.domain.dto.CommonResponse;
import com.finx.configurationsservice.domain.dto.CreateCurrencyRequest;
import com.finx.configurationsservice.domain.dto.CurrencyDTO;
import com.finx.configurationsservice.service.CurrencyService;
import com.finx.configurationsservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/config/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @PostMapping
    public ResponseEntity<CommonResponse<CurrencyDTO>> createCurrency(
            @Valid @RequestBody CreateCurrencyRequest request) {
        log.info("POST /currencies - Creating currency: {}", request.getCurrencyCode());
        CurrencyDTO response = currencyService.createCurrency(request);
        return ResponseWrapper.created("Currency created successfully", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<CurrencyDTO>> getCurrencyById(@PathVariable Long id) {
        log.info("GET /currencies/{} - Fetching currency", id);
        CurrencyDTO response = currencyService.getCurrencyById(id);
        return ResponseWrapper.ok("Currency retrieved successfully", response);
    }

    @GetMapping("/code/{currencyCode}")
    public ResponseEntity<CommonResponse<CurrencyDTO>> getCurrencyByCode(@PathVariable String currencyCode) {
        log.info("GET /currencies/code/{} - Fetching currency", currencyCode);
        CurrencyDTO response = currencyService.getCurrencyByCode(currencyCode);
        return ResponseWrapper.ok("Currency retrieved successfully", response);
    }

    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<CurrencyDTO>>> getActiveCurrencies() {
        log.info("GET /currencies/active - Fetching active currencies");
        List<CurrencyDTO> currencies = currencyService.getActiveCurrencies();
        return ResponseWrapper.ok("Active currencies retrieved successfully", currencies);
    }

    @GetMapping("/base")
    public ResponseEntity<CommonResponse<CurrencyDTO>> getBaseCurrency() {
        log.info("GET /currencies/base - Fetching base currency");
        CurrencyDTO response = currencyService.getBaseCurrency();
        return ResponseWrapper.ok("Base currency retrieved successfully", response);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<CurrencyDTO>>> getAllCurrencies(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /currencies - Fetching all currencies");
        Page<CurrencyDTO> currencies = currencyService.getAllCurrencies(pageable);
        return ResponseWrapper.ok("Currencies retrieved successfully", currencies);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<CurrencyDTO>> updateCurrency(
            @PathVariable Long id,
            @Valid @RequestBody CreateCurrencyRequest request) {
        log.info("PUT /currencies/{} - Updating currency", id);
        CurrencyDTO response = currencyService.updateCurrency(id, request);
        return ResponseWrapper.ok("Currency updated successfully", response);
    }

    @PutMapping("/{id}/exchange-rate")
    public ResponseEntity<CommonResponse<CurrencyDTO>> updateExchangeRate(
            @PathVariable Long id,
            @RequestParam BigDecimal exchangeRate) {
        log.info("PUT /currencies/{}/exchange-rate - Updating exchange rate", id);
        CurrencyDTO response = currencyService.updateExchangeRate(id, exchangeRate);
        return ResponseWrapper.ok("Exchange rate updated successfully", response);
    }

    @PostMapping("/{id}/set-base")
    public ResponseEntity<CommonResponse<CurrencyDTO>> setAsBaseCurrency(@PathVariable Long id) {
        log.info("POST /currencies/{}/set-base - Setting as base currency", id);
        CurrencyDTO response = currencyService.setAsBaseCurrency(id);
        return ResponseWrapper.ok("Base currency set successfully", response);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CommonResponse<CurrencyDTO>> activateCurrency(@PathVariable Long id) {
        log.info("POST /currencies/{}/activate - Activating currency", id);
        CurrencyDTO response = currencyService.activateCurrency(id);
        return ResponseWrapper.ok("Currency activated successfully", response);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<CommonResponse<CurrencyDTO>> deactivateCurrency(@PathVariable Long id) {
        log.info("POST /currencies/{}/deactivate - Deactivating currency", id);
        CurrencyDTO response = currencyService.deactivateCurrency(id);
        return ResponseWrapper.ok("Currency deactivated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteCurrency(@PathVariable Long id) {
        log.info("DELETE /currencies/{} - Deleting currency", id);
        currencyService.deleteCurrency(id);
        return ResponseWrapper.ok("Currency deleted successfully", null);
    }
}
