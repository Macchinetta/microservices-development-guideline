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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.example.m9amsa.reserve.constant.SeatClass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 予約要求情報。
 *
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ReservationRequest implements Serializable {

    private static final long serialVersionUID = -6317616008442925168L;

    /**
     * 便名。
     */
    @NotNull
    private String flightId;

    /**
     * 出発日。
     */
    @NotNull
    private LocalDate departureDate;

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
     * 搭乗クラス。
     */
    @NotNull
    private SeatClass seatClass;

    /**
     * 運賃種別名。
     */
    @NotNull
    private String fareType;

    /**
     * 運賃種別名。
     */
    @NotNull
    private Integer fare;

    /**
     * 搭乗者情報。
     */
    @NotNull
    private List<PassengerInfoModel> passengers;

    /**
     * 会員情報
     */
    @NotNull
    private Optional<MemberInfo> memberInfo;
}
