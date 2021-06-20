package com.itembase.currencyconverter.service;

import com.itembase.currencyconverter.exception.InvalidConversionRequestException;
import com.itembase.currencyconverter.model.ConversionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ValidatorServiceTest {

    @InjectMocks
    ValidatorService validatorService;

    @Test
    void validateConversionRequest_shouldValidateFromField() {

        //Given
        ConversionRequest request = new ConversionRequest();
        request.setTo("USD");
        request.setAmount(new BigDecimal("10"));

        //When
        Exception exception = assertThrows(InvalidConversionRequestException.class, () ->
                validatorService.validateConversionRequest(request));
        assertEquals("'from' field is mandatory!", exception.getMessage());
    }

    @Test
    void validateConversionRequest_shouldValidateToField() {

        //Given
        ConversionRequest request = new ConversionRequest();
        request.setFrom("USD");
        request.setAmount(new BigDecimal("10"));

        //When
        Exception exception = assertThrows(InvalidConversionRequestException.class, () ->
                validatorService.validateConversionRequest(request));
        assertEquals("'to' field is mandatory!", exception.getMessage());
    }

    @Test
    void validateConversionRequest_shouldValidateAmountField() {

        //Given
        ConversionRequest request = new ConversionRequest();
        request.setFrom("USD");
        request.setTo("EUR");

        //When
        Exception exception = assertThrows(InvalidConversionRequestException.class, () ->
                validatorService.validateConversionRequest(request));
        assertEquals("'amount' field is mandatory!", exception.getMessage());
    }
}
