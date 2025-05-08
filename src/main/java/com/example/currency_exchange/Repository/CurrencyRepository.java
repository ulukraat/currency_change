package com.example.currency_exchange.Repository;

import com.example.currency_exchange.Model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
    Currency findByCode(String code);
}