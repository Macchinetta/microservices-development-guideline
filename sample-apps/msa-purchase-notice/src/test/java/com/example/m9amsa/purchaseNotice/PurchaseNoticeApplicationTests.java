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
package com.example.m9amsa.purchaseNotice;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.m9amsa.purchaseNotice.config.TestConfig;
import com.example.m9amsa.purchaseNotice.constant.SeatClass;
import com.example.m9amsa.purchaseNotice.entity.Passenger;
import com.example.m9amsa.purchaseNotice.entity.Purchase;
import com.example.m9amsa.purchaseNotice.entity.PurchaseRepository;
import com.example.m9amsa.purchaseNotice.externalmicroservice.model.AirportForEx;
import com.example.m9amsa.purchaseNotice.externalmicroservice.service.FlightExternalMicroServiceWithFallBack;
import com.example.m9amsa.purchaseNotice.model.topic.MemberTopic;
import com.example.m9amsa.purchaseNotice.model.topic.PassengerTopic;
import com.example.m9amsa.purchaseNotice.model.topic.PurchaseTopic;
import com.example.m9amsa.purchaseNotice.model.topic.PurchaseTopicSink;
import com.example.m9amsa.purchaseNotice.service.PurchaseNoticeService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_PURCHASE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
@Import(TestConfig.class)
@AutoConfigureWireMock(port = 9080)
public class PurchaseNoticeApplicationTests {

    @Autowired
    private PurchaseTopicSink purchaseTopicSink;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private PurchaseNoticeService service;

    @MockBean
    private FlightExternalMicroServiceWithFallBack flightExternalMicroServiceWithFallBack;

    @Value("${info.url.root-path}")
    private String urlRoot;

    private String urlBase;

    @Before
    public void setUp() throws Exception {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        MockitoAnnotations.initMocks(this);
        urlBase = "/" + urlRoot + "/purchase-notice";
    }

    /**
     * Kafka consumer
     */
    @Test
    public void testPurchaseInfoListener() {

        // when
        PassengerTopic passengerInfo = PassengerTopic.builder().name("渡辺太郎").age(31).isMainPassenger(true).build();
        List<PassengerTopic> passengers = new ArrayList<PassengerTopic>();
        passengers.add(passengerInfo);
        MemberTopic memberTopic = MemberTopic.builder().emailId("001@ntt.com").build();
        PurchaseTopic purchaseTopic = PurchaseTopic.builder().reserveId(1L).departureDate(LocalDate.of(2019, 5, 7))
                .flightId("NTT01").departureTime(LocalTime.of(10, 05)).arrivalTime(LocalTime.of(13, 05))
                .departureAirportId("HND").arrivalAirportId("PVD").seatClass(SeatClass.N).fareType("片道").fare(13500)
                .passengers(passengers).purchaseMember(Optional.of(memberTopic)).paymentId(1L)
                .cardNo("0000-0000-0000-0000").payDateTime(LocalDateTime.of(2019, 05, 01, 10, 5)).build();

        // do PurchaseTopic
        Map<String, Object> headers = new HashMap<>();
        headers.put("x-payload-class", "Purchase");
        Message<PurchaseTopic> purchaseTopicMessage = new GenericMessage<PurchaseTopic>(purchaseTopic, headers);
        purchaseTopicSink.input().send(purchaseTopicMessage);

        // verify PurchaseTopic
        Optional<Purchase> purchaseResult = purchaseRepository.findById(1L);
        assertThat("受信した予約Idが作成されていること", purchaseResult.get().getReserveId(), equalTo(1L));
        assertThat("受信した出発時刻が作成されていること", purchaseResult.get().getDepartureTime(), equalTo(LocalTime.of(10, 05)));
        assertThat("受信した出発日が作成されていること", purchaseResult.get().getDepartureDate(), equalTo(LocalDate.of(2019, 5, 7)));
        assertThat("受信した便Idが作成されていること", purchaseResult.get().getFlightId(), equalTo("NTT01"));
        assertThat("受信した到着時刻が作成されていること", purchaseResult.get().getArrivalTime(), equalTo(LocalTime.of(13, 05)));
        assertThat("受信した出発空港が作成されていること", purchaseResult.get().getDepartureAirportId(), equalTo("HND"));
        assertThat("受信した到着空港が作成されていること", purchaseResult.get().getArrivalAirportId(), equalTo("PVD"));
        assertThat("受信した搭乗クラス種別が作成されていること", purchaseResult.get().getSeatClass(), equalTo(SeatClass.N));
        assertThat("受信した運賃種別が作成されていること", purchaseResult.get().getFareType(), equalTo("片道"));
        assertThat("受信した運賃が作成されていること", purchaseResult.get().getFare(), equalTo(13500));
        assertThat("受信した搭乗者名前が作成されていること", purchaseResult.get().getPassengers().get(0).getName(), equalTo("渡辺太郎"));
        assertThat("受信した搭乗者年齢が作成されていること", purchaseResult.get().getPassengers().get(0).getAge(), equalTo(31));
        assertThat("受信した代表搭乗者フラグが作成されていること", purchaseResult.get().getPassengers().get(0).isMainPassenger(),
                equalTo(true));
        assertNotNull("受信した搭乗者Idが作成されていること", purchaseResult.get().getPassengers().get(0).getPassengerInfoId());
        assertThat("受信した決済情報が作成されていること", purchaseResult.get().getPaymentId(), equalTo(1L));
        assertThat("受信したカード番号が作成されていること", purchaseResult.get().getCardNo(), equalTo("0000-0000-0000-0000"));
        assertThat("受信した決済情報の購入情報登録日時が作成されていること", purchaseResult.get().getPayDateTime(),
                equalTo(LocalDateTime.of(2019, 05, 01, 10, 5)));
        assertThat("受信したメールアドレスが作成されていること", purchaseResult.get().getEmailId(), equalTo("001@ntt.com"));
    }

    /**
     * purchaseNotice GET
     * 
     * <pre>
     * Correct pattern.
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testPurchaseNoticeCorrect() throws Exception {

        Clock clock = Clock.fixed(Instant.parse("2019-10-31T12:00:00Z"), ZoneId.systemDefault());
        doReturn(clock).when(service).getClock();

        // when
        Passenger passengerInfo = Passenger.builder().name("渡辺太郎").age(31).isMainPassenger(true).build();
        List<Passenger> passengers = new ArrayList<>();
        passengers.add(passengerInfo);

        String departureAirportId = "HND";
        String arrivalAirportId = "KIX";
        // 空港情報Map
        Map<String, String> airportMap = new HashMap<String, String>();
        airportMap.put(departureAirportId, "東京(羽田)");
        airportMap.put(arrivalAirportId, "大阪(関西)");

        Purchase purchase = Purchase.builder().reserveId(1L).departureDate(LocalDate.of(2019, 5, 7)).flightId("NTT01")
                .departureTime(LocalTime.of(10, 05)).arrivalTime(LocalTime.of(13, 05))
                .departureAirportId(departureAirportId).arrivalAirportId(arrivalAirportId).seatClass(SeatClass.N)
                .fareType("片道").fare(13500).passengers(passengers).emailId("0001@ntt.com").paymentId(1L)
                .cardNo("0000-0000-0000-0000").payDateTime(LocalDateTime.of(2019, 05, 01, 10, 5)).build();
        purchaseRepository.saveAndFlush(purchase);

        airportMap.forEach((k, v) -> {
            AirportForEx airport = AirportForEx.builder().id(k).name(v).build();
            when(flightExternalMicroServiceWithFallBack.getAirport(k)).thenReturn(airport);
        });

        // do
        String emailId = "0001@ntt.com";
        mvc.perform(MockMvcRequestBuilders.get(urlBase + "/rss-feed").param("emailId", emailId)
                .accept(MediaType.APPLICATION_RSS_XML).contentType(MediaType.APPLICATION_RSS_XML))
                .andExpect(status().isOk()).andExpect(content().contentType("application/rss+xml;charset=UTF-8"))
                .andExpect(xpath("/rss/@version").string("2.0")).andExpect(xpath("/rss/channel/title").string("M9AREF"))
                .andExpect(xpath("/rss/channel/description").string("購入通知"))
                .andExpect(xpath("/rss/channel/item/title").string("予約Id：1の購入情報"))
                .andExpect(xpath("/rss/channel/item/pubDate")
                        .string(LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm"))))
                .andExpect(xpath("/rss/channel/item/description").string(
                        "ご購入ありがとうございました。予約の詳細メッセージです。\r\n予約Id：1\r\nフライト名：NTT01\r\n出発空港：HND - 東京(羽田)\r\n出発日：2019-05-07\r\n出発時刻：10:05\r\n到着空港：KIX - 大阪(関西)\r\n到着時刻：13:05\r\n搭乗クラス：N\r\n料金タイプ：片道\r\n料金：\\13500\r\n代表搭乗者：\r\n  お名前：渡辺太郎\r\n  年齢：31\r\n決済日時：2019-05-01T10:05\r\nカード番号：0000-0000-0000-0000\r\n"));

    }

    /**
     * purchaseNotice GET
     * 
     * <pre>
     * Error pattern.
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testPurchaseNoticeError() throws Exception {
        // when
        Passenger passengerInfo = Passenger.builder().name("渡辺太郎").age(31).isMainPassenger(true).build();
        List<Passenger> passengers = new ArrayList<>();
        passengers.add(passengerInfo);
        Purchase purchase = Purchase.builder().reserveId(1L).departureDate(LocalDate.of(2019, 5, 7)).flightId("NTT01")
                .departureTime(LocalTime.of(10, 05)).arrivalTime(LocalTime.of(13, 05)).departureAirportId("HND")
                .arrivalAirportId("PVD").seatClass(SeatClass.N).fareType("片道").fare(13500).passengers(passengers)
                .emailId("0001@ntt.com").paymentId(1L).cardNo("0000-0000-0000-0000")
                .payDateTime(LocalDateTime.of(2019, 05, 01, 10, 5)).build();
        purchaseRepository.saveAndFlush(purchase);

        // do
        String errorEmailId = "00";
        mvc.perform(MockMvcRequestBuilders.get(urlBase + "/rss-feed").param("emailId", errorEmailId)
                .accept(MediaType.APPLICATION_RSS_XML).contentType(MediaType.APPLICATION_RSS_XML))
                .andExpect(status().isOk()).andExpect(content().contentType("application/rss+xml;charset=UTF-8"))
                .andExpect(content().string(
                        "<rss version=\"2.0\"><channel><title>M9AREF</title><description>購入通知</description></channel></rss>"));
    }

    /**
     * purchaseNotice GET
     * 
     * <pre>
     * Error pattern.
     * - Http Status 400
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testPurchaseNotice400() throws Exception {
        // when

        Purchase purchase = Purchase.builder().reserveId(1L).build();
        purchaseRepository.saveAndFlush(purchase);

        // do
        doThrow(new RuntimeException()).when(service).purchaseNotice(null);

        String errorEmailId = null;
        mvc.perform(MockMvcRequestBuilders.get(urlBase + "/rss-feed").param("emailId", errorEmailId)
                .accept(MediaType.APPLICATION_RSS_XML).contentType(MediaType.APPLICATION_RSS_XML))
                .andExpect(status().is(400));
    }

    /**
     * purchaseNotice GET
     * 
     * <pre>
     * Error pattern.
     * - Http Status 500
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testPurchaseNotice500() throws Exception {
        // when

        Purchase purchase = Purchase.builder().reserveId(1L).build();
        purchaseRepository.saveAndFlush(purchase);

        // do
        doThrow(new IOException()).when(service).purchaseNotice("0001@ntt.com");

        String errorEmailId = "0001@ntt.com";
        mvc.perform(MockMvcRequestBuilders.get(urlBase + "/rss-feed").param("emailId", errorEmailId)
                .accept(MediaType.APPLICATION_RSS_XML).contentType(MediaType.APPLICATION_RSS_XML))
                .andExpect(status().is(500));
    }
}
