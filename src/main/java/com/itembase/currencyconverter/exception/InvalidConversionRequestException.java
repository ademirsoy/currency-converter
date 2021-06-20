package com.itembase.currencyconverter.exception;


public class InvalidConversionRequestException extends RuntimeException {
    public InvalidConversionRequestException(String message) {
        super(message);
    }
}
