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
package com.example.m9amsa.reserve.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.example.m9amsa.reserve.constant.SeatClass;

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
@Entity
public class Reservation implements Serializable {

    private static final long serialVersionUID = -126777240861927855L;

    /**
     * 予約番号。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reserveId;

    /**
     * 予約時刻。
     */
    private LocalDateTime reserveTime;

    /**
     * 出発日。
     */
    private LocalDate departureDate;

    /**
     * 便名。
     */
    private String flightId;

    /**
     * 出発時刻。
     */
    private LocalTime departureTime;

    /**
     * 到着時刻。
     */
    private LocalTime arrivalTime;

    /**
     * 出発空港ID。
     */
    private String departureAirportId;

    /**
     * 到着空港ID。
     */
    private String arrivalAirportId;

    /**
     * 搭乗クラス種別。
     */
    @Enumerated(EnumType.STRING)
    private SeatClass seatClass;

    /**
     * 運賃種別。
     */
    private String fareType;

    /**
     * 運賃。
     */
    private Integer fare;

    /**
     * 代表搭乗者情報。
     */
    @OneToOne(cascade = CascadeType.ALL)
    private Passenger mainPassenger;

    /**
     * 同時搭乗者情報。
     */
    @OneToMany(cascade = CascadeType.ALL)
    private List<Passenger> subPassengers;
}
