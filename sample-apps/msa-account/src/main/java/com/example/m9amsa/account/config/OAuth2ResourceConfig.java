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
package com.example.m9amsa.account.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * 認可情報の設定クラス。
 * 
 */
@Configuration
@EnableResourceServer
public class OAuth2ResourceConfig extends ResourceServerConfigurerAdapter {

    @Value("${info.url.root-path}")
    private String rootPath;

    /**
     * エンドポイント + ロールの設定。
     * 
     * @param http Springの認可情報設定クラス
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {

        http.csrf().disable() // 認証トークンの正統性チェックが実行されるのでCSRFチェックは行わない。画面遷移を含むトランザクションチェックが必要であればBFFに任せます。
                .httpBasic().disable() // ベーシック認証は使用しません。
                .formLogin().disable() // アカウントサービスとしてフォームログインを提供しません。
                .logout().disable() // アカウントサービスとしてSpring Securityのログアウト処理を提供しません。
                .authorizeRequests()//
                .antMatchers(String.format("/%s/account/auth/logout", rootPath)).authenticated()//
                .antMatchers(HttpMethod.GET, String.format("/%s/account/member", rootPath)).authenticated()//
                .antMatchers(HttpMethod.PUT, String.format("/%s/account/member", rootPath)).hasAuthority("USER")//
                .antMatchers(HttpMethod.POST, String.format("/%s/account/admission", rootPath)).authenticated()//
                .antMatchers(String.format("/%s/account/auth/login", rootPath)).permitAll()//
                .antMatchers(String.format("/%s/account/auth/validateUser", rootPath)).authenticated()//
                .antMatchers("/actuator", "/actuator/**").permitAll() //
                .anyRequest().denyAll();
    }

}
