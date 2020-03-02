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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 機体。
 *
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class AirplaneTopic implements Serializable {

    private static final long serialVersionUID = 3592342704906593274L;

    /**
     * 機体Id。
     */
    private Long id;

    /**
     * 機体名。
     */
    private String name;

    /**
     * 一般座席数。
     */
    private Integer standardSeats;

    /**
     * 特別座席数。
     */
    private Integer specialSeats;
}
