package com.itembase.currencyconverter.exception;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CurrencyProviderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleCurrencyProviderException(CurrencyProviderException ex) {
        log.error("Currency conversion failed, " + ex.getMessage(), ex);
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(InvalidConversionRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidCurrencyException(InvalidConversionRequestException ex) {
        log.warn(ex.getMessage());
        return new ErrorResponse(ex.getMessage());
    }

    @Data
    public static class ErrorResponse {
        private final String message;
    }
}
