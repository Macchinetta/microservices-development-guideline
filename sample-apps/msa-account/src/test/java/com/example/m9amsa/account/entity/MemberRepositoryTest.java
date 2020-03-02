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
package com.example.m9amsa.account.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.account.constant.Gender;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Before
    public void before() {
        memberRepository.deleteAll();
        memberRepository.flush();
    }

    @After
    public void after() {
        memberRepository.deleteAll();
        memberRepository.flush();
    }

    /**
     * MemberRepositoryをテスト。
     * 
     * <pre>
     * -save - findAll
     * </pre>
     */
    @Test
    public void testMemberRepository() {

        Member expMember = Member.builder() //
                .memberId(1L).surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-5678")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();
        memberRepository.save(expMember);

        List<Member> result = memberRepository.findAll();

        assertThat("取得されるレコード数は1件であること", result.size(), equalTo(1));

        Member resultMember = result.get(0);
        assertThat("Member.surname", resultMember.getSurname(), equalTo(expMember.getSurname()));
        assertThat("Member.firstName", resultMember.getFirstName(), equalTo(expMember.getFirstName()));
        assertThat("Member.surnameKana", resultMember.getSurnameKana(), equalTo(expMember.getSurnameKana()));
        assertThat("Member.firstNameKana", resultMember.getFirstNameKana(), equalTo(expMember.getFirstNameKana()));
        assertThat("Member.birthday", resultMember.getBirthday(), equalTo(expMember.getBirthday()));
        assertThat("Member.gender", resultMember.getGender(), equalTo(expMember.getGender()));
        assertThat("Member.telephoneNo", resultMember.getTelephoneNo(), equalTo(expMember.getTelephoneNo()));
        assertThat("Member.postalCode", resultMember.getPostalCode(), equalTo(expMember.getPostalCode()));
        assertThat("Member.address", resultMember.getAddress(), equalTo(expMember.getAddress()));
        assertNotNull("Card is exists.", resultMember.getCard());

        Card resultCard = resultMember.getCard();
        Card expCard = expMember.getCard();
        assertThat("Card.cardNo", resultCard.getCardNo(), equalTo(resultCard.getCardNo()));
        assertThat("Card.cardCompanyName", resultCard.getCardCompanyName(), equalTo(expCard.getCardCompanyName()));
        assertThat("Card.validTillMonth", resultCard.getValidTillMonth(), equalTo(expCard.getValidTillMonth()));
        assertThat("Card.validTillYear", resultCard.getValidTillYear(), equalTo(expCard.getValidTillYear()));
    }

}
