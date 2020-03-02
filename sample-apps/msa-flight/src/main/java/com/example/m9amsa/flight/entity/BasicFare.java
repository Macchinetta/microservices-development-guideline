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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 区間運賃情報。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@IdClass(BasicFarePk.class)
public class BasicFare implements Serializable {

    private static final long serialVersionUID = 8572288007887919322L;

    /**
     * 出発空港Id。
     */
    @Id
    @Size(min = 3, max = 3)
    private String departure;

    /**
     * 到着空港Id。
     */
    @Id
    @Size(min = 3, max = 3)
    private String arrival;

    /**
     * 運賃。
     */
    @NotNull
    private Integer fare;
}
