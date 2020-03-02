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
package com.example.m9amsa.reserveNotice.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 搭乗者情報。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
public class Passenger implements Serializable {

    private static final long serialVersionUID = 2239444184189243639L;

    /**
     * 搭乗者情報のPK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long passengerInfoId;

    /**
     * 搭乗者Id。
     */
    private Long passengerId;

    /**
     * 搭乗者名。
     */
    private String name;

    /**
     * 年齢。
     */
    private Integer age;

    /**
     * 電話番号。
     */
    private String telephoneNo;

    /**
     * Eメールアドレス。
     */
    private String email;

    /**
     * 代表搭乗者フラグ。
     */
    private boolean isMainPassenger;

    /**
     * 予約情報。
     */
    @JsonIgnore
    @ManyToOne
    @JoinColumn
    Reservation reservation;
}
