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
package com.example.m9amsa.reserve.externalmicroservice.model;

import java.io.Serializable;
import java.time.LocalDate;

import com.example.m9amsa.reserve.constant.SeatClass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * フライト空席確保サービスのリクエストパラメータ。
 *
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ReserveVacantSeatForEx implements Serializable {

    private static final long serialVersionUID = 7784425924609248085L;

    /**
     * 予約Id。
     */
    private Long reserveId;

    /**
     * 出発日。
     */
    private LocalDate departureDate;

    /**
     * 便名。
     */
    private String flightName;

    /**
     * 搭乗クラス。
     */
    private SeatClass seatClass;

    /**
     * 座席予約数。
     */
    private Integer vacantSeatCount;
}
