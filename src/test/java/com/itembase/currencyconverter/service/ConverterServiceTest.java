package com.itembase.currencyconverter.service;

import com.itembase.currencyconverter.exception.CurrencyProviderException;
import com.itembase.currencyconverter.exception.InvalidConversionRequestException;
import com.itembase.currencyconverter.model.ConversionRequest;
import com.itembase.currencyconverter.model.ConversionResponse;
import com.itembase.currencyconverter.model.CurrencyLayerApiResponse;
import com.itembase.currencyconverter.model.ExchangeRateApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConverterServiceTest {

    @InjectMocks
    ConverterService converterService;

    @Mock
    ExchangeRateApiClient exchangeRateApiClient;

    @Mock
    CurrencyLayerApiClient currencyLayerApiClient;

    @Mock
    RandomGenerator randomGenerator;

    @Test
    void convert_shouldReturnConvertedAmount_whenBothExternalApisAvailable() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setAmount(new BigDecimal("10"));
        request.setFrom("EUR");
        request.setTo("USD");

        ExchangeRateApiResponse exchangeRateApiResponse = new ExchangeRateApiResponse();
        Map<String, BigDecimal> rates = Map.of("USD", new BigDecimal("1.2"));
        exchangeRateApiResponse.setRates(rates);
        when(exchangeRateApiClient.getRates("EUR")).thenReturn(Mono.just(exchangeRateApiResponse));

        CurrencyLayerApiResponse currencyLayerApiResponse = new CurrencyLayerApiResponse();
        Map<String, BigDecimal> quotes = Map.of("EURUSD", new BigDecimal("1.2"));
        currencyLayerApiResponse.setQuotes(quotes);
        lenient().when(currencyLayerApiClient.getRates()).thenReturn(Mono.just(currencyLayerApiResponse));

        when(randomGenerator.randomBoolean()).thenReturn(Math.random() >= 0.5);

        //When
        Mono<ConversionResponse> actualMono = converterService.convert(request);

        //Then
        ConversionResponse actual = actualMono.block();
        assertThat(actual).isNotNull();
        assertThat(actual.getConverted()).isEqualTo(new BigDecimal("12.00"));
    }

    @Test
    void convert_shouldReturnConvertedAmount_whenCurrencyLayerApiDoesNotSupportRequestedCurrency() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setAmount(new BigDecimal("10"));
        request.setFrom("USD");
        request.setTo("TRY");

        ExchangeRateApiResponse exchangeRateApiResponse = new ExchangeRateApiResponse();
        Map<String, BigDecimal> rates = Map.of("TRY", new BigDecimal("8.74"));
        exchangeRateApiResponse.setRates(rates);
        when(exchangeRateApiClient.getRates("USD")).thenReturn(Mono.just(exchangeRateApiResponse));

        CurrencyLayerApiResponse currencyLayerApiResponse = new CurrencyLayerApiResponse();
        currencyLayerApiResponse.setSuccess(true);
        Map<String, BigDecimal> quotes = Map.of("EURUSD", new BigDecimal("1.2"));
        currencyLayerApiResponse.setQuotes(quotes);
        when(currencyLayerApiClient.getRates()).thenReturn(Mono.just(currencyLayerApiResponse));

        when(randomGenerator.randomBoolean()).thenReturn(false);

        //When
        Mono<ConversionResponse> actualMono = converterService.convert(request);

        //Then
        ConversionResponse actual = actualMono.block();
        assertThat(actual).isNotNull();
        assertThat(actual.getConverted()).isEqualTo(new BigDecimal("87.40"));
    }

    @Test
    void convert_shouldReturnConvertedAmount_whenCurrencyLayerApiNotAvailable() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setAmount(new BigDecimal("10"));
        request.setFrom("USD");
        request.setTo("TRY");

        ExchangeRateApiResponse exchangeRateApiResponse = new ExchangeRateApiResponse();
        Map<String, BigDecimal> rates = Map.of("TRY", new BigDecimal("8.74"));
        exchangeRateApiResponse.setRates(rates);
        when(exchangeRateApiClient.getRates("USD")).thenReturn(Mono.just(exchangeRateApiResponse));
        when(currencyLayerApiClient.getRates()).thenReturn(Mono.error(new CurrencyProviderException("Internal Server Error")));
        when(randomGenerator.randomBoolean()).thenReturn(false);

        //When
        Mono<ConversionResponse> actualMono = converterService.convert(request);

        //Then
        ConversionResponse actual = actualMono.block();
        assertThat(actual).isNotNull();
        assertThat(actual.getConverted()).isEqualTo(new BigDecimal("87.40"));
    }

    @Test
    void convert_shouldReturnConvertedAmount_whenCurrencyLayerApiReturnsError() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setAmount(new BigDecimal("10"));
        request.setFrom("USD");
        request.setTo("TRY");

        ExchangeRateApiResponse exchangeRateApiResponse = new ExchangeRateApiResponse();
        Map<String, BigDecimal> rates = Map.of("TRY", new BigDecimal("8.74"));
        exchangeRateApiResponse.setRates(rates);
        when(exchangeRateApiClient.getRates("USD")).thenReturn(Mono.just(exchangeRateApiResponse));

        CurrencyLayerApiResponse currencyLayerApiResponse = new CurrencyLayerApiResponse();
        currencyLayerApiResponse.setSuccess(false);
        CurrencyLayerApiResponse.ApiError error = new CurrencyLayerApiResponse.ApiError();
        error.setInfo("Access token not valid");
        currencyLayerApiResponse.setError(error);
        when(currencyLayerApiClient.getRates()).thenReturn(Mono.just(currencyLayerApiResponse));

        when(randomGenerator.randomBoolean()).thenReturn(false);

        //When
        Mono<ConversionResponse> actualMono = converterService.convert(request);

        //Then
        ConversionResponse actual = actualMono.block();
        assertThat(actual).isNotNull();
        assertThat(actual.getConverted()).isEqualTo(new BigDecimal("87.40"));
    }

    @Test
    void convert_shouldReturnConvertedAmount_whenExchangeRateApiDoesNotSupportRequestedCurrency() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setAmount(new BigDecimal("10"));
        request.setFrom("EUR");
        request.setTo("TRY");

        ExchangeRateApiResponse exchangeRateApiResponse = new ExchangeRateApiResponse();
        Map<String, BigDecimal> rates = Map.of("USD", new BigDecimal("1.2"));
        exchangeRateApiResponse.setRates(rates);
        when(exchangeRateApiClient.getRates("EUR")).thenReturn(Mono.just(exchangeRateApiResponse));

        CurrencyLayerApiResponse currencyLayerApiResponse = new CurrencyLayerApiResponse();
        currencyLayerApiResponse.setSuccess(true);
        Map<String, BigDecimal> quotes = Map.of("EURTRY", new BigDecimal("10.35"));
        currencyLayerApiResponse.setQuotes(quotes);
        when(currencyLayerApiClient.getRates()).thenReturn(Mono.just(currencyLayerApiResponse));

        when(randomGenerator.randomBoolean()).thenReturn(true);

        //When
        Mono<ConversionResponse> actualMono = converterService.convert(request);

        //Then
        ConversionResponse actual = actualMono.block();
        assertThat(actual).isNotNull();
        assertThat(actual.getConverted()).isEqualTo(new BigDecimal("103.50"));
    }

    @Test
    void convert_shouldReturnConvertedAmount_whenExchangeRateApiNotAvailable() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setAmount(new BigDecimal("10"));
        request.setFrom("USD");
        request.setTo("TRY");

        when(exchangeRateApiClient.getRates("USD")).thenReturn(Mono.error(new CurrencyProviderException("Internal Server Error")));

        CurrencyLayerApiResponse currencyLayerApiResponse = new CurrencyLayerApiResponse();
        currencyLayerApiResponse.setSuccess(true);
        Map<String, BigDecimal> quotes = Map.of("USDTRY", new BigDecimal("8.74"));
        currencyLayerApiResponse.setQuotes(quotes);
        when(currencyLayerApiClient.getRates()).thenReturn(Mono.just(currencyLayerApiResponse));
        when(randomGenerator.randomBoolean()).thenReturn(true);

        //When
        Mono<ConversionResponse> actualMono = converterService.convert(request);

        //Then
        ConversionResponse actual = actualMono.block();
        assertThat(actual).isNotNull();
        assertThat(actual.getConverted()).isEqualTo(new BigDecimal("87.40"));
    }

    @Test
    void convert_shouldThrowException_whenBothApisDoNotSupportRequestedCurrency() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setAmount(new BigDecimal("10"));
        request.setFrom("USD");
        request.setTo("TRY");

        ExchangeRateApiResponse exchangeRateApiResponse = new ExchangeRateApiResponse();
        Map<String, BigDecimal> rates = Map.of("EUR", new BigDecimal("0.85"));
        exchangeRateApiResponse.setRates(rates);
        when(exchangeRateApiClient.getRates("USD")).thenReturn(Mono.just(exchangeRateApiResponse));

        CurrencyLayerApiResponse currencyLayerApiResponse = new CurrencyLayerApiResponse();
        currencyLayerApiResponse.setSuccess(true);
        Map<String, BigDecimal> quotes = Map.of("EURUSD", new BigDecimal("1.2"));
        currencyLayerApiResponse.setQuotes(quotes);
        when(currencyLayerApiClient.getRates()).thenReturn(Mono.just(currencyLayerApiResponse));

        when(randomGenerator.randomBoolean()).thenReturn(false);

        //When/Then
        StepVerifier.create(this.converterService.convert(request))
                .expectErrorMatches(throwable -> throwable instanceof InvalidConversionRequestException &&
                        throwable.getMessage().equals("Requested currency not available. From: USD To: TRY"))
                .verify();
    }

    @Test
    void convert_shouldThrowException_whenBothApisAreNotAvailable() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setAmount(new BigDecimal("10"));
        request.setFrom("USD");
        request.setTo("TRY");

        when(exchangeRateApiClient.getRates("USD")).thenReturn(Mono.error(new CurrencyProviderException("Internal Server Error")));

        CurrencyLayerApiResponse currencyLayerApiResponse = new CurrencyLayerApiResponse();
        currencyLayerApiResponse.setSuccess(false);
        CurrencyLayerApiResponse.ApiError error = new CurrencyLayerApiResponse.ApiError();
        error.setInfo("Access token not valid");
        currencyLayerApiResponse.setError(error);
        when(currencyLayerApiClient.getRates()).thenReturn(Mono.just(currencyLayerApiResponse));

        when(randomGenerator.randomBoolean()).thenReturn(true);

        //When/Then
        StepVerifier.create(this.converterService.convert(request))
                .expectErrorMatches(throwable -> throwable instanceof CurrencyProviderException &&
                        throwable.getMessage().equals("Access token not valid"))
                .verify();
    }

    @Test
    void convert_shouldThrowException_whenBothApisAreNotAvailable_reverseOrder() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setAmount(new BigDecimal("10"));
        request.setFrom("USD");
        request.setTo("TRY");

        when(exchangeRateApiClient.getRates("USD")).thenReturn(Mono.error(new CurrencyProviderException("Internal Server Error")));

        CurrencyLayerApiResponse currencyLayerApiResponse = new CurrencyLayerApiResponse();
        currencyLayerApiResponse.setSuccess(false);
        CurrencyLayerApiResponse.ApiError error = new CurrencyLayerApiResponse.ApiError();
        error.setInfo("Access token not valid");
        currencyLayerApiResponse.setError(error);
        when(currencyLayerApiClient.getRates()).thenReturn(Mono.just(currencyLayerApiResponse));

        when(randomGenerator.randomBoolean()).thenReturn(false);

        //When/Then
        StepVerifier.create(this.converterService.convert(request))
                .expectErrorMatches(throwable -> throwable instanceof CurrencyProviderException &&
                        throwable.getMessage().equals("Internal Server Error"))
                .verify();
    }

    @Test
    void convert_shouldThrowException_whenBothApisReturnError() {
        //Given
        ConversionRequest request = new ConversionRequest();
        request.setAmount(new BigDecimal("10"));
        request.setFrom("USD");
        request.setTo("TRY");

        ExchangeRateApiResponse exchangeRateApiResponse = new ExchangeRateApiResponse();
        exchangeRateApiResponse.setResult("error");
        exchangeRateApiResponse.setError("Internal Server Error");
        when(exchangeRateApiClient.getRates("USD")).thenReturn(Mono.just(exchangeRateApiResponse));

        CurrencyLayerApiResponse currencyLayerApiResponse = new CurrencyLayerApiResponse();
        currencyLayerApiResponse.setSuccess(false);
        CurrencyLayerApiResponse.ApiError error = new CurrencyLayerApiResponse.ApiError();
        error.setInfo("Access token not valid");
        currencyLayerApiResponse.setError(error);
        when(currencyLayerApiClient.getRates()).thenReturn(Mono.just(currencyLayerApiResponse));

        when(randomGenerator.randomBoolean()).thenReturn(false);

        //When/Then
        StepVerifier.create(this.converterService.convert(request))
                .expectErrorMatches(throwable -> throwable instanceof CurrencyProviderException &&
                        throwable.getMessage().equals("Internal Server Error"))
                .verify();
    }
}
