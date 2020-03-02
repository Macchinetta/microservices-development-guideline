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
package com.example.m9amsa.flight.entity;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 空席情報。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@IdClass(VacantSeatPk.class)
public class VacantSeat implements Serializable {

    private static final long serialVersionUID = 9194675400159533695L;

    /**
     * 出発日。
     */
    @Id
    private LocalDate departureDate;

    /**
     * 便名。
     */
    @Id
    private String flightName;

    /**
     * 一般座席数（空席）。
     */
    private Integer vacantStandardSeatCount;

    /**
     * 特別座席数（空席）。
     */
    private Integer vacantSpecialSeatCount;

    /**
     * 一般席数の空席確保を行います。
     * 
     * @param seatCount 確保席数。
     */
    public void reserveStandardSeat(Integer seatCount) {
        vacantStandardSeatCount -= seatCount;
    }

    /**
     * 一般席数の空席を戻します。
     * 
     * @param seatCount 確保席数。
     */
    public void cancelStandardSeat(Integer seatCount) {
        vacantStandardSeatCount += seatCount;
    }

    /**
     * 特別席数の空席確保を行います。
     * 
     * @param seatCount 確保席数。
     */
    public void reserveSpecialSeat(Integer seatCount) {
        vacantSpecialSeatCount -= seatCount;
    }

    /**
     * 特別席数の空席を戻します。
     * 
     * @param seatCount 確保席数。
     */
    public void cancelSpecialSeat(Integer seatCount) {
        vacantSpecialSeatCount += seatCount;
    }
}
