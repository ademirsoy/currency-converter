package com.itembase.currencyconverter.controller;

import com.itembase.currencyconverter.model.ConversionRequest;
import com.itembase.currencyconverter.model.ConversionResponse;
import com.itembase.currencyconverter.service.ConverterService;
import com.itembase.currencyconverter.service.ValidatorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/currency",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class CurrencyController {

    final ConverterService converterService;
    final ValidatorService validatorService;

    public CurrencyController(ConverterService converterService, ValidatorService validatorService) {
        this.converterService = converterService;
        this.validatorService = validatorService;
    }

    @PostMapping(path = "/convert")
    public Mono<ConversionResponse> convertCurrency(@RequestBody ConversionRequest conversionRequest) {
        this.validatorService.validateConversionRequest(conversionRequest);
        return this.converterService.convert(conversionRequest);
    }
}
