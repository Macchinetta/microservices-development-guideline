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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.purchase.constant.Gender;
import com.example.m9amsa.purchase.constant.SeatClass;
import com.example.m9amsa.purchase.entity.BaseClock;
import com.example.m9amsa.purchase.entity.Purchase;
import com.example.m9amsa.purchase.entity.PurchaseRepository;
import com.example.m9amsa.purchase.model.CardInfo;
import com.example.m9amsa.purchase.model.MemberInfo;
import com.example.m9amsa.purchase.model.PassengerInfo;
import com.example.m9amsa.purchase.model.ReserveInfo;
import com.example.m9amsa.purchase.model.topic.PassengerTopic;

import lombok.extern.slf4j.Slf4j;

/**
 * 購入情報登録サービスのテストクラス。
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_PURCHASE=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
@Slf4j
public class RegisterPurchaseServiceTest {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private RegisterPurchaseService registerPurchaseService;

    @SpyBean
    private BaseClock baseClock;

    @Autowired
    private EntityManager entityManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        reset(baseClock);

        entityManager.clear();
    }

    /**
     * Test for registerPurchase()。
     * 
     * <pre>
     *  正常系テスト。
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    @Transactional
    public void testRegisterPurchaseCorrect() throws Exception {

        // 現在日付を変更
        Clock clock = Clock.fixed(Instant.parse("2019-01-01T00:00:00Z"), ZoneId.systemDefault());
        setClock(clock);

        purchaseRepository.deleteAll();

        // ①非会員：購入者会員情報なし
        // 予約情報を作成します
        ReserveInfo reserveInfo = createReserveInfo(1L);

        Purchase purchaseTopic = registerPurchaseService.registerPurchase(reserveInfo);
        log.info("購入情報が登録されました。{}", purchaseTopic.getPurchaseId());

        assertThat("購入情報が作成されていること", purchaseTopic.getPurchaseId(), greaterThan(0L));
        assertThat("購入情報の購入情報登録日時が正しいこと。", purchaseTopic.getPayDateTime(), equalTo(LocalDateTime.now(clock)));

        // ②会員：購入者会員情報あり、コード情報なし
        reserveInfo = createReserveInfo(2L);
        PassengerTopic firstPassengerTopic = new PassengerTopic();
        BeanUtils.copyProperties(reserveInfo.getPassengers().get(0), firstPassengerTopic);
        MemberInfo member = createMemberInfo(firstPassengerTopic);
        reserveInfo.setPurchaseMember(Optional.of(member));

        purchaseTopic = registerPurchaseService.registerPurchase(reserveInfo);
        log.info("購入情報が登録されました。{}", purchaseTopic.getPurchaseId());

        assertThat("購入情報が作成されていること", purchaseTopic.getPurchaseId(), greaterThan(0L));

        // ③会員：購入者会員情報あり、コード情報あり
        reserveInfo = createReserveInfo(3L);
        member = createMemberInfo(firstPassengerTopic);
        member.setCard(Optional.of(//
                CardInfo.builder()//
                        .cardNo("1234567890123456")//
                        .cardCompanyCode("VIS")//
                        .cardCompanyName("VISA")//
                        .validTillMonth(12)//
                        .validTillYear(23)//
                        .build()));
        reserveInfo.setPurchaseMember(Optional.of(member));

        purchaseTopic = registerPurchaseService.registerPurchase(reserveInfo);
        log.info("購入情報が登録されました。{}", purchaseTopic.getPurchaseId());
        log.info("購入情報。{}", purchaseTopic);

        assertThat("購入情報が作成されていること", purchaseTopic.getPurchaseId(), greaterThan(0L));

        // 各項目チェック
        assertNotNull("予約Idが作成されていること", purchaseTopic.getReserveId());
        assertNotNull("出発日が作成されていること", purchaseTopic.getDepartureDate());
        assertNotNull("便Idが作成されていること", purchaseTopic.getFlightId());
        assertNotNull("出発時刻が作成されていること", purchaseTopic.getDepartureTime());
        assertNotNull("到着時刻が作成されていること", purchaseTopic.getArrivalTime());
        assertNotNull("出発空港が作成されていること", purchaseTopic.getDepartureAirportId());
        assertNotNull("到着空港が作成されていること", purchaseTopic.getArrivalAirportId());
        assertNotNull("搭乗クラス種別が作成されていること", purchaseTopic.getSeatClass());
        assertNotNull("運賃種別が作成されていること", purchaseTopic.getFareType());
        assertThat("決済情報の購入情報登録日時が正しいこと。", purchaseTopic.getPayDateTime(), equalTo(LocalDateTime.now(clock)));
    }

    private void setClock(Clock clock) throws Exception {
        doReturn(clock).when(baseClock).systemDefaultZone();
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
                .build();
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
