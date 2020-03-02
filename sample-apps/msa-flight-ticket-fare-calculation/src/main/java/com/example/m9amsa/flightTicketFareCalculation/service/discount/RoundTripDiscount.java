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

import org.springframework.stereotype.Component;

import com.example.m9amsa.flightTicketFareCalculation.entity.PeakRatio;
import com.example.m9amsa.flightTicketFareCalculation.entity.SeatClassCharge;
import com.example.m9amsa.flightTicketFareCalculation.model.FareCalcInfo;

/**
 * 往復運賃を計算します。
 * 
 */
@Component("roundTripDiscount")
public class RoundTripDiscount implements DiscountCalculator {

    /**
     * {@inheritDoc}
     * 
     * <pre>
     * 往復運賃では、搭乗クラス加算額とピーク時期積算比率から求める基本運賃から往復運賃の割引額を引いた金額を使用します。
     * </pre>
     */
    @Override
    public int calcFare(FareCalcInfo fareCalcInfo, SeatClassCharge seatClassCharge, PeakRatio peakRatio) {
        int basicFare = basicFare(fareCalcInfo.getBasicFare(), seatClassCharge.getCharge(), peakRatio.getRatio());
        // 基本運賃 - 割引額(基本運賃の5％)
        return (int) (basicFare - Math.floor(basicFare * 0.05));
    }

    /**
     * この計算ロジックの対象割引を取得します。
     * 
     * @return {@code DiscountType.ROUND_TRIP}。
     */
    @Override
    public DiscountType targetDiscountId() {
        return DiscountType.ROUND_TRIP;
    }
}
