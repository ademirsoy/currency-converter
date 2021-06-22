package com.itembase.currencyconverter.service;

import com.itembase.currencyconverter.exception.CurrencyProviderException;
import com.itembase.currencyconverter.exception.InvalidConversionRequestException;
import com.itembase.currencyconverter.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Service
public class ConverterService {

    final ExchangeRateApiClient exchangeRateApiClient;
    final CurrencyLayerApiClient currencyLayerApiClient;
    final RandomGenerator randomGenerator;

    public ConverterService(ExchangeRateApiClient exchangeRateApiClient, CurrencyLayerApiClient currencyLayerApiClient, RandomGenerator randomGenerator) {
        this.exchangeRateApiClient = exchangeRateApiClient;
        this.currencyLayerApiClient = currencyLayerApiClient;
        this.randomGenerator = randomGenerator;
    }

    public Mono<ConversionResponse> convert(ConversionRequest request) {
        if (randomGenerator.randomBoolean()) {
            return convertWithExchangeRateApi(request);
        } else {
            return convertWithCurrencyLayerApi(request);
        }
    }

    private Mono<ConversionResponse> convertWithExchangeRateApi(ConversionRequest request) {
        return this.convertWithExchangeRateApi(request, true);
    }

    private Mono<ConversionResponse> convertWithCurrencyLayerApi(ConversionRequest request) {
        return this.convertWithCurrencyLayerApi(request, true);
    }

    private Mono<ConversionResponse> convertWithExchangeRateApi(ConversionRequest request, Boolean retryOnError) {
        log.info("Currency conversion will be performed with Exchange Rate API from " + request.getFrom() + " to " + request.getTo());
        Mono<ExchangeRateApiResponse> exchangeRateResponse = this.exchangeRateApiClient.getRates(request.getFrom());
        return exchangeRateResponse
                .map(exchangeRateApiResponse -> calculateConversion(request, exchangeRateApiResponse))
                .onErrorResume(e -> retryOnError ? convertWithCurrencyLayerApi(request, false) : Mono.error(e));
    }

    private Mono<ConversionResponse> convertWithCurrencyLayerApi(ConversionRequest request, Boolean retryOnError) {
        log.info("Currency conversion will be performed with Currency Layer API from " + request.getFrom() + " to " + request.getTo());
        Mono<CurrencyLayerApiResponse> apiResponse = this.currencyLayerApiClient.getRates();
        return apiResponse
                .map(currencyLayerApiResponse -> calculateConversion(request, currencyLayerApiResponse))
                .onErrorResume(e -> retryOnError ? convertWithExchangeRateApi(request, false): Mono.error(e));
    }

    private ConversionResponse calculateConversion(ConversionRequest request, CurrencyApiResponse apiResponse) {
        ConversionResponse conversionResponse = new ConversionResponse(request);
        Map<String, BigDecimal> rates = apiResponse.getRates();

        if (!apiResponse.isSuccess() || rates == null) {
            log.warn("Could not retrieve rates from external API: {}", apiResponse.getError());
            throw new CurrencyProviderException(apiResponse.getError());
        }
        String currencyKeyPrefix = apiResponse instanceof CurrencyLayerApiResponse ? request.getFrom().toUpperCase() : "";
        BigDecimal exchangeRate = rates.get(currencyKeyPrefix + request.getTo().toUpperCase());

        if (exchangeRate == null) {
            String message = "Requested currency not available. From: " + request.getFrom() + " To: " + request.getTo();
            log.warn(message);
            throw new InvalidConversionRequestException(message);
        }
        BigDecimal convertedValue = request.getAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_EVEN);
        conversionResponse.setConverted(convertedValue);
        log.info("Converted {} {} to {} {}", request.getAmount(), request.getFrom(), convertedValue, request.getTo());
        return conversionResponse;
    }
}
