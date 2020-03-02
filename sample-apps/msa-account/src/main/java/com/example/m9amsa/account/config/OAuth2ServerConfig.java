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

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import com.example.m9amsa.account.entity.AccountRepository;
import com.example.m9amsa.account.service.M9ATokenService;

/**
 * OAuth2認証サーバの設定クラス。
 * 
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2ServerConfig extends AuthorizationServerConfigurerAdapter {

    /**
     * アプリ全体のURLパスprefix。
     */
    @Value("${info.url.root-path}")
    private String rootPath;

    /**
     * application.ymlに定義したdatasource。
     */
    @Autowired
    private DataSource dataSource;

    /**
     * 認証処理マネージャー。
     */
    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    /**
     * アカウントレポジトリ。
     */
    @Autowired
    private AccountRepository accountRepository;

    /**
     * AuthorizationServerSecurityConfigurerへの設定追加。
     * 
     * <pre>
     * ここではPasswordEncoderを設定しています。
     * </pre>
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.passwordEncoder(this.passwordEncoder());
    }

    /**
     * AuthorizationServerEndpointsConfigurerへの設定追加。
     * 
     * <pre>
     * トークン発行処理のエンドポイント(/oauth/token)を/${rootPath}/auth/loginに書き換えています。
     * </pre>
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        endpoints.pathMapping("/oauth/token", String.format("/%s/account/auth/login", rootPath))
                .authenticationManager(authenticationManager).tokenServices(tokenServices());
    }

    /**
     * ClientDetailsServiceConfigurerへの設定追加。
     * 
     * <pre>
     * dataSourceを設定しています。
     * </pre>
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource);
    }

    /**
     * bean名tokenStoreにJdbcTokenStoreを設定します。
     * 
     * @return {@link JdbcTokenStore}
     */
    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    /**
     * bean名clientDetailsにJdbcClientDetailsServiceを設定します。
     * 
     * @return {@link JdbcClientDetailsService}
     */
    @Bean
    public ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }

    /**
     * bean名tokenServiceにM9ATokenServiceを設定します。
     * 
     * @return M9AMSA用のトークンサービス
     */
    @Bean
    public M9ATokenService tokenServices() {

        M9ATokenService tokenService = new M9ATokenService();
        tokenService.setTokenStore(tokenStore());
        tokenService.setSupportRefreshToken(true);
        tokenService.setAccountRepository(accountRepository);
        return tokenService;
    }

    /**
     * bean名passwordEncoderにBCryptPasswordEncoderを設定します。
     * 
     * @return {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}