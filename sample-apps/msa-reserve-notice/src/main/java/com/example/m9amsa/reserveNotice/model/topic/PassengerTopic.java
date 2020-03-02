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
package com.example.m9amsa.reserveNotice.model.topic;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 搭乗者情報モデル。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PassengerTopic implements Serializable {

    private static final long serialVersionUID = -4280988791467618633L;

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
     * 代表搭乗者フラグ。
     */
    private boolean isMainPassenger;

    /**
     * 電話番号。
     */
    private String telephoneNo;

    /**
     * e-mailアドレス。
     */
    private String email;
}