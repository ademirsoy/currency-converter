package com.itembase.currencyconverter.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class CurrencyLayerApiResponse implements CurrencyApiResponse {

    private Boolean success;
    private Long timestamp;
    private Map<String, BigDecimal> quotes;
    private ApiError error;

    @Override
    public Map<String, BigDecimal> getRates() {
        return this.quotes;
    }

    @Override
    public String getError() {
        return this.error.getInfo();
    }

    @Override
    public Boolean isSuccess() {
        return this.success;
    }

    @Data
    public static class ApiError {
        private String info;
    }
}
