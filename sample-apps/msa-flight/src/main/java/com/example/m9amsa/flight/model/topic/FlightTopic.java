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
package com.example.m9amsa.flight.model.topic;

import java.io.Serializable;
import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * {@code flight-topic.FlightTopic}に対応するクラス。
 *
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class FlightTopic implements Serializable {

    private static final long serialVersionUID = -7820024020331551725L;

    /**
     * 便名。
     */
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
     * 機体Id。
     */
    private Long airplaneId;
}
