package com.itembase.currencyconverter.controller;

import com.itembase.currencyconverter.exception.CurrencyProviderException;
import com.itembase.currencyconverter.exception.GlobalExceptionHandler;
import com.itembase.currencyconverter.exception.InvalidConversionRequestException;
import com.itembase.currencyconverter.model.ConversionRequest;
import com.itembase.currencyconverter.model.ConversionResponse;
import com.itembase.currencyconverter.service.ConverterService;
import com.itembase.currencyconverter.service.ValidatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(CurrencyController.class)
@Import(ValidatorService.class)
class CurrencyControllerTest {

    @Autowired
    WebTestClient web;

    @MockBean
    ConverterService converterService;

    @Test
    public void convertCurrency_shouldReturnConvertedAmount() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setFrom("USD");
        request.setTo("TRY");
        request.setAmount(new BigDecimal("10"));
        ConversionResponse response = new ConversionResponse(request);
        response.setAmount(new BigDecimal("86.60"));

        when(converterService.convert(request)).thenReturn(Mono.just(response));

        //Then
        web.post().uri("/currency/convert")
                .body(BodyInserters.fromValue(request))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConversionResponse.class)
                .isEqualTo(response);
    }

    @Test
    public void convertCurrency_shouldReturnError_whenRequestValidationFails() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setFrom("USD");
        request.setTo("ASDF");
        request.setAmount(new BigDecimal("10"));
        GlobalExceptionHandler.ErrorResponse response = new GlobalExceptionHandler.ErrorResponse();
        response.setMessage("'to' field should be a 3 letter currency code");

        //Then
        web.post().uri("/currency/convert")
                .body(BodyInserters.fromValue(request))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(GlobalExceptionHandler.ErrorResponse.class)
                .isEqualTo(response);
    }

    @Test
    public void convertCurrency_shouldReturnError_whenConverterServiceThrowsInvalidRequestException() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setFrom("USD");
        request.setTo("ASD");
        request.setAmount(new BigDecimal("10"));
        GlobalExceptionHandler.ErrorResponse response = new GlobalExceptionHandler.ErrorResponse();
        response.setMessage("API call failed");

        when(converterService.convert(request)).thenThrow(new InvalidConversionRequestException("API call failed"));

        //Then
        web.post().uri("/currency/convert")
                .body(BodyInserters.fromValue(request))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(GlobalExceptionHandler.ErrorResponse.class)
                .isEqualTo(response);
    }

    @Test
    public void convertCurrency_shouldReturnError_whenConverterServiceThrowsCurrencyProviderException() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setFrom("USD");
        request.setTo("ASD");
        request.setAmount(new BigDecimal("10"));
        GlobalExceptionHandler.ErrorResponse response = new GlobalExceptionHandler.ErrorResponse();
        response.setMessage("Internal Server Error");

        when(converterService.convert(request)).thenThrow(new CurrencyProviderException("Internal Server Error"));

        //Then
        web.post().uri("/currency/convert")
                .body(BodyInserters.fromValue(request))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(GlobalExceptionHandler.ErrorResponse.class)
                .isEqualTo(response);
    }
}
