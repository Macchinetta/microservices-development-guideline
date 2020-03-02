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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.example.m9amsa.flight.entity.BasicFare;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class BasicFareInfo {

    /**
     * 出発空港Id。
     */
    @Size(min = 3, max = 3)
    private String departure;

    /**
     * 到着空港Id。
     */
    @Size(min = 3, max = 3)
    private String arrival;

    /**
     * 運賃。
     */
    @NotNull
    private Integer fare;

    /**
     * BasicFareエンティティへ変換します。
     * 
     * @return {@link BasicFare}
     */
    public BasicFare asEntity() {
        return BasicFare.builder().departure(departure).arrival(arrival).fare(fare).build();
    }
}
