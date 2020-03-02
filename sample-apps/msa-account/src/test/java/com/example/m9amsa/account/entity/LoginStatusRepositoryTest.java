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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class LoginStatusRepositoryTest {

    @Autowired
    private LoginStatusRepository loginStatusRepository;

    @SpyBean
    private BaseClock baseClock;

    @Before
    public void setUp() throws Exception {
        loginStatusRepository.deleteAll();
        loginStatusRepository.flush();
        reset(baseClock);
    }

    private void setClock(Clock clock) throws Exception {
        doReturn(clock).when(baseClock).systemDefaultZone();
    }

    /**
     * LoginStatusRepository正常系テスト
     */
    @Test
    @Transactional
    public void testLoginStatusRepositoryCorrect() throws Exception {
        Clock baseClock = Clock.fixed(Instant.parse("2019-01-01T00:00:00Z"), ZoneId.systemDefault());
        setClock(baseClock);

        LoginStatus loginStatus = LoginStatus.builder().memberId(1L).build();
        loginStatusRepository.saveAndFlush(loginStatus);
        loginStatus = loginStatusRepository.findById(1L).get();

        assertThat("ログインステータスが登録されていること", loginStatus.getMemberId(), equalTo(1L));
        assertThat("ログイン日時に現在時刻が登録されていること", loginStatus.getLoggedInAt(), equalTo(LocalDateTime.now(baseClock)));
    }

}
