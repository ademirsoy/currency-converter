package com.itembase.currencyconverter.service;

import com.itembase.currencyconverter.exception.InvalidConversionRequestException;
import com.itembase.currencyconverter.model.ConversionRequest;
import org.springframework.stereotype.Service;

@Service
public class ValidatorService {

    public void validateConversionRequest(ConversionRequest request) {
        if (request.getFrom() == null) {
            throw new InvalidConversionRequestException("'from' field is mandatory!");
        } else if (request.getFrom().length() != 3) {
            throw new InvalidConversionRequestException("'from' field should be a 3 letter currency code");
        } else if (request.getTo() == null) {
            throw new InvalidConversionRequestException("'to' field is mandatory!");
        } else if (request.getTo().length() != 3) {
            throw new InvalidConversionRequestException("'to' field should be a 3 letter currency code");
        } else if (request.getAmount() == null) {
            throw new InvalidConversionRequestException("'amount' field is mandatory!");
        }
    }
}
