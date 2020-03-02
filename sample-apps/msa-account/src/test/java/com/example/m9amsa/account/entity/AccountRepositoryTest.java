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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
@Slf4j
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager entityManager;

    @SpyBean
    private BaseClock baseClock;

    @Before
    public void before() {
        reset(baseClock);
    }

    private void setClock(Clock clock) throws Exception {
        doReturn(clock).when(baseClock).systemDefaultZone();
    }

    /**
     * AccountRepository正常系テスト。
     */
    @Test
    @Transactional
    public void testAccountRepositoryCorrect() throws Exception {
        Clock baseClock = Clock.fixed(Instant.parse("2019-01-01T00:00:00Z"), ZoneId.systemDefault());
        setClock(baseClock);

        Account account = Account.builder().build();
        account.getPasswords().add(Password.builder().password("pass1234").build());
        accountRepository.saveAndFlush(account);
        account = accountRepository.findById(account.getMemberId()).orElseThrow();

        assertThat("会員Idが採番されていること", account.getMemberId(), greaterThan(0L));
        assertTrue("パスワードが作成されていること", account.getPasswords().stream().findFirst().isPresent());
        assertThat("パスワードが正しいこと", account.getPasswords().stream().findFirst().get().getPassword(), equalTo("pass1234"));
        assertThat("パスワード作成日時が正しいこと", account.getPasswords().stream().findFirst().get().getCreatedAt(),
                equalTo(LocalDateTime.now(baseClock)));
        assertTrue("ログインステータスが作成されていないこと", account.getLoginStatus().isEmpty());
        assertTrue("ログイン日時が作成されていないこと", account.getLastLogins().isEmpty());

        // ログインステータス、ログイン日時を追加
        LoginStatus loginStatus = LoginStatus.builder().memberId(account.getMemberId()).build();
        account.setLoginStatus(loginStatus);

        LastLogin lastLogin = LastLogin.builder().build();
        account.getLastLogins().add(lastLogin);

        accountRepository.saveAndFlush(account);
        account = accountRepository.findById(account.getMemberId()).orElseThrow();

        assertTrue("ログインステータスが作成されていること", account.getLoginStatus().isPresent());
        assertFalse("ログイン日時が作成されていること", account.getLastLogins().isEmpty());
        assertThat("ログイン日時が正しいこと", account.getLastLogins().stream().findFirst().get().getLoggedInAt(),
                equalTo(LocalDateTime.now(baseClock)));

        // 現在日付を変更
        baseClock = Clock.fixed(Instant.parse("2019-01-01T11:11:11Z"), ZoneId.systemDefault());
        setClock(baseClock);

        // パスワードを変更
        account.getPasswords().add(Password.builder().password("abcd5678").build());

        // ログイン日時を追加
        lastLogin = LastLogin.builder().build();
        account.getLastLogins().add(lastLogin);

        accountRepository.saveAndFlush(account);

        entityManager.clear();
        account = accountRepository.findById(account.getMemberId()).orElseThrow();
        log.info("**** account: {}", account);

        assertThat("1件目のログイン日時が正しいこと", account.getLastLogins().stream().findFirst().get().getLoggedInAt(),
                equalTo(LocalDateTime.now(baseClock)));
        assertThat("1件目のパスワード作成日時が正しいこと", account.getPasswords().stream().findFirst().get().getCreatedAt(),
                equalTo(LocalDateTime.now(baseClock)));
        assertThat("1件目のパスワードが正しいこと", account.getPasswords().stream().findFirst().get().getPassword(),
                equalTo("abcd5678"));

    }
}
