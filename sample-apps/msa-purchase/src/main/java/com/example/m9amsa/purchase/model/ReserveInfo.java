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
package com.example.m9amsa.purchase.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.example.m9amsa.purchase.constant.SeatClass;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 予約情報。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ReserveInfo {

    /**
     * 予約Id。
     */
    @NotNull
    private Long reserveId;

    /**
     * 出発日。
     */
    @NotNull
    private LocalDate departureDate;

    /**
     * 便Id。
     */
    @NotNull
    private String flightId;

    /**
     * 出発時刻。
     */
    @NotNull
    private LocalTime departureTime;

    /**
     * 到着時刻。
     */
    @NotNull
    private LocalTime arrivalTime;

    /**
     * 出発空港ID。
     */
    @NotNull
    private String departureAirportId;

    /**
     * 到着空港ID。
     */
    @NotNull
    private String arrivalAirportId;

    /**
     * 搭乗クラス種別。
     */
    @NotNull
    private SeatClass seatClass;

    /**
     * 運賃種別。
     */
    @NotNull
    private String fareType;

    /**
     * 運賃。
     */
    @NotNull
    private Integer fare;

    /**
     * 搭乗者。
     */
    @NotNull
    private List<PassengerInfo> passengers;

    /**
     * 購入者会員。
     */
    @Builder.Default
    private Optional<MemberInfo> purchaseMember = Optional.empty();

}
