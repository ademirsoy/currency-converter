package com.itembase.currencyconverter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class ExchangeRateApiResponse implements CurrencyApiResponse {

    private String date;
    private Long time_last_updated;
    private Map<String, BigDecimal> rates;
    @JsonProperty("error-type")
    private String error;
    private String result;

    @Override
    public Map<String, BigDecimal> getRates() {
        return this.rates;
    }

    @Override
    public String getError() {
        return this.error;
    }

    @Override
    public Boolean isSuccess() {
        return !"error".equals(result);
    }
}
