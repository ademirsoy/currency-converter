package com.itembase.currencyconverter.service;

import com.itembase.currencyconverter.exception.CurrencyProviderException;
import com.itembase.currencyconverter.exception.InvalidConversionRequestException;
import com.itembase.currencyconverter.model.CurrencyLayerApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CurrencyLayerApiClient {

    private final WebClient client;

    public CurrencyLayerApiClient(WebClient currencyLayerClient) {
        this.client = currencyLayerClient;
    }

    public Mono<CurrencyLayerApiResponse> getRates() {

        return this.client.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        clientResponse -> Mono.error(new InvalidConversionRequestException("Currency Layer API call failed")))
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse -> Mono.error(new CurrencyProviderException("Internal Server Error: Currency Layer API call failed")))
                .bodyToMono(CurrencyLayerApiResponse.class);
    }
}
