package com.example.currency_exchange.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rate")
@Data
public class ExchangesRates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "baseCurrency")
    private Currency baseCurrencyId;
    @ManyToOne
    @JoinColumn(name = "targetCurrency")
    private Currency targetCurrencyId;
    @Column(precision = 10, scale = 6)
    private BigDecimal rate;
    @Column(nullable = false)
    private LocalDateTime updated;



    public ExchangesRates(Currency baseCurrencyId, Currency targetCurrencyId, BigDecimal Rate) {
        this.baseCurrencyId = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate = Rate;
        this.updated = LocalDateTime.now();
    }

}
