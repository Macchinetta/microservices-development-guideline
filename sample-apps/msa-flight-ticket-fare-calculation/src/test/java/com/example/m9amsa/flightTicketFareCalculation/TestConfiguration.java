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
package com.example.m9amsa.flightTicketFareCalculation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.example.m9amsa.flightTicketFareCalculation.entity.DiscountRepository;
import com.example.m9amsa.flightTicketFareCalculation.entity.PeakRatioRepository;
import com.example.m9amsa.flightTicketFareCalculation.entity.SeatClassChargeRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class TestConfiguration {

    @Primary
    @Bean(name = "discountRepositorySpy")
    public DiscountRepository discountRepositorySpy(final DiscountRepository real) {
        log.info("DiscountRepository   : {}", real);
        log.info("DiscountRepository   : {}", real.getClass().getName());

        DiscountRepositorySpy spy = new DiscountRepositorySpy(real);

        log.info("DiscountRepositorySpy: {}", spy);
        log.info("DiscountRepositorySpy: {}", spy.getClass().getName());

        return spy;
    }

    @Primary
    @Bean(name = "peakRatioRepositorySpy")
    public PeakRatioRepository peakRatioRepositorySpy(final PeakRatioRepository real) {
        log.info("PeakRatioRepository   : {}", real);
        log.info("PeakRatioRepository   : {}", real.getClass().getName());

        PeakRatioRepositorySpy spy = new PeakRatioRepositorySpy(real);

        log.info("PeakRatioRepositorySpy: {}", spy);
        log.info("PeakRatioRepositorySpy: {}", spy.getClass().getName());

        return spy;
    }

    @Primary
    @Bean(name = "seatClassChargeRepositorySpy")
    public SeatClassChargeRepository seatClassChargeRepositorySpy(final SeatClassChargeRepository real) {
        log.info("SeatClassChargeRepository   : {}", real);
        log.info("SeatClassChargeRepository   : {}", real.getClass().getName());

        SeatClassChargeRepositorySpy spy = new SeatClassChargeRepositorySpy(real);

        log.info("SeatClassChargeRepositorySpy: {}", spy);
        log.info("SeatClassChargeRepositorySpy: {}", spy.getClass().getName());

        return spy;
    }

}
