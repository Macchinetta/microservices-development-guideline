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
import java.time.Clock;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import com.example.m9amsa.account.util.BeanUtil;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * パスワード情報。
 * 
 * <pre>
 * アカウントのパスワード情報。履歴で保持します。
 * パスワード作成日時が一番新しいレコードをアカウントの「パスワード」として扱います。
 * </pre>
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
public class Password implements Serializable {

    private static final long serialVersionUID = -2556276615540541468L;

    /**
     * パスワードId。
     */
    @Id
    @GeneratedValue
    private Long passwordId;

    /**
     * パスワード。
     */
    private String password;

    /**
     * パスワード作成日時。
     */
    private LocalDateTime createdAt;

    /**
     * 生成前の日時更新処理。
     */
    @PrePersist
    public void onPrePersist() {
        setCreatedAt(LocalDateTime.now(getClock()));
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
