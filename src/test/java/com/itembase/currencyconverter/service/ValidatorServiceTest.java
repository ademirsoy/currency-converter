package com.itembase.currencyconverter.service;

import com.itembase.currencyconverter.exception.InvalidConversionRequestException;
import com.itembase.currencyconverter.model.ConversionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ValidatorServiceTest {

    @InjectMocks
    ValidatorService validatorService;

    @Test
    void validateConversionRequest_shouldValidateSuccessfulRequest() {

        //Given
        ConversionRequest request = new ConversionRequest();
        request.setFrom("EUR");
        request.setTo("USD");
        request.setAmount(new BigDecimal("10"));

        //When
        assertDoesNotThrow(() -> validatorService.validateConversionRequest(request));
    }

    @Test
    void validateConversionRequest_shouldValidateFromField() {

        //Given
        ConversionRequest request = new ConversionRequest();
        request.setTo("USD");
        request.setAmount(new BigDecimal("10"));

        //When
        Exception exception = assertThrows(InvalidConversionRequestException.class, () ->
                validatorService.validateConversionRequest(request));
        //Then
        assertEquals("'from' field is mandatory!", exception.getMessage());
    }

    @Test
    void validateConversionRequest_shouldValidateFromFieldLength() {

        //Given
        ConversionRequest request = new ConversionRequest();
        request.setFrom("ABCD");
        request.setTo("USD");
        request.setAmount(new BigDecimal("10"));

        //When
        Exception exception = assertThrows(InvalidConversionRequestException.class, () ->
                validatorService.validateConversionRequest(request));
        //Then
        assertEquals("'from' field should be a 3 letter currency code", exception.getMessage());
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
        //Then
        assertEquals("'to' field is mandatory!", exception.getMessage());
    }

    @Test
    void validateConversionRequest_shouldValidateToFieldLength() {

        //Given
        ConversionRequest request = new ConversionRequest();
        request.setFrom("USD");
        request.setTo("AB");
        request.setAmount(new BigDecimal("10"));

        //When
        Exception exception = assertThrows(InvalidConversionRequestException.class, () ->
                validatorService.validateConversionRequest(request));
        //Then
        assertEquals("'to' field should be a 3 letter currency code", exception.getMessage());
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
        //Then
        assertEquals("'amount' field is mandatory!", exception.getMessage());
    }
}
