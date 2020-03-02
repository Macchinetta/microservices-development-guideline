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
package com.example.m9amsa.purchase.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.purchase.constant.SeatClass;
import com.example.m9amsa.purchase.entity.Payment;
import com.example.m9amsa.purchase.entity.Purchase;
import com.example.m9amsa.purchase.entity.PurchaseRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 購入状況照会サービスのテストクラス。
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_PURCHASE=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
@Slf4j
public class FindPurchaseServiceTest {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private FindPurchaseService findPurchaseService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test for findPurchase()。
     * 
     * <pre>
     *  正常系テスト。
     * </pre>
     */
    @Test
    @Transactional
    public void testFindPurchaseCorrect() {

        purchaseRepository.deleteAll();

        Long reserveId = 1L;
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

        Purchase purchase = Purchase.builder() //
                .reserveId(reserveId)//
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
        payment = purchase.getPayment();
        log.info("購入情報が作成されました。id: {}", purchase.getPurchaseId());
        log.info("決済情報が作成されました。id: {}", payment.getPaymentId());

        Optional<Purchase> purchaseResult = findPurchaseService.findPurchase(purchase.getReserveId());
        assertTrue("購入情報の検索ができました。", purchaseResult.isPresent());
        // 各項目チェック
        Purchase purchaseResultObj = purchaseResult.get();
        assertNotNull("購入Id検索ができました", purchaseResultObj.getPurchaseId());
        assertNotNull("予約Id検索ができました", purchaseResultObj.getReserveId());
        assertNotNull("購入者会員Id検索ができました", purchaseResultObj.getPurchaseMemberId());
        assertNotNull("出発日検索ができました", purchaseResultObj.getDepartureDate());
        assertNotNull("便Id検索ができました", purchaseResultObj.getFlightId());
        assertNotNull("出発時刻検索ができました", purchaseResultObj.getDepartureTime());
        assertNotNull("到着時刻検索ができました", purchaseResultObj.getArrivalTime());
        assertNotNull("出発空港検索ができました", purchaseResultObj.getDepartureAirportId());
        assertNotNull("到着空港検索ができました", purchaseResultObj.getArrivalAirportId());
        assertNotNull("搭乗クラス種別検索ができました", purchaseResultObj.getSeatClass());
        assertNotNull("運賃種別検索ができました", purchaseResultObj.getFareType());
        assertNotNull("運賃検索ができました", purchaseResultObj.getFare());
        assertNotNull("搭乗者人数検索ができました", purchaseResultObj.getPassengerCount());
        assertNotNull("購入情報登録日時検索ができました", purchaseResultObj.getPayDateTime());

        assertNotNull("購入情報の決済情報検索ができました。", purchaseResult.get().getPayment());
        // 各項目チェック
        Payment paymentObj = purchaseResult.get().getPayment();
        assertNotNull("決済Id検索ができました", paymentObj.getPaymentId());
        assertNotNull("決済日時検索ができました", paymentObj.getPayDateTime());
        assertNotNull("運賃検索ができました", paymentObj.getFare());
        assertNotNull("会員の姓 漢字検索ができました", paymentObj.getSurname());
        assertNotNull("会員の名 漢字検索ができました", paymentObj.getFirstName());
        assertNotNull("会員の姓 カタカナ検索ができました", paymentObj.getSurnameKana());
        assertNotNull("会員の名 カタカナ検索ができました", paymentObj.getFirstNameKana());
        assertNotNull("e-mailアドレス検索ができました", paymentObj.getEmailId());
        assertNotNull("カード番号検索ができました", paymentObj.getCardNo());
        assertNotNull("カード会社番号検索ができました", paymentObj.getCardCompanyCode());
        assertNotNull("カード会社検索ができました", paymentObj.getCardCompanyName());
        assertNotNull("カード有効期限の月検索ができました", paymentObj.getValidTillMonth());
        assertNotNull("カード有効期限の年検索ができました", paymentObj.getValidTillYear());
    }
}
