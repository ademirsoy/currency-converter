package com.itembase.currencyconverter.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ConversionResponse extends ConversionRequest {

    private BigDecimal converted;

    public ConversionResponse(ConversionRequest conversionRequest) {
        this.setFrom(conversionRequest.getFrom());
        this.setTo(conversionRequest.getTo());
        this.setAmount(conversionRequest.getAmount());
    }
}
