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
package com.example.m9amsa.reserve.model;

import java.io.Serializable;
import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.example.m9amsa.reserve.constant.FlightType;
import com.example.m9amsa.reserve.constant.SeatClass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 空席照会条件。
 *
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class VacantSeatQueryCondition implements Serializable {

    private static final long serialVersionUID = -1083507891987252271L;

    /**
     * フライト種別。
     */
    @NotNull
    private FlightType flightType;

    /**
     * 出発空港Id。
     */
    @NotNull
    private String departureAirportId;

    /**
     * 到着空港Id。
     */
    @NotNull
    private String arrivalAirportId;

    /**
     * 出発日。
     */
    @NotNull
    private LocalDate departureDate;

    /**
     * 搭乗クラス。
     */
    @NotNull
    private SeatClass seatClass;
}
