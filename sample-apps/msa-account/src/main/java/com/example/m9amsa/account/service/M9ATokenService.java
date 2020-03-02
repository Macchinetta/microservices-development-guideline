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

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.account.entity.Account;
import com.example.m9amsa.account.entity.AccountRepository;
import com.example.m9amsa.account.entity.LastLogin;
import com.example.m9amsa.account.entity.LoginStatus;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2トークン発行処理にログインステータスの更新処理を追加する拡張サービスクラス。
 * 
 */
@Setter
@Slf4j
public class M9ATokenService extends DefaultTokenServices {

    /**
     * アカウント情報。
     */
    private AccountRepository accountRepository;

    /**
     * {@link DefaultTokenServices#createAccessToken(OAuth2Authentication)}を実行した後、前回ログイン日時とログインステータスの更新を行う拡張処理です。
     */
    @Override
    @Transactional
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {

        OAuth2AccessToken token = super.createAccessToken(authentication);

        // tokenが正常に発行されたらログイン関連テーブルの更新を行います
        Account account = accountRepository.findById(Long.valueOf(authentication.getName())).get();

        account.setLoginStatus(LoginStatus.builder().memberId(account.getMemberId()).build());
        account.getLastLogins().add(LastLogin.builder().build());

        log.info("** login : {}", account);
        accountRepository.saveAndFlush(account);

        return token;
    }

}
