package com.example.currency_exchange.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "currencies")
@Data
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false)
    private String sign;
    @OneToMany(mappedBy = "baseCurrencyId")
    private List<ExchangesRates> baseExchangeRates;
    @OneToMany(mappedBy = "targetCurrencyId")
    private List<ExchangesRates> targetExchangeRates;

}
