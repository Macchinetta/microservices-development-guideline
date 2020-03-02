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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.account.entity.Account;
import com.example.m9amsa.account.entity.AccountRepository;
import com.example.m9amsa.account.entity.Authorities;
import com.example.m9amsa.account.entity.Password;
import com.example.m9amsa.account.entity.PasswordRepository;
import com.example.m9amsa.account.service.M9AUserDetailsServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class M9AUserDetailsServiceImplTest {

    @Autowired
    private M9AUserDetailsServiceImpl userDetailsService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Before
    public void setUp() throws Exception {
        accountRepository.deleteAll();
        passwordRepository.deleteAll();
    }

    /**
     * {@link JdbcDaoImpl#loadUserByUsername(String)}を実行してM9AUserDetailsServiceImplの拡張部分をテストします
     */
    @Test
    public void testLoadUsersByUsername() {

        Account account = Account.builder().build();
        account = accountRepository.saveAndFlush(account);

        System.out.println("member_id = " + account.getMemberId().toString());
        account.getPasswords().add(Password.builder().password(passwordEncoder.encode("password")).build());
        account.getAuthorities().add(Authorities.builder().memberId(account.getMemberId()).authority("USER").build());
        accountRepository.saveAndFlush(account);

        // run
        UserDetails result = userDetailsService.loadUserByUsername(account.getMemberId().toString());

        assertThat("ユーザ名の確認", result.getUsername(), equalTo(account.getMemberId().toString()));
        assertThat("パスワードが取得できていること", result.getPassword(),
                equalTo(account.getPasswords().stream().findFirst().get().getPassword()));
        assertThat("取得した認可権限が1件であること", result.getAuthorities().size(), equalTo(1));
        assertThat("取得した認可権限が正しいこと", result.getAuthorities().stream().findFirst().get().getAuthority(),
                equalTo(account.getAuthorities().stream().findFirst().get().getAuthority()));
    }

}
