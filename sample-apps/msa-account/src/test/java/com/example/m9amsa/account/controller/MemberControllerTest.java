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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.account.constant.Gender;
import com.example.m9amsa.account.controller.MemberController;
import com.example.m9amsa.account.entity.Card;
import com.example.m9amsa.account.entity.Member;
import com.example.m9amsa.account.model.MemberUpdateInfo;
import com.example.m9amsa.account.service.MemberService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class MemberControllerTest {
    @Mock
    MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    @Mock
    private OAuth2Authentication authentication;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testGetMember() {

        Member expMember = Member.builder() //
                .surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-5678")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();

        when(memberService.getMember(1L)).thenReturn(Optional.of(expMember));
        when(authentication.getName()).thenReturn("1");

        // test result one
        Member actual = memberController.getMember(authentication);
        assertNotNull("memberController.getMember()の値が取得できること", actual);
        assertThat("memberController.getMember()の値が正しいこと", actual, equalTo(expMember));
    }

    @Test
    public void testUpdateMember() {

        MemberUpdateInfo memberUpdateInfo = MemberUpdateInfo.builder() //
                .surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-5678")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .password("new-password").reEnterPassword("new-password").currentPassword("password").build();

        doNothing().when(memberService).updateMember(0L, memberUpdateInfo);
        doThrow(new RuntimeException()).when(memberService).updateMember(1L, memberUpdateInfo);
        when(authentication.getName()).thenReturn("0");

        // test result one
        try {
            memberController.updateMember(authentication, memberUpdateInfo);
        } catch (Exception e) {
            fail("serviceの正常終了時に例外をスローしないこと");
        }
    }
}
