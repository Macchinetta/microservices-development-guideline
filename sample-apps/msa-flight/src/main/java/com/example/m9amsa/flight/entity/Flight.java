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
import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * フライト情報。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
public class Flight implements Serializable {

    private static final long serialVersionUID = 1450929853921650995L;

    /**
     * 便名。
     */
    @Id
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
     * 機体情報。
     */
    @NotNull
    @OneToOne(cascade = CascadeType.REFRESH)
    @JoinColumn
    private Airplane airplane;

    /**
     * 空港情報。
     */
    @NotNull
    @OneToOne(cascade = CascadeType.REFRESH)
    @JoinColumn
    private Airport departureAirport;

    /**
     * 到着空港。
     */
    @NotNull
    @OneToOne(cascade = CascadeType.REFRESH)
    @JoinColumn
    private Airport arrivalAirport;

    /**
     * 区間運賃情報。
     */
    @NotNull
    @OneToOne(cascade = CascadeType.REFRESH)
    @JoinColumns({ @JoinColumn, @JoinColumn })
    private BasicFare basicFare;
}
