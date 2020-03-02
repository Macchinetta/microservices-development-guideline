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
package com.example.m9amsa.purchase.entity;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import com.example.m9amsa.purchase.util.BeanUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 決済情報。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
public class Payment implements Serializable {

    private static final long serialVersionUID = -3423250387902961043L;

    /**
     * 決済Id。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long paymentId;

    /**
     * 購入情報。
     */
    @JsonIgnore
    @OneToOne
    @JoinColumn
    private Purchase purchase;

    /**
     * 決済日時。
     */
    private LocalDateTime payDateTime;

    /**
     * 運賃。
     */
    private Integer fare;

    /**
     * 会員の姓 漢字。
     */
    private String surname;

    /**
     * 会員の名 漢字。
     */
    private String firstName;

    /**
     * 会員の姓 カタカナ。
     */
    private String surnameKana;

    /**
     * 会員の名 カタカナ。
     */
    private String firstNameKana;

    /**
     * e-mailアドレス。
     */
    private String emailId;

    /**
     * カード番号。
     */
    private String cardNo;

    /**
     * カード会社番号。
     */
    private String cardCompanyCode;

    /**
     * カード会社。
     */
    private String cardCompanyName;

    /**
     * カード有効期限の月。
     */
    private String validTillMonth;

    /**
     * カード有効期限の年。
     */
    private String validTillYear;

    /**
     * 生成前の日時更新処理。
     */
    @PrePersist
    public void onPrePersist() {
        setPayDateTime(LocalDateTime.now(getClock()));
    }

    /**
     * 現在日付取得用の基準Clock。
     * 
     * @return 現在日付取得用の基準Clock。
     */
    public Clock getClock() {
        BaseClock baseClock = BeanUtil.getBean(BaseClock.class);
        return baseClock.systemDefaultZone();
    }
}
