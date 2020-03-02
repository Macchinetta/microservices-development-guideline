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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.m9amsa.account.service.M9AUserDetailsServiceImpl;

/**
 * Spring Security関連の設定クラス。
 * 
 */
@Configuration
@EnableWebSecurity
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * JDBC設定情報。
     */
    @Autowired
    private DataSource dataSource;

    /**
     * AuthenticationManagerの設定。
     * 
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(this.userDetailsService());
    }

    /**
     * AuthenticationManagerのbean定義。
     */
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * UserDetailsServiceのbean定義。
     * 
     * <pre>
     * username, password, authorityの取得クエリをここで設定します。
     * </pre>
     */
    @Bean
    public UserDetailsService userDetailsService() {
        M9AUserDetailsServiceImpl jdbcDaoImpl = new M9AUserDetailsServiceImpl();
        jdbcDaoImpl.setDataSource(dataSource);
        jdbcDaoImpl.setUsersByUsernameQuery(
                "select a.member_id as username, p.password from account a join account_passwords ap on a.member_id = ap.account_member_id join password p on p.password_id = ap.passwords_password_id where a.member_id = ? order by p.created_at limit 1");
        jdbcDaoImpl.setAuthoritiesByUsernameQuery(
                "select a.member_id as username, b.authority from account a join authorities b on a.member_id = b.member_id where a.member_id = ?");
        return jdbcDaoImpl;
    }

}