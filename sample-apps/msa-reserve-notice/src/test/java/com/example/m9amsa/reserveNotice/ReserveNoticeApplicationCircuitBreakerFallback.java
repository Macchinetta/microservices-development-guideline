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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.lang.reflect.Constructor;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.ReflectionUtils;

import com.example.m9amsa.reserveNotice.constant.SeatClass;
import com.example.m9amsa.reserveNotice.entity.BaseClock;
import com.example.m9amsa.reserveNotice.entity.Passenger;
import com.example.m9amsa.reserveNotice.entity.Reservation;
import com.example.m9amsa.reserveNotice.entity.ReservationRepository;
import com.example.m9amsa.reserveNotice.externalmicroservice.service.FlightExternalMicroService;
import com.example.m9amsa.reserveNotice.service.ReserveNoticeService;

import feign.FeignException;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_RESERVE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
public class ReserveNoticeApplicationCircuitBreakerFallback {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private ReserveNoticeService service;

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
     * reserveNotice GET
     * 
     * <pre>
     * Correct pattern.
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testReserveNoticeCorrectForFallback() throws Exception {
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

        Reservation reservation = Reservation.builder().reserveId(1L).departureDate(LocalDate.of(2019, 5, 7))
                .flightId("NTT01").departureTime(LocalTime.of(10, 05)).arrivalTime(LocalTime.of(13, 05))
                .departureAirportId(departureAirportId).arrivalAirportId(arrivalAirportId).seatClass(SeatClass.N)
                .fareType("片道").fare(13500).passengers(passengers).emailId("0001@ntt.com").build();

        reservationRepository.saveAndFlush(reservation);

        // do
        // 出発空港情報が取得できない場合
        // 空港情報Map
        Map<String, String> airportMap = new HashMap<String, String>();
        airportMap.put(departureAirportId, "");
        airportMap.put(arrivalAirportId, "");
        String emailId = "0001@ntt.com";
        doThrow(createFeignException(HttpStatus.TOO_MANY_REQUESTS)).when(flightExternalMicroService)
                .getAirport(any(String.class));

        mvc.perform(MockMvcRequestBuilders.get(urlBase + "/rss-feed").param("emailId", emailId)
                .accept(MediaType.APPLICATION_RSS_XML).contentType(MediaType.APPLICATION_RSS_XML))
                .andExpect(status().isOk()).andExpect(content().contentType("application/rss+xml;charset=UTF-8"))
                .andExpect(xpath("/rss/@version").string("2.0")).andExpect(xpath("/rss/channel/title").string("M9AREF"))
                .andExpect(xpath("/rss/channel/description").string("予約完了通知"))
                .andExpect(xpath("/rss/channel/item/title").string("予約Id：1の予約情報"))
                .andExpect(xpath("/rss/channel/item/pubDate")
                        .string(LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm"))))
                .andExpect(xpath("/rss/channel/item/description").string(
                        "ご予約ありがとうございました。予約の詳細メッセージです。\r\n予約Id：1\r\nフライト名：NTT01\r\n出発空港：HND\r\n出発日：2019-05-07\r\n出発時刻：10:05\r\n到着空港：KIX\r\n到着時刻：13:05\r\n搭乗クラス：N\r\n料金タイプ：片道\r\n料金：\\13500\r\n代表搭乗者：\r\n  お名前：渡辺太郎\r\n  年齢：31\r\n同時搭乗者：\r\n  お名前：渡辺花子\r\n  年齢：31\r\n"));

    }

    /**
     * FeignExceptionインスタンスを作る。
     * 
     * @param httpStatus HttpStatus。
     * @return FeignException FeignExceptionインスタンス。
     * @throws Exception 例外。
     */
    private FeignException createFeignException(HttpStatus httpStatus) throws Exception {
        Constructor<FeignException> feConstructor = ReflectionUtils.accessibleConstructor(FeignException.class,
                int.class, String.class);
        feConstructor.setAccessible(true);
        return feConstructor.newInstance(httpStatus.value(), httpStatus.getReasonPhrase());
    }

}
