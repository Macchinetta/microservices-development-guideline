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

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.account.entity.LoginStatus;
import com.example.m9amsa.account.entity.LoginStatusRepository;
import com.example.m9amsa.account.service.LogoutService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class LogoutServiceTest {

    /**
     * ログインステータスリポジトリ。
     */
    @Autowired
    private LoginStatusRepository loginStatusRepository;

    @Autowired
    private LogoutService logoutService;

    @Captor
    private ArgumentCaptor<Long> memberIdCaptor;

    /**
     * test for logout()
     */
    @Test
    public void testLogout() {

        LoginStatus countCondition = LoginStatus.builder().memberId(1L).build();
        Example<LoginStatus> example = Example.of(countCondition);

        loginStatusRepository.deleteAll();
        LoginStatus loginStatus = LoginStatus.builder().loggedInAt(LocalDateTime.now()).memberId(1L).build();
        loginStatusRepository.save(loginStatus);
        assertThat("テストデータinsertの確認", loginStatusRepository.count(example), equalTo(1L));

        logoutService.logout(1L);

        assertThat("login_statusに指定したIDのレコードが残っていないこと", loginStatusRepository.count(example), equalTo(0L));

    }

}
