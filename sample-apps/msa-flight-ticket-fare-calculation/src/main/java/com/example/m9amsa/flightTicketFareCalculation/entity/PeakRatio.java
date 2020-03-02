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
package com.example.m9amsa.flightTicketFareCalculation.entity;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * ピーク時期積算比率。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@IdClass(PeakRatioPk.class)
public class PeakRatio implements Serializable {

    private static final long serialVersionUID = -7617738396415882312L;

    /**
     * 開始日。
     */
    @Id
    private LocalDate fromDate;

    /**
     * 終了日。
     */
    @Id
    private LocalDate toDate;

    /**
     * 積算比率。
     * 
     * <pre>
     * 整数で指定します。
     * 下2桁は小数部として判断します。
     * 120  ➝ x 1.2
     * 1    ➝ '001' ➝ x 0.01
     * 1300 ➝  x 13.00
     * </pre>
     */
    private Integer ratio;
}
