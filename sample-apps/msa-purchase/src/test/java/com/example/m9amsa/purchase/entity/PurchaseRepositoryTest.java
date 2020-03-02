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
package com.example.m9amsa.purchase.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.purchase.constant.SeatClass;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_PURCHASE=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
@Slf4j
public class PurchaseRepositoryTest {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private EntityManager entityManager;

    @SpyBean
    private BaseClock baseClock;

    @Before
    public void before() {
        reset(baseClock);

        entityManager.clear();
    }

    private void setClock(Clock clock) throws Exception {
        doReturn(clock).when(baseClock).systemDefaultZone();
    }

    /**
     * PurchaseRepository正常系テスト。
     */
    @Test
    @Transactional
    public void testPurchaseRepositoryCorrect() throws Exception {

        Clock clock = Clock.fixed(Instant.parse("2019-01-01T00:00:00Z"), ZoneId.systemDefault());
        setClock(clock);

        Purchase purchase = Purchase.builder() //
                .reserveId(1L)//
                .purchaseMemberId(2L)//
                .departureDate(LocalDate.now())//
                .flightId("NTT001")//
                .departureTime(LocalTime.now())//
                .arrivalTime(LocalTime.now())//
                .departureAirportId("HND")//
                .arrivalAirportId("CTS")//
                .seatClass(SeatClass.N)//
                .fareType("basicFare")//
                .fare(30000)//
                .passengerCount(3)//
                .payDateTime(LocalDateTime.now())//
                .build();

        purchase = purchaseRepository.saveAndFlush(purchase);
        log.info("購入情報が作成されました。id: {}", purchase.getPurchaseId());

        assertThat("購入情報が作成されていること", purchase.getPurchaseId(), greaterThan(0L));
        assertThat("購入情報の購入情報登録日時が正しいこと。", purchase.getPayDateTime(), equalTo(LocalDateTime.now(clock)));
        assertNull("決済情報が作成されていないこと", purchase.getPayment());

        Payment payment = Payment.builder()//
                .payDateTime(LocalDateTime.now()) //
                .fare(1000) //
                .surname("渡辺") //
                .firstName("太郎") //
                .surnameKana("ワタナベ")//
                .firstNameKana("タロウ") //
                .emailId("sample@example.com")//
                .cardNo("1234567890123456")//
                .cardCompanyCode("VIS")//
                .cardCompanyName("VISA")//
                .validTillMonth("12")//
                .validTillYear("23")//
                .build();

        purchase = Purchase.builder() //
                .reserveId(2L)//
                .purchaseMemberId(3L)//
                .departureDate(LocalDate.now())//
                .flightId("NTT001")//
                .departureTime(LocalTime.now())//
                .arrivalTime(LocalTime.now())//
                .departureAirportId("HND")//
                .arrivalAirportId("CTS")//
                .seatClass(SeatClass.N)//
                .fareType("basicFare")//
                .fare(30000)//
                .passengerCount(3)//
                .payDateTime(LocalDateTime.now())//
                .payment(payment)//
                .build();

        purchase = purchaseRepository.saveAndFlush(purchase);
        log.info("購入情報が作成されました。id: {}", purchase.getPurchaseId());
        log.info("決済情報が作成されました。id: {}", payment.getPaymentId());

        assertThat("購入情報が作成されていること", purchase.getPurchaseId(), greaterThan(0L));
        assertThat("購入情報の購入情報登録日時が正しいこと。", purchase.getPayDateTime(), equalTo(LocalDateTime.now(clock)));

        assertThat("決済情報が作成されていること", purchase.getPayment().getPaymentId(), greaterThan(0L));
        assertThat("決済情報の購入情報登録日時が正しいこと。", purchase.getPayment().getPayDateTime(), equalTo(LocalDateTime.now(clock)));

        // 現在日付を変更
        clock = Clock.fixed(Instant.parse("2019-01-01T11:11:11Z"), ZoneId.systemDefault());
        setClock(clock);

        payment = Payment.builder()//
                .payDateTime(LocalDateTime.now()) //
                .fare(1000) //
                .surname("渡辺") //
                .firstName("太郎") //
                .surnameKana("ワタナベ")//
                .firstNameKana("タロウ") //
                .emailId("sample@example.com")//
                .cardNo("1234567890123456")//
                .cardCompanyCode("VIS")//
                .cardCompanyName("VISA")//
                .validTillMonth("12")//
                .validTillYear("23")//
                .build();

        purchase = Purchase.builder() //
                .reserveId(1L)//
                .purchaseMemberId(2L)//
                .departureDate(LocalDate.now())//
                .flightId("NTT001")//
                .departureTime(LocalTime.now())//
                .arrivalTime(LocalTime.now())//
                .departureAirportId("HND")//
                .arrivalAirportId("CTS")//
                .seatClass(SeatClass.N)//
                .fareType("basicFare")//
                .fare(30000)//
                .passengerCount(3)//
                .payDateTime(LocalDateTime.now())//
                .payment(payment)//
                .build();

        purchase = purchaseRepository.saveAndFlush(purchase);
        log.info("購入情報が作成されました。id: {}", purchase.getPurchaseId());

        purchase = purchaseRepository.findById(purchase.getPurchaseId()).orElseThrow();

        assertThat("購入情報が作成されていること", purchase.getPurchaseId(), greaterThan(0L));
        assertThat("購入情報の購入情報登録日時が正しいこと。", purchase.getPayDateTime(), equalTo(LocalDateTime.now(clock)));
        assertThat("決済情報が作成されていること", purchase.getPayment().getPaymentId(), greaterThan(0L));
        assertThat("決済情報の購入情報登録日時が正しいこと。", purchase.getPayment().getPayDateTime(), equalTo(LocalDateTime.now(clock)));

    }
}
