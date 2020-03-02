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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.account.constant.Gender;
import com.example.m9amsa.account.entity.Account;
import com.example.m9amsa.account.entity.AccountRepository;
import com.example.m9amsa.account.entity.Authorities;
import com.example.m9amsa.account.entity.Member;
import com.example.m9amsa.account.entity.Password;
import com.example.m9amsa.account.model.AdmissionMemberInfo;
import com.example.m9amsa.account.model.CardInfo;
import com.example.m9amsa.account.model.MemberIdInfo;
import com.example.m9amsa.account.service.AdmissionService;
import com.example.m9amsa.account.service.MemberService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class AdmissionServiceTest {

    /**
     * アカウントリポジトリ。
     */
    @Mock
    private AccountRepository accountRepository;

    /**
     * 会員情報サービス。
     */
    @Mock
    private MemberService memberService;

    @Mock
    private EntityManager entityManager;

    @SpyBean
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdmissionService admissionService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    /**
     * Test for AdmissionService#createMemberAccount()
     */
    @Test
    public void testCreateMemberAccount() {
        // -- 正常系
        // input
        AdmissionMemberInfo inputMemberInfo = AdmissionMemberInfo.builder().surname("渡辺").firstName("太郎")
                .surnameKana("ワタナベ").firstNameKana("タロウ").birthday(LocalDate.of(2019, 9, 10)).gender(Gender.Male)
                .telephoneNo("090-1234-5678").postalCode("270-1234").address("東京都").emailId("sample@example.com")
                .card(CardInfo.builder().cardNo("123456789012").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth(9).validTillYear(23).build())
                .password("password").build();

        // actual
        Account actualCaptorAccount = Account.builder().build();
        actualCaptorAccount.getPasswords()
                .add(Password.builder().password(passwordEncoder.encode(inputMemberInfo.getPassword())).build());
        Account actualAccount = Account.builder().memberId(1L).build();
        actualAccount.getPasswords()
                .add(Password.builder().password(passwordEncoder.encode(inputMemberInfo.getPassword())).build());
        actualAccount.getAuthorities().add(Authorities.builder().memberId(1L).authority("USER").build());

        // when
        when(accountRepository.saveAndFlush(accountCaptor.capture())).thenReturn(actualAccount);

        // run & verify
        MemberIdInfo resultMemberId = admissionService.createMemberAccount(inputMemberInfo);
        boolean passwordMatche = passwordEncoder.matches(inputMemberInfo.getPassword(),
                actualCaptorAccount.getPasswords().iterator().next().getPassword());
        assertThat("accountRepositoryの引数のパスワードがエンコードされていること", passwordMatche, equalTo(true));
        assertThat("結果のmemberIdがaccountRepositoryの戻り値と一致すること", resultMemberId.getMemberId(),
                equalTo(actualAccount.getMemberId()));

        assertThat("入会時の引数としてUSERが指定されていること",
                accountCaptor.getAllValues().get(0).getAuthorities().stream().findFirst().get().getAuthority(),
                equalTo("USER"));

        // -- 異常系
        doThrow(new RuntimeException()).when(memberService).createMember(any(Member.class));
        try {
            admissionService.createMemberAccount(inputMemberInfo);
            fail("例外が返らない場合はテスト失敗");
        } catch (Exception e) {
            assertThat("サービスの例外をそのままスローすること", e.getClass(), equalTo(RuntimeException.class));
        }

        when(accountRepository.saveAndFlush(any(Account.class))).thenThrow(new IndexOutOfBoundsException());
        try {
            admissionService.createMemberAccount(inputMemberInfo);
            fail("例外が返らない場合はテスト失敗");
        } catch (Exception e) {
            assertThat("リポジトリの例外をそのままスローすること", e.getClass(), equalTo(IndexOutOfBoundsException.class));
        }
    }

}
