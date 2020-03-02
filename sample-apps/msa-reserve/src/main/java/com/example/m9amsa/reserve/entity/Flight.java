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
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * フライト情報。
 *
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
public class Flight implements Serializable {

    private static final long serialVersionUID = -6192102823712333960L;

    /**
     * 便名。
     */
    @Id
    private String name;

    /**
     * 出発空港Id。
     */
    private String departureAirportId;

    /**
     * 到着空港Id。
     */
    private String arrivalAirportId;

    /**
     * 出発時刻。
     */
    private LocalTime departureTime;

    /**
     * 到着時刻。
     */
    private LocalTime arrivalTime;

    /**
     * 機体名。
     */
    private String airplaneName;

    /**
     * 一般座席数。
     */
    private Integer standardSeats;

    /**
     * 特別座席数。
     */
    private Integer specialSeats;

    /**
     * 運賃。
     */
    private Integer fare;
}
