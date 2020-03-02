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
package com.example.m9amsa.flightTicketFareCalculation.model;

import java.io.Serializable;
import java.time.LocalDate;

import com.example.m9amsa.flightTicketFareCalculation.constant.FlightType;
import com.example.m9amsa.flightTicketFareCalculation.constant.SeatClass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 運賃計算入力情報。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class FareCalcInfo implements Serializable {

    private static final long serialVersionUID = 1435635869400721141L;

    /**
     * 出発空港Id。
     */
    private String departureAirportId;

    /**
     * 到着空港Id。
     */
    private String arrivalAirportId;

    /**
     * 出発日。
     */
    private LocalDate travelDate;

    /**
     * 搭乗クラス。
     */
    private SeatClass seatClass;

    /**
     * 予約タイプ。
     */
    private FlightType flightType;

    /**
     * 搭乗人数。
     */
    private Integer totalPassengers;

    /**
     * 区間基本運賃。
     */
    private Integer basicFare;

    /**
     * 割引Id。
     */
    private String discountId;

}
