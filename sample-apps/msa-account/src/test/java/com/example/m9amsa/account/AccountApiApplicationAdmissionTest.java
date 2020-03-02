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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.m9amsa.account.constant.Gender;
import com.example.m9amsa.account.entity.Account;
import com.example.m9amsa.account.entity.AccountRepository;
import com.example.m9amsa.account.entity.Authorities;
import com.example.m9amsa.account.entity.Password;
import com.example.m9amsa.account.entity.PasswordRepository;
import com.example.m9amsa.account.model.AdmissionMemberInfo;
import com.example.m9amsa.account.model.CardInfo;
import com.example.m9amsa.account.model.MemberIdInfo;
import com.example.m9amsa.account.service.AdmissionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class AccountApiApplicationAdmissionTest {

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private AdmissionService admissionAccountService;

    @Autowired
    private OAuthHelper oauthHelper;

    @Value("${info.url.root-path}")
    private String urlRoot;
    private String urlBase;

    @Before
    public void setUp() throws Exception {
        accountRepository.deleteAll();
        accountRepository.flush();
        passwordRepository.deleteAll();
        passwordRepository.flush();

        urlBase = String.format("/%s/account/admission", urlRoot);
    }

    /**
     * Test for admission.
     */
    @Test
    public void testAdmission() throws Exception {

        AdmissionMemberInfo admissionMemberInfo = AdmissionMemberInfo.builder() /**/
                .surname("渡辺")/**/
                .firstName("太郎")/**/
                .surnameKana("ワタナベ")/**/
                .firstNameKana("タロウ")/**/
                .birthday(LocalDate.of(1980, 3, 12))/**/
                .gender(Gender.Male)/**/
                .telephoneNo("090-1234-5678")/**/
                .postalCode("270-1234")/**/
                .address("１丁目３－３東京")/**/
                .emailId("sample@example.com")/**/
                .card(CardInfo.builder().cardNo("1234567890123456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth(12).validTillYear(23).build())
                .password("password").build();

        String json = jsonMapper.writeValueAsString(admissionMemberInfo);
        Long memberId = this.insertAccount();
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(memberId.toString(), "USER");

        MvcResult result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post(urlBase) //
                        .accept(MediaType.APPLICATION_JSON) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)//
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andReturn();

        accountRepository.deleteById(memberId);

        List<Account> accounts = accountRepository.findAll();
        Account resultAccount = accounts.get(0);

        assertThat("会員の登録件数は1件であること", accounts.size(), equalTo(1));
        assertThat("パスワードが正しいこと", passwordEncoder.matches("password",
                resultAccount.getPasswords().stream().findFirst().get().getPassword()), equalTo(true));

        MemberIdInfo info = jsonMapper.readValue(result.getResponse().getContentAsString(), MemberIdInfo.class);
        assertThat("memberIdが返却されること", info.getMemberId(), equalTo(resultAccount.getMemberId()));
    }

    /**
     * test for /admission POST
     * 
     * <pre>
     * Error pattern.
     * - Http Status 400
     * </pre>
     */
    @Test
    public void testAdmission400() throws Exception {

        AdmissionMemberInfo admissionMemberInfo = AdmissionMemberInfo.builder() /**/
//                .surname("渡辺")/**/
                .firstName("太郎")/**/
                .surnameKana("ワタナベ")/**/
                .firstNameKana("タロウ")/**/
                .birthday(LocalDate.of(1980, 3, 12))/**/
                .gender(Gender.Male)/**/
                .telephoneNo("090-1234-5678")/**/
                .postalCode("270-1234")/**/
                .address("１丁目３－３東京")/**/
                .emailId("sample@example.com")/**/
                .card(CardInfo.builder().cardNo("1234567890123456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth(12).validTillYear(23).build())
                .password("password").build();

        String json = jsonMapper.writeValueAsString(admissionMemberInfo);

        Long memberId = this.insertAccount();
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(memberId.toString(), "USER");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post(urlBase)//
                        .accept(MediaType.APPLICATION_JSON) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)//
        ) //
                .andExpect(status().is(400)) //
        ;
    }

    /**
     * test for /admission POST
     * 
     * <pre>
     * Error pattern.
     * - Http Status 500
     * </pre>
     */
    @Test
    public void testAdmission500() throws Exception {

        AdmissionMemberInfo admissionMemberInfo = AdmissionMemberInfo.builder() /**/
                .surname("渡辺")/**/
                .firstName("太郎")/**/
                .surnameKana("ワタナベ")/**/
                .firstNameKana("タロウ")/**/
                .birthday(LocalDate.of(1980, 3, 12))/**/
                .gender(Gender.Male)/**/
                .telephoneNo("090-1234-5678")/**/
                .postalCode("270-1234")/**/
                .address("１丁目３－３東京")/**/
                .emailId("sample@example.com")/**/
                .card(CardInfo.builder().cardNo("1234567890123456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth(12).validTillYear(23).build())
                .password("password").build();

        String json = jsonMapper.writeValueAsString(admissionMemberInfo);

        doThrow(new RuntimeException()).when(admissionAccountService)
                .createMemberAccount(any(AdmissionMemberInfo.class));

        Long memberId = this.insertAccount();
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(memberId.toString(), "USER");

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post(urlBase) //
                        .accept(MediaType.APPLICATION_JSON) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)//
        ) //
                .andExpect(status().is(500)) //
        ;
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
}
