package com.itembase.currencyconverter.service;

import com.itembase.currencyconverter.exception.CurrencyProviderException;
import com.itembase.currencyconverter.exception.InvalidConversionRequestException;
import com.itembase.currencyconverter.model.ExchangeRateApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ExchangeRateApiClient {

    private final WebClient client;

    public ExchangeRateApiClient(WebClient exchangeRateClient) {
        this.client = exchangeRateClient;
    }

    public Mono<ExchangeRateApiResponse> getRates(String currency) {

        return this.client.get()
                .uri("/" + currency)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        clientResponse -> clientResponse
                                .bodyToMono(ExchangeRateApiResponse.class)
                                .flatMap(apiResponse -> Mono.error(new InvalidConversionRequestException("Invalid currency rate request: " + apiResponse.getError()))))
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse -> Mono.error(new CurrencyProviderException("Internal Server Error: Exchange Rate API call failed"))
                )
                .bodyToMono(ExchangeRateApiResponse.class);
    }
}
