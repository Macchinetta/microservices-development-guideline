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
package com.example.m9amsa.account.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.account.service.LogoutService;
import com.example.m9amsa.account.service.M9ATokenService;

/**
 * AuthControllerのテストケース。
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class AuthControllerTest {

    /**
     * ログアウトサービス。
     */
    @SpyBean
    private LogoutService logoutService;

    /**
     * トークン管理クラス
     */
    @Mock
    private TokenStore tokenStore;

    /**
     * M9AMSA拡張トークンサービス
     */
    @Mock
    private M9ATokenService tokenService;

    /**
     * 認証コントローラ。
     */
    @InjectMocks
    private AuthController authController;

    @Captor
    private ArgumentCaptor<OAuth2Authentication> authenticationCaptor;

    @Captor
    private ArgumentCaptor<String> tokenCaptor;

    @Captor
    private ArgumentCaptor<Long> memberIdCaptor;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(logoutService);
    }

    /**
     * Test for logout.
     */
    @Test
    public void testLogout() {

        // 正常系
        OAuth2Authentication authenticationMock = Mockito.mock(OAuth2Authentication.class);
        when(authenticationMock.getName()).thenReturn("1234");
        OAuth2AccessToken accessTokenMock = Mockito.mock(OAuth2AccessToken.class);
        when(accessTokenMock.getValue()).thenReturn("test-token");

        when(tokenStore.getAccessToken(authenticationCaptor.capture())).thenReturn(accessTokenMock);
        when(tokenService.revokeToken(tokenCaptor.capture())).thenReturn(true);
        doNothing().when(logoutService).logout(memberIdCaptor.capture());

        authController.logout(authenticationMock);

        verify(logoutService, times(1)).logout(memberIdCaptor.getValue());

        assertThat("tokenStoreへのパラメータが正しいこと", authenticationCaptor.getValue(), equalTo(authenticationMock));
        assertThat("tokenServiceへのパラメータが正しいこと", tokenCaptor.getValue(), equalTo(accessTokenMock.getValue()));
        assertThat("logoutServiceへのパラメータが正しいこと", memberIdCaptor.getValue(),
                equalTo(Long.valueOf(authenticationMock.getName())));

        // 異常系
        doThrow(new DataSourceLookupFailureException("test")).when(logoutService).logout(any(Long.class));
        try {
            authController.logout(authenticationMock);
            fail("例外が出ない場合テスト失敗");
        } catch (Exception e) {
            // ServiceAspectがRuntimeExceptionを捕まえて500例外を返す
            assertThat("logoutServiceの例外をそのままスローすること", e.getClass(), equalTo(DataSourceLookupFailureException.class));
            assertThat("logoutServiceの例外をそのままスローすること", e.getMessage(), equalTo("test"));
        }
    }

}
