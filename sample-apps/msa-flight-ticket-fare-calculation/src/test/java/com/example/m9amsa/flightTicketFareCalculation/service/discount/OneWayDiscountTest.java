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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flightTicketFareCalculation.entity.PeakRatio;
import com.example.m9amsa.flightTicketFareCalculation.entity.SeatClassCharge;
import com.example.m9amsa.flightTicketFareCalculation.model.FareCalcInfo;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.DiscountCalculator.DiscountType;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_CALCULATE_FARE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=localhost" })
public class OneWayDiscountTest {

    @Autowired
    private DiscountCalculatorFactory discountCalculatorFactory;

    /**
     * Test for calcFare().
     */
    @Test
    public void testCalcFare() {
        DiscountCalculator discount = discountCalculatorFactory.getCalculator(DiscountType.ONE_WAY);

        FareCalcInfo input = FareCalcInfo.builder().basicFare(30000).build();
        SeatClassCharge seatClassCharge = SeatClassCharge.builder().charge(5000).build();
        PeakRatio peakRatio = PeakRatio.builder().ratio(140).build();

        int actFare = discount.calcFare(input, seatClassCharge, peakRatio);

        assertThat("割引価格が正しいこと", actFare, equalTo(49000));
    }

    /**
     * Test for targetDiscountId().
     */
    @Test
    public void testTargetDiscountId() {
        DiscountCalculator discount = discountCalculatorFactory.getCalculator(DiscountType.ONE_WAY);

        assertThat("割引タイプが正しいこと", discount.targetDiscountId(), equalTo(DiscountType.ONE_WAY));
    }

    /**
     * Test for isAvailableTravelDate().
     */
    @Test
    public void testIsAvailableTravelDate() {
        DiscountCalculator discount = discountCalculatorFactory.getCalculator(DiscountType.ONE_WAY);

        // 搭乗日当日
        LocalDate travelDate = LocalDate.now();

        Boolean actual = discount.isAvailableTravelDate(travelDate);

        assertTrue("搭乗日当日", actual);

        // 搭乗日前日
        travelDate = LocalDate.now().plusDays(1);

        actual = discount.isAvailableTravelDate(travelDate);

        assertTrue("搭乗日前日", actual);

        // 搭乗日89日前
        travelDate = LocalDate.now().plusDays(89);

        actual = discount.isAvailableTravelDate(travelDate);

        assertTrue("搭乗日89日前", actual);

        // 搭乗日90日前
        travelDate = LocalDate.now().plusDays(90);

        actual = discount.isAvailableTravelDate(travelDate);

        assertTrue("搭乗日90日前", actual);

        // 搭乗日91日前
        travelDate = LocalDate.now().plusDays(91);

        actual = discount.isAvailableTravelDate(travelDate);

        assertFalse("搭乗日91日前", actual);
    }

    /**
     * Test for isAvailableTotalPassengers().
     */
    @Test
    public void testIsAvailableTotalPassengers() {
        DiscountCalculator discount = discountCalculatorFactory.getCalculator(DiscountType.ONE_WAY);

        Boolean actual = discount.isAvailableTotalPassengers(0);

        assertFalse("搭乗人数：0", actual);

        actual = discount.isAvailableTotalPassengers(1);

        assertTrue("搭乗人数：1", actual);

        actual = discount.isAvailableTotalPassengers(2);

        assertTrue("搭乗人数：2", actual);
    }
}
