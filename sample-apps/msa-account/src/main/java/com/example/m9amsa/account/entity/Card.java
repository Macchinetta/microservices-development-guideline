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
package com.example.m9amsa.account.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * カード情報。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@IdClass(CardId.class)
public class Card implements Serializable {

    private static final long serialVersionUID = 8760632366428073245L;

    /**
     * カード番号。
     */
    @Id
    @NotNull
    private String cardNo;

    /**
     * カード会社番号。
     */
    @NotNull
    private String cardCompanyCode;

    /**
     * カード会社。
     */
    @NotNull
    private String cardCompanyName;

    /**
     * カード有効期限の月。
     */
    @Id
    @NotNull
    private String validTillMonth;

    /**
     * カード有効期限の年。
     */
    @Id
    @NotNull
    private String validTillYear;

}
