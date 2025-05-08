package com.example.currency_exchange.Service;

import com.example.currency_exchange.Model.Currency;
import com.example.currency_exchange.Model.ExchangesRates;
import com.example.currency_exchange.Repository.ExchangeRateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ExchangeRateService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ExchangeRateRepository repository;

    public Double getExchangeRate(String baseCurrency, String targetCurrency) {
        String url = "https://apilayer.net/api/live?access_key=d966635574d010d33662189c101700a4&currencies=EUR,RUB,KGS&source=USD&format=1";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        Map<String, Object> quotes = (Map<String, Object>) response.getBody().get("quotes");

        // Формируем правильный ключ валюты (например, "USDEUR")
        String currencyKey = baseCurrency.toUpperCase() + targetCurrency.toUpperCase();

        if (quotes == null || !quotes.containsKey(currencyKey)) {
            throw new IllegalArgumentException("❌ Ошибка: API не содержит курс " + currencyKey + ". JSON-ответ: " + response.getBody());
        }

        System.out.println("💰 Полученный курс " + currencyKey + ": " + quotes.get(currencyKey));

        return (Double) quotes.get(currencyKey);
    }



    public ExchangeRateService(ExchangeRateRepository repository) {
        this.repository = repository;
    }


    public void saveExchangeRate(int baseCurrency, int targetCurrency, BigDecimal rate) {
        rate = rate.setScale(6, RoundingMode.HALF_UP); // Округляем перед использованием
        ExchangesRates exchangeRate = new ExchangesRates(baseCurrency, targetCurrency, rate);
        repository.save(exchangeRate);
    }
    @Scheduled(fixedRate = 86400000)
    @Scheduled(fixedRate = 86400000)
    public void updateRates() {
        try {
            Double rate = getExchangeRate("USD", "EUR");

            // Проверяем, не null ли rate
            if (rate == null) {
                throw new IllegalArgumentException("Ошибка: API вернул null вместо курса валют!");
            }

            saveExchangeRate(1, 2, BigDecimal.valueOf(rate));
            System.out.println("✅ Курс USD → EUR обновлен: " + rate);
        } catch (Exception e) {
            System.err.println("❌ Ошибка в updateRates: " + e.getMessage());
            e.printStackTrace();
        }
    }



}
