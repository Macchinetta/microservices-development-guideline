/*
 * Copyright(c) 2019 NTT Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.example.m9amsa.flightTicketFareCalculation.service.discount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.m9amsa.flightTicketFareCalculation.service.discount.DiscountCalculator.DiscountType;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link com.example.m9amsa.flightTicketFareCalculation.service.discount.DiscountCalculator
 * DiscountCalculator}のファクトリクラス。
 * 
 */
@Component
@Slf4j
public class DiscountCalculatorFactory {

    /**
     * アプリケーションコンテキスト
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * { @link com.example.flightTicketFareCalculation.entity.DiscountType
     * DiscountId} をもとに { @link
     * com.example.flightTicketFareCalculation.service.discount.DiscountCalculator
     * DiscountCalculator}を生成します。
     * 
     * @param discountId { @link
     *                   com.example.flightTicketFareCalculation.entity.DiscountType
     *                   DiscountId}。
     * @return discountIdに対応した{ @link
     *         com.example.flightTicketFareCalculation.service.discount.DiscountCalculator
     *         DiscountCalculator}。
     */
    public DiscountCalculator getCalculator(DiscountType discountId) {
        log.info("discountId: {}", discountId);
        DiscountCalculator calculator = applicationContext.getBean(discountId.toString(), DiscountCalculator.class);
        log.info("calculator: {}", calculator.getClass().getSimpleName());
        return calculator;
    }

}
