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
package com.example.m9amsa.reserveNotice;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
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
import org.mockito.Mock;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.esotericsoftware.minlog.Log;
import com.example.m9amsa.reserveNotice.config.TestConfig;
import com.example.m9amsa.reserveNotice.constant.SeatClass;
import com.example.m9amsa.reserveNotice.entity.BaseClock;
import com.example.m9amsa.reserveNotice.entity.Passenger;
import com.example.m9amsa.reserveNotice.entity.Reservation;
import com.example.m9amsa.reserveNotice.entity.ReservationRepository;
import com.example.m9amsa.reserveNotice.externalmicroservice.model.AirportForEx;
import com.example.m9amsa.reserveNotice.externalmicroservice.service.FlightExternalMicroService;
import com.example.m9amsa.reserveNotice.externalmicroservice.service.FlightExternalMicroServiceWithFallBack;
import com.example.m9amsa.reserveNotice.model.topic.PassengerTopic;
import com.example.m9amsa.reserveNotice.model.topic.ReservationTopic;
import com.example.m9amsa.reserveNotice.model.topic.ReservationTopicSink;
import com.example.m9amsa.reserveNotice.service.ReserveNoticeService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_RESERVE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
@Import(TestConfig.class)
@AutoConfigureWireMock(port = 9080)
public class ReserveNoticeApplicationTests {

    @Autowired
    private ReservationTopicSink reservationTopicSink;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private ReserveNoticeService service;

    @MockBean
    private FlightExternalMicroServiceWithFallBack flightExternalMicroServiceWithFallBack;

    @MockBean
    private FlightExternalMicroService flightExternalMicroService;

    @Mock
    private BaseClock baseClock;

    @Value("${info.url.root-path}")
    private String urlRoot;

    private String urlBase;

    @Before
    public void setUp() throws Exception {
        reservationRepository.deleteAll();
        reservationRepository.flush();

        MockitoAnnotations.initMocks(this);
        urlBase = "/" + urlRoot + "/reserve-notice";

        reset(baseClock);
    }

    /**
     * Kafka consumer
     */
    @Test
    public void testReserveInfoListener() {
        // when
        PassengerTopic passengerInfo1 = PassengerTopic.builder().name("渡辺太郎").age(31).isMainPassenger(true)
                .email("001@ntt.com").telephoneNo("123456789").build();
        PassengerTopic passengerInfo2 = PassengerTopic.builder().name("渡辺花子").age(31).isMainPassenger(false).build();

        List<PassengerTopic> passengers = new ArrayList<>();
        passengers.add(passengerInfo1);
        passengers.add(passengerInfo2);

        ReservationTopic reservation = ReservationTopic.builder().reserveId(1L).departureDate(LocalDate.of(2019, 5, 7))
                .flightId("NTT01").departureTime(LocalTime.of(10, 05)).arrivalTime(LocalTime.of(13, 05))
                .departureAirportId("HND").arrivalAirportId("ITM").seatClass(SeatClass.N).fareType("片道").fare(13500)
                .passenger(passengers).build();

        // do ReserveInfo
        Map<String, Object> headers = new HashMap<>();
        headers.put("x-payload-class", "Reserve");
        Message<ReservationTopic> reserveInfoMessage = new GenericMessage<ReservationTopic>(reservation, headers);
        reservationTopicSink.input().send(reserveInfoMessage);

        // verify ReserveInfo
        Optional<Reservation> result = reservationRepository.findById(1L);
        assertThat("予約Idが作成されていること", result.get().getReserveId(), equalTo(1L));
        assertThat("出発時刻が作成されていること", result.get().getDepartureTime(), equalTo(LocalTime.of(10, 05)));
        assertThat("出発日が作成されていること", result.get().getDepartureDate(), equalTo(LocalDate.of(2019, 5, 7)));
        assertThat("便Idが作成されていること", result.get().getFlightId(), equalTo("NTT01"));
        assertThat("到着時刻が作成されていること", result.get().getArrivalTime(), equalTo(LocalTime.of(13, 05)));
        assertThat("出発空港が作成されていること", result.get().getDepartureAirportId(), equalTo("HND"));
        assertThat("到着空港が作成されていること", result.get().getArrivalAirportId(), equalTo("ITM"));
        assertThat("搭乗クラス種別が作成されていること", result.get().getSeatClass(), equalTo(SeatClass.N));
        assertThat("運賃種別が作成されていること", result.get().getFareType(), equalTo("片道"));
        assertThat("運賃が作成されていること", result.get().getFare(), equalTo(13500));
        assertThat("搭乗者名前が作成されていること", result.get().getPassengers().get(0).getName(), equalTo("渡辺太郎"));
        assertThat("搭乗者年齢が作成されていること", result.get().getPassengers().get(0).getAge(), equalTo(31));
        assertThat("電話番号が作成されていること", result.get().getPassengers().get(0).getTelephoneNo(), equalTo("123456789"));
        assertThat("代表搭乗者フラグが作成されていること", result.get().getPassengers().get(0).isMainPassenger(), equalTo(true));
        assertNotNull("搭乗者Idが作成されていること", result.get().getPassengers().get(0).getPassengerInfoId());
        assertThat("搭乗者名前が作成されていること", result.get().getPassengers().get(1).getName(), equalTo("渡辺花子"));
        assertThat("搭乗者年齢が作成されていること", result.get().getPassengers().get(1).getAge(), equalTo(31));
        assertThat("同時搭乗者フラグが作成されていること", result.get().getPassengers().get(1).isMainPassenger(), equalTo(false));
        assertNotNull("搭乗者Idが作成されていること", result.get().getPassengers().get(1).getPassengerInfoId());
        assertThat("メールアドレスが作成されていること", result.get().getEmailId(), equalTo("001@ntt.com"));

    }

    /**
     * reserveNotice GET
     * 
     * <pre>
     * Correct pattern.
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testReserveNoticeCorrect() throws Exception {

        Clock clock = Clock.fixed(Instant.parse("2019-10-31T12:00:00Z"), ZoneId.systemDefault());
        doReturn(clock).when(service).getClock();

        // when
        Passenger passengerInfo1 = Passenger.builder().name("渡辺太郎").age(31).isMainPassenger(true).build();
        Passenger passengerInfo2 = Passenger.builder().name("渡辺花子").age(31).isMainPassenger(false).build();

        List<Passenger> passengers = new ArrayList<>();
        passengers.add(passengerInfo1);
        passengers.add(passengerInfo2);

        String departureAirportId = "HND";
        String arrivalAirportId = "KIX";
        // 空港情報Map
        Map<String, String> airportMap = new HashMap<String, String>();
        airportMap.put(departureAirportId, "東京(羽田)");
        airportMap.put(arrivalAirportId, "大阪(関西)");

        Reservation reservation = Reservation.builder().reserveId(1L).departureDate(LocalDate.of(2019, 5, 7))
                .flightId("NTT01").departureTime(LocalTime.of(10, 05)).arrivalTime(LocalTime.of(13, 05))
                .departureAirportId(departureAirportId).arrivalAirportId(arrivalAirportId).seatClass(SeatClass.N)
                .fareType("片道").fare(13500).passengers(passengers).emailId("0001@ntt.com").build();

        reservationRepository.saveAndFlush(reservation);

        airportMap.forEach((k, v) -> {
            AirportForEx airport = AirportForEx.builder().id(k).name(v).build();
            doReturn(airport).when(flightExternalMicroServiceWithFallBack).getAirport(k);
        });

        // do
        String emailId = "0001@ntt.com";
        mvc.perform(MockMvcRequestBuilders.get(urlBase + "/rss-feed").param("emailId", emailId)
                .accept(MediaType.APPLICATION_RSS_XML).contentType(MediaType.APPLICATION_RSS_XML))
                .andExpect(status().isOk()).andExpect(content().contentType("application/rss+xml;charset=UTF-8"))
                .andExpect(xpath("/rss/@version").string("2.0")).andExpect(xpath("/rss/channel/title").string("M9AREF"))
                .andExpect(xpath("/rss/channel/description").string("予約完了通知"))
                .andExpect(xpath("/rss/channel/item/title").string("予約Id：1の予約情報"))
                .andExpect(xpath("/rss/channel/item/pubDate")
                        .string(LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm"))))
                .andExpect(xpath("/rss/channel/item/description").string(
                        "ご予約ありがとうございました。予約の詳細メッセージです。\r\n予約Id：1\r\nフライト名：NTT01\r\n出発空港：HND - 東京(羽田)\r\n出発日：2019-05-07\r\n出発時刻：10:05\r\n到着空港：KIX - 大阪(関西)\r\n到着時刻：13:05\r\n搭乗クラス：N\r\n料金タイプ：片道\r\n料金：\\13500\r\n代表搭乗者：\r\n  お名前：渡辺太郎\r\n  年齢：31\r\n同時搭乗者：\r\n  お名前：渡辺花子\r\n  年齢：31\r\n"));

    }

    /**
     * reserveNotice GET
     * 
     * <pre>
     * Error pattern.
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testReserveNoticeError() throws Exception {
        // when
        Passenger passengerInfo1 = Passenger.builder().name("渡辺太郎").age(31).isMainPassenger(true).build();
        Passenger passengerInfo2 = Passenger.builder().name("渡辺花子").age(31).isMainPassenger(false).build();

        List<Passenger> passengers = new ArrayList<>();
        passengers.add(passengerInfo1);
        passengers.add(passengerInfo2);

        Reservation reservation = Reservation.builder().reserveId(1L).departureDate(LocalDate.of(2019, 5, 7))
                .flightId("NTT01").departureTime(LocalTime.of(10, 05)).arrivalTime(LocalTime.of(13, 05))
                .departureAirportId("HND").arrivalAirportId("ITM").seatClass(SeatClass.N).fareType("片道").fare(13500)
                .passengers(passengers).emailId("0001@ntt.com").build();

        reservationRepository.saveAndFlush(reservation);

        // do
        String errorEmailId = "00";
        mvc.perform(MockMvcRequestBuilders.get(urlBase + "/rss-feed").param("emailId", errorEmailId)
                .accept(MediaType.APPLICATION_RSS_XML).contentType(MediaType.APPLICATION_RSS_XML))
                .andExpect(status().isOk()).andExpect(content().contentType("application/rss+xml;charset=UTF-8"))
                .andExpect(content().string(
                        "<rss version=\"2.0\"><channel><title>M9AREF</title><description>予約完了通知</description></channel></rss>"));
    }

    /**
     * reserveNotice GET
     * 
     * <pre>
     * Error pattern.
     * - Http Status 400
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testReserveNotice400() throws Exception {
        // when

        Reservation reservation = Reservation.builder().reserveId(1L).build();
        reservationRepository.saveAndFlush(reservation);

        // do
        doThrow(new RuntimeException()).when(service).getReserveCompleteNotice(null);

        String errorEmailId = null;
        mvc.perform(MockMvcRequestBuilders.get(urlBase + "/rss-feed").param("emailId", errorEmailId)
                .accept(MediaType.APPLICATION_RSS_XML).contentType(MediaType.APPLICATION_RSS_XML))
                .andExpect(status().is(400));
    }

    /**
     * reserveNotice GET
     * 
     * <pre>
     * Error pattern.
     * - Http Status 500
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testReserveNotice500() throws Exception {
        // when

        Reservation reservation = Reservation.builder().reserveId(1L).build();
        reservationRepository.saveAndFlush(reservation);

        // do
        doThrow(new IOException()).when(service).getReserveCompleteNotice("0001@ntt.com");

        String errorEmailId = "0001@ntt.com";
        MvcResult mvcResult = mvc
                .perform(MockMvcRequestBuilders.get(urlBase + "/rss-feed").param("emailId", errorEmailId)
                        .accept(MediaType.APPLICATION_RSS_XML).contentType(MediaType.APPLICATION_RSS_XML))
                .andExpect(status().is(500)).andReturn();
        Log.info(mvcResult.getResponse().getContentAsString());
    }
}
