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
import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.example.m9amsa.account.constant.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 入会サービスに送信する会員情報。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class AdmissionMemberInfo implements Serializable {

    private static final long serialVersionUID = 3263763966073068270L;

    /**
     * 会員の姓 漢字。
     */
    @NotNull
    private String surname;

    /**
     * 会員の名 漢字。
     */
    @NotNull
    private String firstName;

    /**
     * 会員の姓 カタカナ。
     */
    @NotNull
    private String surnameKana;

    /**
     * 会員の名 カタカナ。
     */
    @NotNull
    private String firstNameKana;

    /**
     * 誕生日。
     */
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    /**
     * 性別。
     */
    @NotNull
    private Gender gender;

    /**
     * 電話番号。
     */
    @NotNull
    private String telephoneNo;

    /**
     * 郵便番号。
     */
    @NotNull
    private String postalCode;

    /**
     * 住所。
     */
    @NotNull
    private String address;

    /**
     * e-mailアドレス。
     */
    @NotNull
    private String emailId;

    /**
     * カード情報。
     */
    @NotNull
    private CardInfo card;

    /**
     * パスワード。
     */
    @NotNull
    private String password;
}
