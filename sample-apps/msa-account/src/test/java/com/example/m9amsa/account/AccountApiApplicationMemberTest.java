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
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.m9amsa.account.constant.Gender;
import com.example.m9amsa.account.controller.MemberController;
import com.example.m9amsa.account.entity.Account;
import com.example.m9amsa.account.entity.AccountRepository;
import com.example.m9amsa.account.entity.Authorities;
import com.example.m9amsa.account.entity.Card;
import com.example.m9amsa.account.entity.Member;
import com.example.m9amsa.account.entity.MemberRepository;
import com.example.m9amsa.account.entity.Password;
import com.example.m9amsa.account.entity.PasswordRepository;
import com.example.m9amsa.account.model.MemberUpdateInfo;
import com.example.m9amsa.account.service.MemberService;
import com.example.m9amsa.account.spy.MemberRepositorySpy;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class AccountApiApplicationMemberTest {

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private OAuthHelper oauthHelper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    @Value("${info.url.root-path}")
    private String urlRoot;
    private String urlBase;

    @Before
    public void setUp() throws Exception {
        // account, memberをあらかじめ消しておく
        accountRepository.deleteAll();
        memberRepository.deleteAll();
        passwordRepository.deleteAll();
        accountRepository.flush();
        memberRepository.flush();
        passwordRepository.flush();

        urlBase = String.format("/%s/account/member", urlRoot);
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
     * test for /member GET
     * 
     * <pre>
     * Correct pattern.
     * </pre>
     */
    @Test
    public void testGetMemberCorrect() throws Exception {
        Long memberId = this.insertAccount();
        Member expMember = Member.builder() //
                .memberId(memberId).surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-5678")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();

        Member savedMember = memberRepository.saveAndFlush(expMember);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(savedMember.getMemberId().toString(), "USER");

        mvc.perform( //
                MockMvcRequestBuilders //
                        .get(urlBase) //
                        .accept(MediaType.APPLICATION_JSON) //
                        .with(postProcessor)) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expMember))) //
        ;
    }

    /**
     * test for /member GET
     * 
     * <pre>
     * Error pattern.
     * - Http Status 401
     * </pre>
     */
    @Test
    public void testGetMember401() throws Exception {
        mvc.perform( //
                MockMvcRequestBuilders //
                        .get(urlBase) //
                        .accept(MediaType.APPLICATION_JSON) //
        ) //
                .andExpect(status().is(401)) //
        ;
    }

    /**
     * test for /member GET
     * 
     * <pre>
     * Error pattern.
     * - Http Status 404
     * </pre>
     */
    @Test
    public void testGetMember404() throws Exception {
        Long memberId = this.insertAccount();
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(memberId.toString(), "USER");

        mvc.perform( //
                MockMvcRequestBuilders //
                        .get(urlBase) //
                        .accept(MediaType.APPLICATION_JSON) //
                        .with(postProcessor)) //
                .andExpect(status().is(404)) //
        ;
    }

    /**
     * test for /member GET
     * 
     * <pre>
     * Error pattern.
     * - Http Status 500
     * </pre>
     */
    @Test
    public void testGetMember500() throws Exception {
        Long memberId = this.insertAccount();
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(memberId.toString(), "USER");

        MemberRepositorySpy memberRepositorySpy = (MemberRepositorySpy) memberRepository;
        MemberRepository org = memberRepositorySpy.getOrg();
        try {

            MemberRepository orgMock = mock(MemberRepository.class);
            doThrow(new RuntimeException("SQL ERROR")).when(orgMock).findById(memberId);
            memberRepositorySpy.setOrg(orgMock);

            mvc.perform( //
                    MockMvcRequestBuilders //
                            .get(urlBase) //
                            .accept(MediaType.APPLICATION_JSON) //
                            .with(postProcessor))
                    .andExpect(status().is(500)) //
            ;
        } finally {
            memberRepositorySpy.setOrg(org);

        }
    }

    /**
     * test for /member PUT
     * 
     * <pre>
     * Correct pattern.
     * </pre>
     */
    @Test
    public void testPutMemberCorrect() throws Exception {
        Long memberId = this.insertAccount();
        Member expMember = Member.builder() //
                .memberId(memberId).surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-5678")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();

        Member savedMember = memberRepository.saveAndFlush(expMember);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(savedMember.getMemberId().toString(), "USER");

        MemberUpdateInfo updateInfo = MemberUpdateInfo.builder() //
                .surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-9876")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .currentPassword("password").password("new-password").reEnterPassword("new-password").build();

        String json = jsonMapper.writeValueAsString(updateInfo);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .put(urlBase) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)) //
                .andExpect(status().isOk()) //
        ;

        Account account = accountRepository.findById(memberId).get();
        assertThat("パスワードの更新を確認", passwordEncoder.matches("new-password",
                account.getPasswords().stream().findFirst().get().getPassword()), equalTo(true));
    }

    /**
     * test for /member PUT
     * 
     * <pre>
     * Error pattern.
     * - Http Status 400
     * </pre>
     */
    @Test
    public void testPutMember400() throws Exception {
        Long memberId = this.insertAccount();
        MemberUpdateInfo updateInfo = MemberUpdateInfo.builder() //
//                .surname("渡辺")
                .firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ").birthday(LocalDate.of(1980, 3, 12))
                .gender(Gender.Male).telephoneNo("080-1234-9876").postalCode("272-1234").address("東京1-2-3")
                .emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(memberId.toString(), "USER");

        String json = jsonMapper.writeValueAsString(updateInfo);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .put(urlBase) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)) //
                .andExpect(status().is(400)) //
        ;

        updateInfo.setSurname("渡辺");
        updateInfo.setPassword("password");
        updateInfo.setReEnterPassword("wrong");
        updateInfo.setCurrentPassword("current-password");

        json = jsonMapper.writeValueAsString(updateInfo);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .put(urlBase) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)) //
                .andExpect(status().is(400)) //
        ;
    }

    /**
     * test for /member PUT
     * 
     * <pre>
     * Error pattern.
     * - Http Status 401
     * </pre>
     */
    @Test
    public void testPutMember401() throws Exception {
//        Long memberId = this.insertAccount();

        MemberUpdateInfo updateInfo = MemberUpdateInfo.builder() //
                .surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-9876")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com").currentPassword("current")
                .password("password").reEnterPassword("password") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();

        String json = jsonMapper.writeValueAsString(updateInfo);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .put(urlBase) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8))//
                .andExpect(status().is(401)) //
        ;
    }

    /**
     * test for /member PUT
     * 
     * <pre>
     * Error pattern.
     * - Http Status 403
     * </pre>
     */
    @Test
    public void testPutMember403() throws Exception {
        Long memberId = this.insertAccount();
        Member expMember = Member.builder() //
                .memberId(memberId).surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-5678")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();

        Member savedMember = memberRepository.saveAndFlush(expMember);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(savedMember.getMemberId().toString(), "GUEST");

        MemberUpdateInfo updateInfo = MemberUpdateInfo.builder() //
                .surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-9876")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .currentPassword("password").password("new-password").reEnterPassword("new-password").build();

        String json = jsonMapper.writeValueAsString(updateInfo);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .put(urlBase) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)) //
                .andExpect(status().is(403)) // Access is denied
        ;
    }

    /**
     * test for /member PUT
     * 
     * <pre>
     * Error pattern.
     * - Http Status 500
     * </pre>
     */
    @Test
    public void testPutMember500() throws Exception {
        Long memberId = this.insertAccount();

        MemberUpdateInfo updateInfo = MemberUpdateInfo.builder() //
                .surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-9876")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com").currentPassword("current")
                .password("password").reEnterPassword("password") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(memberId.toString(), "USER");

        String json = jsonMapper.writeValueAsString(updateInfo);

        doThrow(new NoSuchElementException()).when(memberService).updateMember(memberId, updateInfo);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .put(urlBase) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)) //
                .andExpect(status().is(500)) //
        ;
    }
}
