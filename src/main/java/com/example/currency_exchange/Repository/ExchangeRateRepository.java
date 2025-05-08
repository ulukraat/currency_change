package com.example.currency_exchange.Repository;

import com.example.currency_exchange.Model.ExchangesRates;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangesRates, Long> {
    public ExchangesRates findById(long id);
}
