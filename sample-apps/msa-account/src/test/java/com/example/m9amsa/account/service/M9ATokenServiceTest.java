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
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.account.entity.Account;
import com.example.m9amsa.account.entity.AccountRepository;
import com.example.m9amsa.account.service.M9ATokenService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class M9ATokenServiceTest {

    @Autowired
    private M9ATokenService m9aTokenService;

    /**
     * アカウント情報
     */
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OAuth2Authentication authentication;

    @Mock
    private TokenStore tokenStore;

    @Captor
    private ArgumentCaptor<OAuth2Authentication> authenticationCaptor;

    @Captor
    private ArgumentCaptor<Long> memberIdCaptor;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m9aTokenService.setAccountRepository(accountRepository);
        m9aTokenService.setTokenStore(tokenStore);
        reset(accountRepository, authentication, tokenStore);
    }

    /**
     * Test for M9ATokenService#CreateAccessToken()
     */
    @Test
    public void testCreateAccessToken() throws Exception {

        OAuth2AccessToken token = new DefaultOAuth2AccessToken("test-token");
        Account account = Account.builder().memberId(1234L).build();

        when(authentication.getName()).thenReturn("1234");
        when(tokenStore.getAccessToken(authenticationCaptor.capture())).thenReturn(token);
        when(accountRepository.findById(memberIdCaptor.capture())).thenReturn(Optional.of(account));
        when(accountRepository.saveAndFlush(accountCaptor.capture())).thenReturn(any(Account.class));

        OAuth2AccessToken result = m9aTokenService.createAccessToken(authentication);

        // ログイン日時生成の確認はrepositoryのテストで実施
        assertThat("内部で取得するtokenをそのまま返せること", result, equalTo(token));
        assertThat("tokenStoreにauthenticationがそのまま渡ること", authenticationCaptor.getValue(), equalTo(authentication));
        assertThat("会員検索にauthenticationのvalueを使っていること", memberIdCaptor.getValue().toString(),
                equalTo(authentication.getName()));
        Account captorAccount = accountCaptor.getValue();
        assertThat("saveAndFlushするAccountのmemberIdが引数と一致すること", captorAccount.getMemberId().toString(),
                equalTo(authentication.getName()));
        assertThat("saveAndFlushするLoginStatusのmemberIdが引数と一致すること",
                captorAccount.getLoginStatus().get().getMemberId().toString(), equalTo(authentication.getName()));
        assertThat("saveAndFlushするLastLoginの生成されていること", captorAccount.getLastLogins().isEmpty(), equalTo(false));

        // 異常系 会員検索0件
        when(authentication.getName()).thenReturn("5678");
        when(accountRepository.findById(5678L)).thenReturn(Optional.empty());

        try {
            m9aTokenService.createAccessToken(authentication);
            fail("正常終了した場合テスト失敗");
        } catch (Exception e) {
            assertThat("NoSuchElementExceptionをそのまま返すこと", e.getClass(), equalTo(NoSuchElementException.class));
        }
    }

}
