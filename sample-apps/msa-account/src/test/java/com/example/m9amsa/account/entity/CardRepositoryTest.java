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
import static org.junit.Assert.assertThat;

import java.time.LocalDate;

import javax.transaction.Transactional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.account.constant.Gender;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class CardRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CardRepository cardRepository;

    @Before
    public void setUp() throws Exception {
        memberRepository.deleteAll();
        cardRepository.deleteAll();
        memberRepository.flush();
        cardRepository.flush();
    }

    @After
    public void tearDown() {
        memberRepository.deleteAll();
        cardRepository.deleteAll();
        memberRepository.flush();
        cardRepository.flush();
    }

    /**
     * CardRepository正常系テスト
     */
    @Test
    @Transactional
    public void testCardRepositoryCorrect() {
        Member expMember = Member.builder() //
                .memberId(1L).surname("渡辺").firstName("太郎").surnameKana("ワタナベ").firstNameKana("タロウ")
                .birthday(LocalDate.of(1980, 3, 12)).gender(Gender.Male).telephoneNo("080-1234-5678")
                .postalCode("272-1234").address("東京1-2-3").emailId("abc@example.com") //
                .card(Card.builder() //
                        .cardNo("1234-5678-9012-3456").cardCompanyCode("VIS").cardCompanyName("VISA")
                        .validTillMonth("09").validTillYear("23").build())
                .build();
        memberRepository.save(expMember);

        Card card = cardRepository
                .findById(
                        CardId.builder().cardNo("1234-5678-9012-3456").validTillMonth("09").validTillYear("23").build())
                .get();
        assertThat("カードを取得できること", card, equalTo(expMember.getCard()));
    }

}
