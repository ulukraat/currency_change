package com.example.currency_exchange.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rate")
@Data
public class ExchangesRates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private int baseCurrencyId;
    @Column(nullable = false)
    private int targetCurrencyId;
    @Column(nullable = false,precision = 10, scale = 6)
    private BigDecimal rate;
    @Column(nullable = false)
    private LocalDateTime updated;

    public ExchangesRates(int baseCurrencyId, int targetCurrencyId, BigDecimal Rate) {
        this.baseCurrencyId = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate = Rate;
        this.updated = LocalDateTime.now();
    }

    public ExchangesRates() {

    }
}
