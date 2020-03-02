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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.account.constant.Gender;
import com.example.m9amsa.account.entity.Card;
import com.example.m9amsa.account.entity.CardRepository;
import com.example.m9amsa.account.entity.Member;
import com.example.m9amsa.account.entity.MemberRepository;
import com.example.m9amsa.account.model.MemberUpdateInfo;
import com.example.m9amsa.account.service.MemberService;
import com.example.m9amsa.account.service.UpdatePasswordService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UpdatePasswordService updatePasswordService;

    @InjectMocks
    private MemberService memberService;

    @Captor
    private ArgumentCaptor<Member> memberCaptor;

    @Captor
    private ArgumentCaptor<Card> cardCaptor;

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

        when(memberRepository.findById(0L)).thenReturn(Optional.empty());
        when(memberRepository.findById(1L)).thenReturn(Optional.of(expMember));

        // test result one
        Optional<Member> actual = memberService.getMember(1L);
        assertTrue("memberService.getMember()の値が取得できること", actual.isPresent());
        assertThat("memberService.getMember()の値が正しいこと", actual.get(), equalTo(expMember));

        // test result empty
        actual = memberService.getMember(0L);
        assertFalse("memberService.getMember()の値が取得できないこと", actual.isPresent());
    }

    @Test
    public void testUpdateMember() {

        Member currentMember = Member.builder().surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-9876")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();

        MemberUpdateInfo updateInfo = MemberUpdateInfo.builder() //
                .surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-9876")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();
        Member updatedMember = new Member();
        BeanUtils.copyProperties(updateInfo, updatedMember);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(currentMember));
        when(memberRepository.findById(0L)).thenReturn(Optional.empty());
        when(memberRepository.save(memberCaptor.capture())).thenReturn(updatedMember);
        doNothing().when(updatePasswordService).updatePassword(1L, "password");
        doNothing().when(cardRepository).delete(cardCaptor.capture());

        // test success
        try {
            memberService.updateMember(1L, updateInfo);
        } catch (Exception e) {
            fail("バリデーションOKの入力値に対して例外をスローしないこと");
        }

        // test password update sccess
        updateInfo.setCurrentPassword("current");
        updateInfo.setPassword("password");
        updateInfo.setReEnterPassword("password");
        try {
            memberService.updateMember(1L, updateInfo);
        } catch (Exception e) {
            fail("認証サービス正常終了の場合に例外をスローしないこと");
        }

        // test card update success
        updateInfo.getCard().setValidTillMonth("12");
        updateInfo.getCard().setValidTillYear("28");
        try {
            memberService.updateMember(1L, updateInfo);
        } catch (Exception e) {
            fail("カード更新正常終了の場合に例外をスローしないこと");
        }
        assertThat("カード削除に古いCardが指定されていること", cardCaptor.getValue(), equalTo(currentMember.getCard()));

        // test auth fail
        try {
            memberService.updateMember(0L, updateInfo);
            fail("会員情報検索エラーの場合に正常終了しないこと");
        } catch (Exception e) {
            assertThat("会員情報検索エラーの場合Optionalのエラーがそのままスローされること", e, instanceOf(NoSuchElementException.class));
        }
    }

    @Test
    public void testCreateMember() {

        Member member = Member.builder().surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-9876")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com").memberId(1L)
                .card(Card.builder().cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();

        when(memberRepository.save(Mockito.any(Member.class))).thenReturn(member);

        Member result = memberService.createMember(member);

        verify(memberRepository).save(member);
        assertThat("カード会員登録されること", result.getMemberId(), equalTo(1L));
    }
}
