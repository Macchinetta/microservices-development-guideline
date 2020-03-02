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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.account.constant.Gender;
import com.example.m9amsa.account.controller.AdmissionController;
import com.example.m9amsa.account.model.AdmissionMemberInfo;
import com.example.m9amsa.account.model.CardInfo;
import com.example.m9amsa.account.model.MemberIdInfo;
import com.example.m9amsa.account.service.AdmissionService;

/**
 * カード会員入会コントローラのテストケース。
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class AdmissionControllerTest {

    /**
     * カード会員入会サービス。
     */
    @SpyBean
    private AdmissionService admissionService;

    @Captor
    private ArgumentCaptor<AdmissionMemberInfo> admissionMemberInfoCaptor;

    @InjectMocks
    private AdmissionController admissionController;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(admissionService);

    }

    /**
     * Test for admission.
     */
    @Test
    public void testAdmission() {

        // 正常系
        AdmissionMemberInfo admissionMemberInfo = AdmissionMemberInfo.builder() /**/
                .surname("渡辺")/**/
                .firstName("太郎")/**/
                .surnameKana("ワタナベ")/**/
                .firstNameKana("タロウ")/**/
                .birthday(LocalDate.of(1990, 5, 1))/**/
                .gender(Gender.Male)/**/
                .telephoneNo("090-1234-5678")/**/
                .postalCode("270-1234")/**/
                .address("１丁目３－３東京")/**/
                .emailId("sample@example.com")/**/
                .card(CardInfo.builder().cardNo("1234567890123456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth(12).validTillYear(23).build())
                .password("password").build();
        MemberIdInfo returnMemberId = MemberIdInfo.builder().memberId(10L).build();

        doReturn(returnMemberId).when(admissionService).createMemberAccount(admissionMemberInfo);

        MemberIdInfo resultMemberIdInfo = admissionController.admission(admissionMemberInfo);

        verify(admissionService).createMemberAccount(admissionMemberInfoCaptor.capture());

        assertThat("admissionServiceへのパラメータが正しいこと", admissionMemberInfoCaptor.getValue(), equalTo(admissionMemberInfo));
        assertThat("admissionServiceの戻り値をそのまま返却できること", resultMemberIdInfo, equalTo(returnMemberId));
    }

}
