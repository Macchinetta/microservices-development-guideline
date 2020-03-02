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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * アカウント。
 * 
 * <pre>
 * パスワードとログイン日時はそれぞれ履歴で保持しています。
 * 現在のパスワード、最終ログイン日時を得るには下記のようにアクセスします。
 * <code>
 * Account a; // omitted instantiate code.
 * // Password.
 * Optional&lt;Password&gt; password = a.getPasswords().stream().findFirst();
 * // Last logged in date time.
 * Optional&lt;LastLogin&gt; lastDateTime = a.getLastLogins().stream().findFirst();
 * </code>
 * </pre>
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
public class Account implements Serializable {

    private static final long serialVersionUID = -9004798464089626980L;

    /**
     * メンバーId。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_member_id_seq")
    @SequenceGenerator(name = "account_member_id_seq", sequenceName = "account_member_id_seq")
    private Long memberId;

    /**
     * ログインステータス。
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "memberId")
    private LoginStatus loginStatus;

    /**
     * パスワード履歴。
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("createdAt desc")
    private final Set<Password> passwords = new LinkedHashSet<>();

    /**
     * ログイン履歴。
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("loggedInAt desc")
    private final Set<LastLogin> lastLogins = new LinkedHashSet<>();

    /**
     * 権限情報。
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private final Set<Authorities> authorities = new HashSet<>();

    /**
     * ログインステータスを取得します。
     * 
     * @return ログインステータス。
     */
    public Optional<LoginStatus> getLoginStatus() {
        return Optional.ofNullable(loginStatus);
    }
}
