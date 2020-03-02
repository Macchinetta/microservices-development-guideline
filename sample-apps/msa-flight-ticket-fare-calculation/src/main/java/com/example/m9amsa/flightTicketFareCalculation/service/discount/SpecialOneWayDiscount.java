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
 * 特別片道運賃を計算します。
 * 
 */
@Component("specialOneWayDiscount")
public class SpecialOneWayDiscount implements DiscountCalculator {

    /**
     * {@inheritDoc}
     * 
     * <pre>
     * 特別片道運賃では、搭乗クラス加算額とピーク時期積算比率から求める基本運賃をそのまま使用します。
     * </pre>
     */
    @Override
    public int calcFare(FareCalcInfo fareCalcInfo, SeatClassCharge seatClassCharge, PeakRatio peakRatio) {
        return basicFare(fareCalcInfo.getBasicFare(), seatClassCharge.getCharge(), peakRatio.getRatio());
    }

    /**
     * この計算ロジックの対象割引を取得します。
     * 
     * @return {@code DiscountType.SPECIAL_ONE_WAY}。
     */
    @Override
    public DiscountType targetDiscountId() {
        return DiscountType.SPECIAL_ONE_WAY;
    }
}
