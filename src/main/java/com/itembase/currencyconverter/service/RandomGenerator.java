package com.itembase.currencyconverter.service;

import org.springframework.stereotype.Service;

@Service
public class RandomGenerator {

    public boolean randomBoolean() {
        return Math.random() >= 0.5;
    }
}
