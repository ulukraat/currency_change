package com.example.currency_exchange.Service;

import com.example.currency_exchange.Model.Currency;
import com.example.currency_exchange.Model.ExchangesRates;
import com.example.currency_exchange.Repository.CurrencyRepository;
import com.example.currency_exchange.Repository.ExchangeRateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Service
public class ExchangeRateService {
    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ExchangeRateRepository repository;

    // URL для API запроса
    private static final String API_URL = "https://apilayer.net/api/live?access_key=d966635574d010d33662189c101700a4&currencies=EUR,USD,RUB&source=KGS&format=1";

    // Кэшированные данные
    private Map<String, Object> cachedQuotes = null;
    private LocalDateTime lastUpdateTime = null;

    // Время жизни кэша (в минутах)
    private static final int CACHE_TTL_MINUTES = 1440; // 24 часа

    // Список поддерживаемых валют
    private final List<String> supportedCurrencies = Arrays.asList("EUR", "USD", "RUB");

    public ExchangeRateService(ExchangeRateRepository repository, CurrencyRepository currencyRepository) {
        this.repository = repository;
        this.currencyRepository = currencyRepository;
    }

    /**
     * Получает курсы валют из API или из кэша, если данные актуальны
     */
    public Map<String, Object> getQuotes() {
        boolean needUpdate = cachedQuotes == null || lastUpdateTime == null ||
                LocalDateTime.now().minusMinutes(CACHE_TTL_MINUTES).isAfter(lastUpdateTime);

        if (needUpdate) {
            System.out.println("Запрашиваем новые данные из API...");
            try {
                ResponseEntity<Map> response = restTemplate.getForEntity(API_URL, Map.class);
                Map<String, Object> body = response.getBody();

                if (body != null && body.containsKey("quotes")) {
                    // Обновляем кэш и время последнего обновления
                    cachedQuotes = (Map<String, Object>) body.get("quotes");
                    lastUpdateTime = LocalDateTime.now();
                    System.out.println("Кэш обновлен: " + lastUpdateTime);
                } else {
                    System.err.println("API вернул некорректные данные: " + body);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при обращении к API: " + e.getMessage());
                if (cachedQuotes != null) {
                    System.out.println("Используем последние кэшированные данные из-за ошибки API");
                }
            }
        } else {
            System.out.println("Используем кэшированные данные (возраст: " +
                    java.time.Duration.between(lastUpdateTime, LocalDateTime.now()).toMinutes() +
                    " минут)");
        }

        return cachedQuotes;
    }

    /**
     * Получает курс обмена для конкретной пары валют
     */
    public Double getExchangeRate(String baseCurrency, String targetCurrency) throws InterruptedException {
        // Добавим задержку для стабильности
        Thread.sleep(500);

        Map<String, Object> quotes = getQuotes();

        if (quotes == null) {
            throw new IllegalArgumentException("Не удалось получить данные о курсах валют");
        }

        String currencyKey = baseCurrency.toUpperCase() + targetCurrency.toUpperCase();

        if (!quotes.containsKey(currencyKey)) {
            throw new IllegalArgumentException("Курс для пары " + currencyKey + " не найден");
        }

        System.out.println("Полученный курс " + currencyKey + ": " + quotes.get(currencyKey));

        return (Double) quotes.get(currencyKey);
    }

    /**
     * Сохраняет курс валюты в базу данных
     */
    public void saveExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        // Округляем до 6 знаков после запятой
        rate = rate.setScale(6, RoundingMode.HALF_UP);

        // Находим валюты по коду
        Currency baseCurrency = currencyRepository.findByCode(baseCurrencyCode);
        Currency targetCurrency = currencyRepository.findByCode(targetCurrencyCode);

        if (baseCurrency == null || targetCurrency == null) {
            throw new IllegalArgumentException("Ошибка: Одна из валют не найдена в базе! Base: " +
                    baseCurrencyCode + ", Target: " + targetCurrencyCode);
        }

        // Создаем новую запись курса
        ExchangesRates exchangeRate = new ExchangesRates(baseCurrency, targetCurrency, rate);
        repository.save(exchangeRate);
        System.out.println("Сохранен курс: " + baseCurrencyCode + " -> " + targetCurrencyCode + " = " + rate);
    }

    /**
     * Обновляет курсы валют по расписанию (раз в сутки)
     */
    @Scheduled(fixedRate = 86400000) // 24 часа
    public void updateRates() {
        try {
            // Принудительно обновляем кэш при плановом обновлении
            lastUpdateTime = null;
            Map<String, Object> quotes = getQuotes();

            if (quotes == null || quotes.isEmpty()) {
                throw new IllegalArgumentException("Ошибка: API вернул пустые данные курсов валют!");
            }

            for (String targetCurrency : supportedCurrencies) {
                String currencyKey = "KGS" + targetCurrency;
                if (quotes.containsKey(currencyKey)) {
                    Double rate = (Double) quotes.get(currencyKey);
                    saveExchangeRate("KGS", targetCurrency, BigDecimal.valueOf(rate));
                    System.out.println("Обновлен курс для " + targetCurrency + ": " + rate);
                } else {
                    System.err.println("Курс для " + currencyKey + " не найден в API ответе");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка в updateRates: " + e.getMessage());
            e.printStackTrace();
        }
    }
}