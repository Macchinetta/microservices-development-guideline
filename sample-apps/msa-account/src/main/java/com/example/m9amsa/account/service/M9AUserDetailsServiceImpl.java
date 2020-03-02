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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

import com.example.m9amsa.account.config.OAuth2SecurityConfig;

/**
 * 認証・認可のカスタムクエリを処理する{@link JdbcDaoImpl}の拡張クラス。
 * 
 * <pre>
 * Springのデフォルト実装ではユーザIDをStringで処理しています(パラメータ名がusername)
 * 今回のリファレンス実装ではユーザID(会員ID)をJPAの仕様に従ってLongとしたため、JdbcDaoImplが数値を扱えるようにオーバーライドしています。
 * カスタムクエリは{@link OAuth2SecurityConfig#userDetailsService()}で設定しています。
 * </pre>
 * 
 */
public class M9AUserDetailsServiceImpl extends JdbcDaoImpl {

    /**
     * 数値ID対応のユーザ情報取得処理。
     * 
     * @param username 会員ID
     * @return {@code List<UserDetails> } ユーザ情報リスト
     */
    @Override
    protected List<UserDetails> loadUsersByUsername(String username) {
        return getJdbcTemplate().query(getUsersByUsernameQuery(), new Long[] { Long.valueOf(username) },
                new RowMapper<UserDetails>() {
                    @Override
                    public UserDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Long username = rs.getLong(1);
                        String password = rs.getString(2);
//                        boolean enabled = rs.getBoolean(3);
                        // 無効ユーザの設定はないのでenableに必ずtrueを指定
                        return new User(username.toString(), password, true, true, true, true,
                                AuthorityUtils.NO_AUTHORITIES);
                    }

                });
    }

    /**
     * 数値ID対応の認可情報取得処理。
     * 
     * @param username 会員ID
     * @return {@code List<GrantedAuthority>} 認可情報リスト
     */
    @Override
    protected List<GrantedAuthority> loadUserAuthorities(String username) {
        return getJdbcTemplate().query(getAuthoritiesByUsernameQuery(), new Long[] { Long.valueOf(username) },
                new RowMapper<GrantedAuthority>() {
                    @Override
                    public GrantedAuthority mapRow(ResultSet rs, int rowNum) throws SQLException {
                        String roleName = getRolePrefix() + rs.getString(2);

                        return new SimpleGrantedAuthority(roleName);
                    }
                });
    }
}
