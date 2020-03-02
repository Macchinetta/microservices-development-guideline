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

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.purchase.constant.Gender;
import com.example.m9amsa.purchase.constant.SeatClass;
import com.example.m9amsa.purchase.entity.Payment;
import com.example.m9amsa.purchase.entity.Purchase;
import com.example.m9amsa.purchase.entity.PurchaseRepository;
import com.example.m9amsa.purchase.exception.HttpStatus401Exception;
import com.example.m9amsa.purchase.model.CardInfo;
import com.example.m9amsa.purchase.model.MemberInfo;
import com.example.m9amsa.purchase.model.PassengerInfo;
import com.example.m9amsa.purchase.model.ReserveInfo;
import com.example.m9amsa.purchase.model.topic.PurchaseTopic;
import com.example.m9amsa.purchase.model.topic.PurchaseTopicSource;
import com.example.m9amsa.purchase.service.DeletePurchaseService;
import com.example.m9amsa.purchase.service.FindPurchaseService;
import com.example.m9amsa.purchase.service.RegisterPurchaseService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * PurchaseControllerのテストケース。
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_PURCHASE=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class PurchaseControllerTest {

    /**
     * 購入コントローラー。
     */
    @Autowired
    private PurchaseController purchaseController;

    /**
     * 購入情報登録サービス。
     */
    @SpyBean
    private RegisterPurchaseService registerPurchaseService;

    /**
     * 購入情報削除サービス。
     */
    @SpyBean
    private DeletePurchaseService deletePurchaseService;

    /**
     * 購入状況照会サービス。
     */
    @SpyBean
    private FindPurchaseService findPurchaseService;

    /**
     * モデル変換ヘルパー
     */
    @Autowired
    private PurchaseHelper purchaseHelper;

    /**
     * 購入トピック。
     */
    @Autowired
    private PurchaseTopicSource purchaseTopic;

    /**
     * 購入情報のArgument。
     */
    @Captor
    private ArgumentCaptor<ReserveInfo> reserveInfoCaptor;

    /**
     * 購入情報削除処理のArgument。
     */
    @Captor
    private ArgumentCaptor<Long> reserveIdCaptor;

    /**
     * 予約IdのArgument。
     */
    @Captor
    private ArgumentCaptor<Long> longerCaptor;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private ObjectMapper jsonMapper;

    @Mock
    private OAuth2Authentication authenticationMock;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);

        reset(registerPurchaseService);
        reset(findPurchaseService);

        purchaseRepository.deleteAll();

    }

    /**
     * registerPurchaseのテスト。
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRegisterPurchase() throws Exception {

        ReserveInfo reserveInfo = createReserveInfo();
        PassengerInfo firstPassengerInfo = reserveInfo.getPassengers().get(0);
        MemberInfo member = createMemberInfo(firstPassengerInfo);
        member.setCard(Optional.of(//
                CardInfo.builder()//
                        .cardNo("1234567890123456")//
                        .cardCompanyCode("VIS")//
                        .cardCompanyName("VISA")//
                        .validTillMonth(12)//
                        .validTillYear(23)//
                        .build()));
        reserveInfo.setPurchaseMember(Optional.of(member));

        Payment payment = Payment.builder().build();
        BeanUtils.copyProperties(reserveInfo, payment);
        BeanUtils.copyProperties(reserveInfo.getPurchaseMember().get(), payment);
        BeanUtils.copyProperties(reserveInfo.getPurchaseMember().get().getCard().get(), payment);

        Purchase purchase = Purchase.builder().build();
        BeanUtils.copyProperties(reserveInfo, purchase);
        purchase.setPayment(payment);

        Purchase purchaseInfo = Purchase.builder().build();
        BeanUtils.copyProperties(purchase, purchaseInfo);

        doReturn(purchaseInfo).when(registerPurchaseService).registerPurchase(reserveInfo);

        purchaseController.registerPurchase(reserveInfo);

        verify(registerPurchaseService, times(1)).registerPurchase(reserveInfoCaptor.capture());

        assertThat("購入サービスへ渡しているパラメータが正しいこと", reserveInfoCaptor.getValue(), equalTo(reserveInfo));

        Message<String> sendMessage = (Message<String>) messageCollector.forChannel(purchaseTopic.output()).poll();

        assertFalse("トピックヘッダに'x-payload-class'が設定されていないこと", sendMessage.getHeaders().containsKey("x-payload-class"));

        // helperで生成したtopicと内容が一致すること
        PurchaseTopic expectTpic = purchaseHelper.createPurchaseTopic(reserveInfo, purchaseInfo);
        JSONAssert.assertEquals("トピックのメッセージが正しいこと", jsonMapper.writeValueAsString(expectTpic),
                sendMessage.getPayload(), false);
    }

    /**
     * deletePurchaseのテスト。
     */
    @Test
    public void testDeletePurchase() throws Exception {

        doNothing().when(deletePurchaseService).deleteByReserveId(1L);

        purchaseController.deleteByReserveId(1L);

        verify(deletePurchaseService, times(1)).deleteByReserveId(reserveIdCaptor.capture());

        assertThat("購入サービスへ渡しているパラメータが正しいこと", reserveIdCaptor.getValue(), equalTo(1L));
    }

    /**
     * findPurchaseのテスト。
     */
    @Test
    public void testFindPurchase() throws Exception {

        ReserveInfo reserveInfo = createReserveInfo();
        PassengerInfo firstPassengerInfo = reserveInfo.getPassengers().get(0);
        MemberInfo member = createMemberInfo(firstPassengerInfo);
        member.setCard(Optional.of(//
                CardInfo.builder()//
                        .cardNo("1234567890123456")//
                        .cardCompanyCode("VIS")//
                        .cardCompanyName("VISA")//
                        .validTillMonth(12)//
                        .validTillYear(23)//
                        .build()));
        reserveInfo.setPurchaseMember(Optional.of(member));

        Payment payment = Payment.builder().build();
        BeanUtils.copyProperties(reserveInfo, payment);
        BeanUtils.copyProperties(reserveInfo.getPurchaseMember().get(), payment);
        BeanUtils.copyProperties(reserveInfo.getPurchaseMember().get().getCard().get(), payment);
        registerPurchaseService.registerPurchase(reserveInfo);

        Long reserveId = 1L;
        Long purchaseId = 2L;
        Long paymentId = 3L;
        Long purchaseMemberId = 4L;
        payment = Payment.builder() //
                .paymentId(paymentId) //
                .payDateTime(LocalDateTime.now()) //
                .fare(30000) //
                .surname("渡辺") //
                .firstName("太郎") //
                .surnameKana("ワタナベ")//
                .firstNameKana("タロウ") //
                .emailId("sample@example.com") //
                .cardNo("1234567890123456")//
                .cardCompanyCode("VIS")//
                .cardCompanyName("VISA")//
                .validTillMonth("12")//
                .validTillYear("23")//
                .build();

        Purchase purchase = Purchase.builder() //
                .purchaseId(purchaseId) //
                .reserveId(reserveId) //
                .purchaseMemberId(purchaseMemberId) //
                .departureDate(LocalDate.now()) //
                .flightId("NTT001") //
                .departureTime(LocalTime.now()) //
                .arrivalTime(LocalTime.now()) //
                .departureAirportId("HND") //
                .arrivalAirportId("CTS") //
                .seatClass(SeatClass.N) //
                .fareType("basicFare") //
                .fare(30000) //
                .passengerCount(3) //
                .payDateTime(LocalDateTime.now()) //
                .payment(payment) //
                .build();

        when(findPurchaseService.findPurchase(reserveId)).thenReturn(Optional.of(purchase));

        when(authenticationMock.getName()).thenReturn(purchase.getPurchaseMemberId().toString());

        purchaseController.findPurchase(authenticationMock, reserveId);

        verify(findPurchaseService, times(1)).findPurchase(longerCaptor.capture());

        assertThat("購入状況照会サービスへ渡しているパラメータが正しいこと", longerCaptor.getValue(), equalTo(reserveId));
    }

    /**
     * findPurchaseのテスト。
     * 
     * <pre>
     * 認証済み会員IDと購入時会員IDが違う場合、認証エラーとします。
     * </pre>
     */
    @Test
    public void testFindPurchaseError() throws Exception {

        ReserveInfo reserveInfo = createReserveInfo();
        PassengerInfo firstPassengerInfo = reserveInfo.getPassengers().get(0);
        MemberInfo member = createMemberInfo(firstPassengerInfo);
        member.setCard(Optional.of(//
                CardInfo.builder()//
                        .cardNo("1234567890123456")//
                        .cardCompanyCode("VIS")//
                        .cardCompanyName("VISA")//
                        .validTillMonth(12)//
                        .validTillYear(23)//
                        .build()));
        reserveInfo.setPurchaseMember(Optional.of(member));

        Payment payment = Payment.builder().build();
        BeanUtils.copyProperties(reserveInfo, payment);
        BeanUtils.copyProperties(reserveInfo.getPurchaseMember().get(), payment);
        BeanUtils.copyProperties(reserveInfo.getPurchaseMember().get().getCard().get(), payment);
        registerPurchaseService.registerPurchase(reserveInfo);

        Long reserveId = 1L;
        Long purchaseId = 2L;
        Long paymentId = 3L;
        Long purchaseMemberId = 4L;
        payment = Payment.builder() //
                .paymentId(paymentId) //
                .payDateTime(LocalDateTime.now()) //
                .fare(30000) //
                .surname("渡辺") //
                .firstName("太郎") //
                .surnameKana("ワタナベ")//
                .firstNameKana("タロウ") //
                .emailId("sample@example.com") //
                .cardNo("1234567890123456")//
                .cardCompanyCode("VIS")//
                .cardCompanyName("VISA")//
                .validTillMonth("12")//
                .validTillYear("23")//
                .build();

        Purchase purchase = Purchase.builder() //
                .purchaseId(purchaseId) //
                .reserveId(reserveId) //
                .purchaseMemberId(purchaseMemberId) //
                .departureDate(LocalDate.now()) //
                .flightId("NTT001") //
                .departureTime(LocalTime.now()) //
                .arrivalTime(LocalTime.now()) //
                .departureAirportId("HND") //
                .arrivalAirportId("CTS") //
                .seatClass(SeatClass.N) //
                .fareType("basicFare") //
                .fare(30000) //
                .passengerCount(3) //
                .payDateTime(LocalDateTime.now()) //
                .payment(payment) //
                .build();

        when(findPurchaseService.findPurchase(reserveId)).thenReturn(Optional.of(purchase));

        Long badPurchaseMemberId = 5L;
        when(authenticationMock.getName()).thenReturn(badPurchaseMemberId.toString());

        RuntimeException e = catchThrowableOfType(() -> {
            purchaseController.findPurchase(authenticationMock, reserveId);
        }, HttpStatus401Exception.class);

        assertNotNull("HttpStatus401Exception例外が発生すること", e);
    }

    /**
     * 予約情報を作成します。
     * 
     * @return 予約情報。
     */
    private ReserveInfo createReserveInfo() {
        // 予約情報
        Long reserveId = 1L;
        return ReserveInfo.builder() //
                .reserveId(reserveId) //
                .departureDate(LocalDate.now()) //
                .flightId("NTT001") //
                .departureTime(LocalTime.now()) //
                .arrivalTime(LocalTime.now()) //
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
                .purchaseMember(Optional.empty()) //
                .build();
    }

    /**
     * 会員情報を作成します。
     * 
     * @param passengerInfo 搭乗者情報。
     * @return 会員情報。
     */
    private MemberInfo createMemberInfo(PassengerInfo passengerInfo) {
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
                .card(Optional.empty())//
                .build();
    }

}
