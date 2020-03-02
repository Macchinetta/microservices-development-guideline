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
package com.example.m9amsa.account.model;

import java.io.Serializable;

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
public class CardInfo implements Serializable {

    private static final long serialVersionUID = -888495553191522895L;

    /**
     * カード番号。
     */
    private String cardNo;

    /**
     * カード会社コード
     */
    private String cardCompanyCode;

    /**
     * カード会社。
     */
    private String cardCompanyName;

    /**
     * カード有効期限の月。
     */
    private Integer validTillMonth;

    /**
     * カード有効期限の年。
     */
    private Integer validTillYear;
}
