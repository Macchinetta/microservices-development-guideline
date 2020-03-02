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

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;

import com.example.m9amsa.flight.constant.SeatClass;
import com.example.m9amsa.flight.entity.ReserveVacantSeat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ReserveVacantSeatInfo {

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
     * 便名。
     */
    @NotNull
    private String flightName;

    /**
     * 搭乗クラス。
     */
    @NotNull
    private SeatClass seatClass;

    /**
     * 座席予約数。
     */
    @NotNull
    @Range(min = 1)
    private Integer vacantSeatCount;

    public ReserveVacantSeat asEntity() {
        return ReserveVacantSeat.builder().reserveId(reserveId).departureDate(departureDate).flightName(flightName)
                .seatClass(seatClass).vacantSeatCount(vacantSeatCount).build();
    }
}
