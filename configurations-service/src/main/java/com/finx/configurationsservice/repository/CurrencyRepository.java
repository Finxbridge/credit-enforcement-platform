package com.finx.configurationsservice.repository;

import com.finx.configurationsservice.domain.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    Optional<Currency> findByCurrencyCode(String currencyCode);

    List<Currency> findByIsActiveTrue();

    Optional<Currency> findByIsBaseCurrencyTrue();

    boolean existsByCurrencyCode(String currencyCode);
}
