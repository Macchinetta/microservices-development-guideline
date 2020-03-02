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
package com.example.m9amsa.flight.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * フライト更新モデル。
 * 
 * <pre>
 * フライト情報の更新用モデルです。
 * 空港情報、機体情報はエンティティではなく、Idを指定します。
 * </pre>
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class FlightUpdateInfo implements Serializable {

    private static final long serialVersionUID = 7927500478226465565L;

    /**
     * 便名。
     */
    @NotNull
    private String name;

    /**
     * 出発時刻。
     */
    @NotNull
    private LocalDateTime departureTime;

    /**
     * 到着時刻。
     */
    @NotNull
    private LocalDateTime arrivalTime;

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
     * 機体Id。
     */
    @NotNull
    private Long airplaneId;
}
