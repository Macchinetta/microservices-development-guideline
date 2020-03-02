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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * フライト空席情報。
 *
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@IdClass(FlightVacantSeatId.class)
public class FlightVacantSeat implements Serializable {

    private static final long serialVersionUID = -7319407616606927781L;

    /**
     * 搭乗日。
     */
    @Id
    private LocalDate departureDate;

    /**
     * 便名。
     */
    @Id
    private String flightName;

    /**
     * 一般座席空席数。
     */
    private Integer standardSeats;

    /**
     * 特別座席空席数。
     */
    private Integer specialSeats;

}
