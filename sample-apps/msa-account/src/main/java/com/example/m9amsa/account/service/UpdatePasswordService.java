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
package com.example.m9amsa.account.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.account.entity.Account;
import com.example.m9amsa.account.entity.AccountRepository;
import com.example.m9amsa.account.entity.Password;

/**
 * パスワード変更サービス。
 * 
 */
@Service
public class UpdatePasswordService {

    /**
     * アカウントレポジトリ。
     */
    @Autowired
    private AccountRepository accountRepository;

    /**
     * パスワードエンコード。
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * パスワードを変更します。
     * 
     * @param memberId    会員ID。
     * @param newPassword 変更後パスワード。
     */
    @Transactional
    public void updatePassword(Long memberId, String newPassword) {

        // 認証済みアカウントのレコードをとれない場合、データ異常とみなす
        Account account = accountRepository.findById(memberId).orElseThrow();
        Password password = Password.builder().password(passwordEncoder.encode(newPassword)).build();
        account.getPasswords().add(password);
        accountRepository.save(account);
    }
}
