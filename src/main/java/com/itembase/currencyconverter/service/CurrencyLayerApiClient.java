package com.itembase.currencyconverter.service;

import com.itembase.currencyconverter.exception.CurrencyProviderException;
import com.itembase.currencyconverter.exception.InvalidConversionRequestException;
import com.itembase.currencyconverter.model.CurrencyLayerApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
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
                        clientResponse -> {
                            logResponse(clientResponse);
                            return Mono.error(new InvalidConversionRequestException("Currency Layer API call failed"));
                        })
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse -> {
                            logResponse(clientResponse);
                            return Mono.error(new CurrencyProviderException("Currency Layer API call failed"));
                        })
                .bodyToMono(CurrencyLayerApiResponse.class);
    }

    private Mono<ClientResponse> logResponse(ClientResponse clientResponse) {
        return clientResponse
                .bodyToMono(String.class)
                .flatMap(body -> {
                    log.warn("Currency Layer API call failed: {}", body);
                    return Mono.just(clientResponse);
                });
    }
}
