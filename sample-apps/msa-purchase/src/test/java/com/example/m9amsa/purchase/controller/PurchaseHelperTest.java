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
package com.example.m9amsa.purchase.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.purchase.constant.Gender;
import com.example.m9amsa.purchase.constant.SeatClass;
import com.example.m9amsa.purchase.entity.Payment;
import com.example.m9amsa.purchase.entity.Purchase;
import com.example.m9amsa.purchase.model.CardInfo;
import com.example.m9amsa.purchase.model.MemberInfo;
import com.example.m9amsa.purchase.model.PassengerInfo;
import com.example.m9amsa.purchase.model.ReserveInfo;
import com.example.m9amsa.purchase.model.topic.PassengerTopic;
import com.example.m9amsa.purchase.model.topic.PurchaseTopic;

/**
 * PurchaseHelperのテストケース。
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_PURCHASE=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class PurchaseHelperTest {

    @Autowired
    private PurchaseHelper purchaseHelper;
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreatePurchaseTopic() {
        // ①非会員：購入者会員情報なし
        // 予約情報を作成します
        ReserveInfo reserveInfo = createReserveInfo(1L);
        Purchase purchase = createPurchase(reserveInfo);
        // assert
        PurchaseTopic purchaseTopic = purchaseHelper.createPurchaseTopic(reserveInfo, purchase);
        assertThat("purchaseTopic.getArrivalAirportId()", purchaseTopic.getArrivalAirportId(), equalTo(reserveInfo.getArrivalAirportId()));
        assertThat("purchaseTopic.getArrivalTime()", purchaseTopic.getArrivalTime(), equalTo(reserveInfo.getArrivalTime()));
        assertNull("purchaseTopic.getCardNo()", purchaseTopic.getCardNo());
        assertThat("purchaseTopic.getDepartureAirportId()", purchaseTopic.getDepartureAirportId(), equalTo(reserveInfo.getDepartureAirportId()));
        assertThat("purchaseTopic.getDepartureDate()", purchaseTopic.getDepartureDate(), equalTo(reserveInfo.getDepartureDate()));
        assertThat("purchaseTopic.getFare()", purchaseTopic.getFare(), equalTo(reserveInfo.getFare()));
        assertThat("purchaseTopic.getFareType()", purchaseTopic.getFareType(), equalTo(reserveInfo.getFareType()));
        assertThat("purchaseTopic.getFlightId()", purchaseTopic.getFlightId(), equalTo(reserveInfo.getFlightId()));
        
        int count = 0;
        for (PassengerTopic passengerTopic: purchaseTopic.getPassengers()) {
            PassengerInfo info = new PassengerInfo();
            BeanUtils.copyProperties(passengerTopic, info);
            assertThat("passengerTopic", info, equalTo(reserveInfo.getPassengers().get(count)));
            count++;
        }

        assertNull("purchaseTopic.getPayDateTime()", purchaseTopic.getPayDateTime());
        assertNull("purchaseTopic.getPaymentId()", purchaseTopic.getPaymentId());
        assertThat("purchaseTopic.getPurchaseId()", purchaseTopic.getPurchaseId(), equalTo(purchase.getPurchaseId()));
        assertTrue("purchaseTopic.getPurchaseMember()", purchaseTopic.getPurchaseMember().isEmpty());
        assertThat("purchaseTopic.getReserveId()", purchaseTopic.getReserveId(), equalTo(purchase.getReserveId()));
        assertThat("purchaseTopic.getSeatClass()", purchaseTopic.getSeatClass(), equalTo(purchase.getSeatClass()));

        // ②会員：購入者会員情報あり、コード情報なし
        ReserveInfo reserveInfo2 = createReserveInfo(2L);
        Purchase purchase2 = createPurchase(reserveInfo2);
        MemberInfo member = createMemberInfo(purchaseTopic.getPassengers().get(0));
        reserveInfo2.setPurchaseMember(Optional.of(member));
        
        // assert
        PurchaseTopic purchaseTopic2 = purchaseHelper.createPurchaseTopic(reserveInfo2, purchase2);
        System.out.println(purchaseTopic2);
        assertThat("purchaseTopic2.getArrivalAirportId()", purchaseTopic2.getArrivalAirportId(), equalTo(reserveInfo2.getArrivalAirportId()));
        assertThat("purchaseTopic2.getArrivalTime()", purchaseTopic2.getArrivalTime(), equalTo(reserveInfo2.getArrivalTime()));
        assertNull("purchaseTopic2.getCardNo()", purchaseTopic2.getCardNo());
        assertThat("purchaseTopic2.getDepartureAirportId()", purchaseTopic2.getDepartureAirportId(), equalTo(reserveInfo2.getDepartureAirportId()));
        assertThat("purchaseTopic2.getDepartureDate()", purchaseTopic2.getDepartureDate(), equalTo(reserveInfo2.getDepartureDate()));
        assertThat("purchaseTopic2.getFare()", purchaseTopic2.getFare(), equalTo(reserveInfo2.getFare()));
        assertThat("purchaseTopic2.getFareType()", purchaseTopic2.getFareType(), equalTo(reserveInfo2.getFareType()));
        assertThat("purchaseTopic2.getFlightId()", purchaseTopic2.getFlightId(), equalTo(reserveInfo2.getFlightId()));
        
        count = 0;
        for (PassengerTopic passengerTopic: purchaseTopic2.getPassengers()) {
            PassengerInfo info = new PassengerInfo();
            BeanUtils.copyProperties(passengerTopic, info);
            assertThat("passengerTopic", info, equalTo(reserveInfo2.getPassengers().get(count)));
            count++;
        }

        assertNull(" purchaseTopic2.getPayDateTime()", purchaseTopic2.getPayDateTime());
        assertNull("purchaseTopic2.getPaymentId()", purchaseTopic2.getPaymentId());
        assertThat("purchaseTopic2.getPurchaseId()", purchaseTopic2.getPurchaseId(), equalTo(purchase.getPurchaseId()));
        MemberInfo memberResult = new MemberInfo();
        BeanUtils.copyProperties(purchaseTopic2.getPurchaseMember().get(), memberResult);
        assertThat("PurchaseMember", memberResult, equalTo(member));
        assertThat("purchaseTopic2.getReserveId()", purchaseTopic2.getReserveId(), equalTo(purchase2.getReserveId()));
        assertThat("purchaseTopic2.getSeatClass()", purchaseTopic2.getSeatClass(), equalTo(purchase2.getSeatClass()));
        
        // ③会員：購入者会員情報あり、コード情報あり
        ReserveInfo reserveInfo3 = createReserveInfo(3L);
        Purchase purchase3 = createPurchase(reserveInfo3);
        //firstPassengerTopic = reserveInfo.getPassengers().get(0);
        MemberInfo member3 = createMemberInfo(purchaseTopic.getPassengers().get(0));
        member3.setCard(Optional.of(//
                CardInfo.builder()//
                        .cardNo("1234567890123456")//
                        .cardCompanyCode("VIS")//
                        .cardCompanyName("VISA")//
                        .validTillMonth(12)//
                        .validTillYear(23)//
                        .build()));
        reserveInfo3.setPurchaseMember(Optional.of(member3));
        
        // assert
        PurchaseTopic purchaseTopic3 = purchaseHelper.createPurchaseTopic(reserveInfo3, purchase3);
        System.out.println(purchaseTopic3);
        assertThat("purchaseTopic3.getArrivalAirportId()", purchaseTopic3.getArrivalAirportId(), equalTo(reserveInfo3.getArrivalAirportId()));
        assertThat("purchaseTopic3.getArrivalTime()", purchaseTopic3.getArrivalTime(), equalTo(reserveInfo3.getArrivalTime()));
        assertNull("purchaseTopic3.getCardNo()", purchaseTopic3.getCardNo());
        assertThat("purchaseTopic3.getDepartureAirportId()", purchaseTopic3.getDepartureAirportId(), equalTo(reserveInfo3.getDepartureAirportId()));
        assertThat("purchaseTopic3.getDepartureDate()", purchaseTopic3.getDepartureDate(), equalTo(reserveInfo3.getDepartureDate()));
        assertThat("purchaseTopic3.getFare()", purchaseTopic3.getFare(), equalTo(reserveInfo3.getFare()));
        assertThat("purchaseTopic3.getFareType()", purchaseTopic3.getFareType(), equalTo(reserveInfo3.getFareType()));
        assertThat("purchaseTopic3.getFlightId()", purchaseTopic3.getFlightId(), equalTo(reserveInfo3.getFlightId()));
        
        count = 0;
        for (PassengerTopic passengerTopic: purchaseTopic3.getPassengers()) {
            PassengerInfo info = new PassengerInfo();
            BeanUtils.copyProperties(passengerTopic, info);
            assertThat("passengerTopic", info, equalTo(reserveInfo3.getPassengers().get(count)));
            count++;
        }

        assertNull(" purchaseTopic3.getPayDateTime()", purchaseTopic3.getPayDateTime());
        assertNull("purchaseTopic3.getPaymentId()", purchaseTopic3.getPaymentId());
        assertThat("purchaseTopic3.getPurchaseId()", purchaseTopic3.getPurchaseId(), equalTo(purchase.getPurchaseId()));
        MemberInfo memberResult3 = new MemberInfo();
        CardInfo cardInfo = new CardInfo();
        BeanUtils.copyProperties(purchaseTopic3.getPurchaseMember().get(), memberResult3);
        BeanUtils.copyProperties(purchaseTopic3.getPurchaseMember().get().getCard().get(), cardInfo);
        memberResult3.setCard(Optional.of(cardInfo));
        assertThat("PurchaseMember", memberResult3, equalTo(member3));
        assertThat("purchaseTopic3.getReserveId()", purchaseTopic3.getReserveId(), equalTo(purchase3.getReserveId()));
        assertThat("purchaseTopic3.getSeatClass()", purchaseTopic3.getSeatClass(), equalTo(purchase3.getSeatClass()));
    }

    /**
     * 予約情報を作成します。
     * 
     * @return 予約情報。
     */
    private ReserveInfo createReserveInfo(Long reserveId) {
        // 予約情報
        return ReserveInfo.builder() //
                .reserveId(reserveId) //
                .departureDate(LocalDate.of(2019, 12, 3)) //
                .flightId("NTT001") //
                .departureTime(LocalTime.of(12, 30, 40)) //
                .arrivalTime(LocalTime.of(13, 40, 50)) //
                .departureAirportId("HND") //
                .arrivalAirportId("CTS") //
                .seatClass(SeatClass.N) //
                .fareType("basicFare") //
                .fare(30000) //
                .passengers(Arrays.asList(new PassengerInfo[] //
                { //
                        PassengerInfo.builder().name("渡辺 太郎").age(20).telephoneNo("01012345678")
                                .email("sample@example.com").isMainPassenger(true).build()//
                        ,
                        PassengerInfo.builder().name("搭乗者２").age(22).telephoneNo("").email("").isMainPassenger(false)
                                .build() })) //
                .build();
    }

    // serviceのpurchase生成処理そのまま流用
    private Purchase createPurchase(ReserveInfo reserveInfo) {
        Purchase purchase = new Purchase();
        BeanUtils.copyProperties(reserveInfo, purchase);
        purchase.setPassengerCount(reserveInfo.getPassengers().size());
        purchase.setPurchaseId(999999999L);

        // 会員情報が設定されている場合
        reserveInfo.getPurchaseMember().ifPresent(m -> {
            purchase.setPurchaseMemberId(m.getMemberId());
            m.getCard().ifPresent(c -> { // カード情報が存在する場合、自動決済を行います
                Payment payment = new Payment();
                BeanUtils.copyProperties(m, payment);
                BeanUtils.copyProperties(c, payment);
                payment.setValidTillYear(c.getValidTillYear().toString());
                payment.setValidTillMonth(c.getValidTillMonth().toString());
                payment.setPurchase(purchase);
                payment.setFare(reserveInfo.getFare());
                purchase.setPayment(payment);
            });
        });
        return purchase;
    }

    /**
     * 会員情報を作成します。
     * 
     * @param passengerInfo 搭乗者情報。
     * @return 会員情報。
     */
    private MemberInfo createMemberInfo(PassengerTopic passengerInfo) {
        return MemberInfo.builder() //
                .memberId(1L)//
                .surname("渡辺") //
                .firstName("太郎") //
                .surnameKana("ワタナベ")//
                .firstNameKana("タロウ") //
                .birthday(LocalDate.of(1999, 9, 12)) //
                .gender(Gender.Male)//
                .telephoneNo(passengerInfo.getTelephoneNo())//
                .postalCode("0000000")//
                .address("東京都中央区") //
                .emailId(passengerInfo.getEmail())//
                .build();
    }
}
