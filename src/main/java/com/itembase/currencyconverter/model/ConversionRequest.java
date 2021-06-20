package com.itembase.currencyconverter.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConversionRequest {

    private String from;
    private String to;
    private BigDecimal amount;
}
