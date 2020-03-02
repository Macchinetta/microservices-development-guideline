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
package com.example.m9amsa.account.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.m9amsa.account.service.LogoutService;
import com.example.m9amsa.account.service.M9ATokenService;

/**
 * 認証コントローラ。
 * 
 * <pre>
 * ログインはSpring Security OAuth2のトークン発行処理を拡張して実装します。
 * このクラスではログアウト(認証トークンの無効化)を実装します。
 * </pre>
 * 
 */
@RestController
@RequestMapping("/${info.url.root-path}/account/auth")
@Validated
public class AuthController {

    /**
     * ログアウトサービス。
     */
    @Autowired
    private LogoutService logoutService;

    /**
     * トークン管理クラス。
     */
    @Autowired
    private TokenStore tokenStore;

    /**
     * M9AMSA拡張トークンサービス。
     */
    @Autowired
    private M9ATokenService tokenService;

    /**
     * ユーザーのバリデーション
     * 
     * @param user ユーザー
     * @return Principal
     */
    @RequestMapping("/validateUser")
    public Principal user(Principal user) {
        return user;
    }

    /**
     * ログアウトを行います。
     * 
     * <pre>
     * ログインの状況にかかわらず、ログアウト処理を行います。
     * </pre>
     * 
     * @param authentication 会員IDを含むOAuth2認証情報。
     */
    @PostMapping("/logout")
    public void logout(OAuth2Authentication authentication) {

        // トークンの破棄。結果に関わらずDB更新を行うため戻り値は無視
        tokenService.revokeToken(tokenStore.getAccessToken(authentication).getValue());

        // ログインステータス更新
        logoutService.logout(Long.valueOf(authentication.getName()));
    }
}
