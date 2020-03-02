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
package com.example.m9amsa.account;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.m9amsa.account.entity.Account;
import com.example.m9amsa.account.entity.AccountRepository;
import com.example.m9amsa.account.entity.Authorities;
import com.example.m9amsa.account.entity.LoginStatus;
import com.example.m9amsa.account.entity.LoginStatusRepository;
import com.example.m9amsa.account.entity.Password;
import com.example.m9amsa.account.entity.PasswordRepository;
import com.example.m9amsa.account.service.LogoutService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class AccountApiApplicationAuthTest {

    @Autowired
    private OAuthHelper oauthHelper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private LoginStatusRepository loginStatusRepository;

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private LogoutService logoutService;

    @Value("${info.url.root-path}")
    private String urlRoot;
    private String urlBase;

    @Before
    public void setUp() throws Exception {
        accountRepository.deleteAll();
        accountRepository.flush();
        passwordRepository.deleteAll();
        passwordRepository.flush();

        urlBase = String.format("/%s/account/auth", urlRoot);
    }

    private Long insertAccount() {
        // accountをinsertしてmember_idを生成
        Account account = Account.builder().build();
        account = accountRepository.saveAndFlush(account);
        System.out.println("member_id = " + account.getMemberId());
        account.getPasswords().add(Password.builder().password("password").build());
        account.getAuthorities().add(Authorities.builder().memberId(account.getMemberId()).authority("USER").build());
        accountRepository.saveAndFlush(account);
        System.out.println(accountRepository.findById(account.getMemberId()).orElseThrow());
        return account.getMemberId();
    }

    /**
     * test for /auth/logout
     * 
     * <pre>
     * Correct pattern.
     * </pre>
     */
    @Test
    public void testLogoutCorrect() throws Exception {
        Long memberId = this.insertAccount();

        // ログイン済みにします
        Account account = accountRepository.findById(memberId).get();
        account.setLoginStatus(LoginStatus.builder().memberId(memberId).build());
        accountRepository.saveAndFlush(account);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(memberId.toString(), "USER");

        assertThat("事前確認：ログインステータスがあること", loginStatusRepository.existsById(memberId), equalTo(true));

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post(urlBase + "/logout") //
                        .accept(MediaType.APPLICATION_JSON) //
                        .with(postProcessor)) //
                .andExpect(status().isOk()) //
        ;

        assertThat("ログインステータスが削除されていること", loginStatusRepository.existsById(memberId), equalTo(false));
    }

    /**
     * test for /auth/logout
     * 
     * <pre>
     * Error pattern.
     * - Http Status 401
     * </pre>
     */
    @Test
    public void testLogout401() throws Exception {

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post(urlBase + "/logout") //
                        .accept(MediaType.APPLICATION_JSON) //
//                        .with(postProcessor)
        ) //
                .andExpect(status().is(401)) //
        ;
    }

    /**
     * test for /auth/logout
     * 
     * <pre>
     * Error pattern.
     * - Http Status 500
     * </pre>
     */
    @Test
    public void testLogout500() throws Exception {
        Long memberId = this.insertAccount();

        // ログイン済みにします
        Account account = accountRepository.findById(memberId).get();
        account.setLoginStatus(LoginStatus.builder().memberId(memberId).build());
        accountRepository.saveAndFlush(account);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(memberId.toString(), "USER");

        assertThat("事前確認：ログインステータスがあること", loginStatusRepository.existsById(memberId), equalTo(true));

        doThrow(new RuntimeException()).when(logoutService).logout(memberId);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post(urlBase + "/logout") //
                        .accept(MediaType.APPLICATION_JSON) //
                        .with(postProcessor)) //
                .andExpect(status().is(500)) //
        ;
    }
}
