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
package com.example.m9amsa.flightTicketFareCalculation.model;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * フライト運賃。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class FlightFareInfo implements Serializable {

    private static final long serialVersionUID = -8696227802994936107L;

    /**
     * 割引Id。
     */
    private String discountId;

    /**
     * 割引名称。
     */
    private String name;

    /**
     * 割引説明。
     */
    private String description;

    /**
     * 運賃。
     */
    private Integer fare;

}
