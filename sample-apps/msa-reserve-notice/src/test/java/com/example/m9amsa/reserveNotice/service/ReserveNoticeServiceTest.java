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
package com.example.m9amsa.reserveNotice.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.xml.HasXPath.hasXPath;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.example.m9amsa.reserveNotice.constant.SeatClass;
import com.example.m9amsa.reserveNotice.entity.BaseClock;
import com.example.m9amsa.reserveNotice.entity.Passenger;
import com.example.m9amsa.reserveNotice.entity.Reservation;
import com.example.m9amsa.reserveNotice.entity.ReservationRepository;
import com.example.m9amsa.reserveNotice.externalmicroservice.model.AirportForEx;
import com.example.m9amsa.reserveNotice.externalmicroservice.service.FlightExternalMicroService;
import com.example.m9amsa.reserveNotice.model.RssRoot;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import feign.FeignException;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_RESERVE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
public class ReserveNoticeServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @SpyBean
    private ReserveNoticeService reserveNoticeService;

    @MockBean
    private FlightExternalMicroService flightExternalMicroService;

    @Mock
    private BaseClock baseClock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        reservationRepository.deleteAll();
        reservationRepository.flush();

        reset(baseClock);
    }

    @After
    public void tearDown() throws Exception {
        MockitoAnnotations.initMocks(this);
        reservationRepository.deleteAll();
        reservationRepository.flush();
    }

    @Test
    public void testReserveNotice() throws Exception {

        Clock clock = Clock.fixed(Instant.parse("2019-10-31T12:00:00Z"), ZoneId.systemDefault());
        doReturn(clock).when(reserveNoticeService).getClock();

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
            when(flightExternalMicroService.getAirport(k)).thenReturn(airport);
        });

        // メールアドレスが正しい場合
        RssRoot rssRoot = reserveNoticeService.getReserveCompleteNotice("0001@ntt.com");
        DocumentBuilder xmlBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        Document rssRootAsXml = xmlBuilder.parse(new InputSource(
                new ByteArrayInputStream(new XmlMapper().writeValueAsString(rssRoot).getBytes("utf-8"))));

        assertThat("rssのversionを確認する", rssRootAsXml, hasXPath("/rss/@version", equalTo("2.0")));
        assertThat("channelのtitleを確認する", rssRootAsXml, hasXPath("/rss/channel/title", equalTo("M9AREF")));
        assertThat("channelのdescriptionを確認する", rssRootAsXml, hasXPath("/rss/channel/description", equalTo("予約完了通知")));
        assertThat("itemのtitleを確認する", rssRootAsXml, hasXPath("/rss/channel/item/title", equalTo("予約Id：1の予約情報")));
        assertThat("itemのpubDateを確認する", rssRootAsXml, hasXPath("/rss/channel/item/pubDate",
                equalTo(LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm")))));
        assertThat("itemのdescriptionを確認する", rssRootAsXml, hasXPath("/rss/channel/item/description", equalTo(
                "ご予約ありがとうございました。予約の詳細メッセージです。\r\n予約Id：1\r\nフライト名：NTT01\r\n出発空港：HND - 東京(羽田)\r\n出発日：2019-05-07\r\n出発時刻：10:05\r\n到着空港：KIX - 大阪(関西)\r\n到着時刻：13:05\r\n搭乗クラス：N\r\n料金タイプ：片道\r\n料金：\\13500\r\n代表搭乗者：\r\n  お名前：渡辺太郎\r\n  年齢：31\r\n同時搭乗者：\r\n  お名前：渡辺花子\r\n  年齢：31\r\n")));
        // メールアドレスが正しくない場合
        rssRoot = reserveNoticeService.getReserveCompleteNotice("0001@ntt.com");
        assertThat("rssのversionを確認する", rssRootAsXml, hasXPath("/rss/@version", equalTo("2.0")));
        assertThat("channelのtitleを確認する", rssRootAsXml, hasXPath("/rss/channel/title", equalTo("M9AREF")));
        assertThat("channelのdescriptionを確認する", rssRootAsXml, hasXPath("/rss/channel/description", equalTo("予約完了通知")));

        AirportForEx airport = AirportForEx.builder().id(departureAirportId).name(airportMap.get(departureAirportId)).build();

        when(flightExternalMicroService.getAirport(departureAirportId)).thenReturn(airport);
        // 到着空港情報が取得できない場合
        airportMap.put(arrivalAirportId, "");
        doThrow(FeignException.InternalServerError.class).when(flightExternalMicroService).getAirport(arrivalAirportId);

        rssRoot = reserveNoticeService.getReserveCompleteNotice("0001@ntt.com");
        assertThat("rssのversionを確認する", rssRootAsXml, hasXPath("/rss/@version", equalTo("2.0")));
        assertThat("channelのtitleを確認する", rssRootAsXml, hasXPath("/rss/channel/title", equalTo("M9AREF")));
        assertThat("channelのdescriptionを確認する", rssRootAsXml, hasXPath("/rss/channel/description", equalTo("予約完了通知")));
        assertThat("itemのtitleを確認する", rssRootAsXml, hasXPath("/rss/channel/item/title", equalTo("予約Id：1の予約情報")));
        assertThat("itemのpubDateを確認する", rssRootAsXml, hasXPath("/rss/channel/item/pubDate",
                equalTo(LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm")))));
        assertThat("itemのdescriptionを確認する", rssRootAsXml, hasXPath("/rss/channel/item/description", equalTo(
                "ご予約ありがとうございました。予約の詳細メッセージです。\r\n予約Id：1\r\nフライト名：NTT01\r\n出発空港：HND - 東京(羽田)\r\n出発日：2019-05-07\r\n出発時刻：10:05\r\n到着空港：KIX - 大阪(関西)\r\n到着時刻：13:05\r\n搭乗クラス：N\r\n料金タイプ：片道\r\n料金：\\13500\r\n代表搭乗者：\r\n  お名前：渡辺太郎\r\n  年齢：31\r\n同時搭乗者：\r\n  お名前：渡辺花子\r\n  年齢：31\r\n")));

    }

}
