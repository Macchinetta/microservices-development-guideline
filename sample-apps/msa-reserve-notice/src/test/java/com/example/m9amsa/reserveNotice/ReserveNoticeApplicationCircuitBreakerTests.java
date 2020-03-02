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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.m9amsa.reserveNotice.config.TestConfig;
import com.example.m9amsa.reserveNotice.constant.SeatClass;
import com.example.m9amsa.reserveNotice.entity.BaseClock;
import com.example.m9amsa.reserveNotice.entity.Passenger;
import com.example.m9amsa.reserveNotice.entity.Reservation;
import com.example.m9amsa.reserveNotice.entity.ReservationRepository;
import com.example.m9amsa.reserveNotice.service.ReserveNoticeService;
import com.github.tomakehurst.wiremock.client.WireMock;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_RESERVE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
@Import(TestConfig.class)
@AutoConfigureWireMock(port = 9080)
@Slf4j
public class ReserveNoticeApplicationCircuitBreakerTests {

    // controller
    @Autowired
    private MockMvc mvc;

    @Value("${info.url.root-path}")
    private String urlRoot;
    private String urlBase;

    @Autowired
    private ReservationRepository reservationRepository;

    @Mock
    private BaseClock baseClock;

    @SpyBean
    private ReserveNoticeService service;

    @Before
    public void setUp() throws Exception {
        this.delete();

        MockitoAnnotations.initMocks(this);
        urlBase = "/" + urlRoot + "/reserve-notice";
    }

    private void delete() {
        reservationRepository.deleteAll();
        reservationRepository.flush();
    }

    /**
     * /getAirport GET
     * 
     * <pre>
     * flightCircuitBreaker CircuitBreaker OPEN
     * </pre>
     */
    @Test
    public void testGetAirportCircuitBreaker() throws Exception {

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

        String emailId = "0001@ntt.com";

        int runTimes = 3;
        // 400系、6回目の実行でもサーキットブレーカーがOPENしません。
        // 429: HttpStatus.TOO_MANY_REQUESTS
        stubFor(WireMock.get(WireMock.urlMatching("/msaref/flight/airport/.+"))
                .willReturn(aResponse().withStatus(429)));

        for (int i = 1; i <= runTimes; i++) {
            // 通知サービスからの1回のリクエストでフライトサービスを2回をアクセスします。
            log.info("実行{},{}回目.", i * 2 - 1, i * 2);
            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(urlBase + "/rss-feed")
                    .param("emailId", emailId).accept(MediaType.APPLICATION_JSON_UTF8)//
                    .contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();//
            assertNull("例外が発生していないこと。", mvcResult.getResolvedException());
        }
        verify(exactly(6), WireMock.getRequestedFor(WireMock.urlMatching("/msaref/airport/.+")));

        System.out.println("-----------------------------------------------------------------------------------");
        WireMock.removeAllMappings();
        WireMock.reset();

        // 500系、1回目の実行でサーキットブレーカーがOPENします。
        stubFor(WireMock.get(WireMock.urlMatching("/msaref/airport/.+"))
                .willReturn(aResponse().withStatus(500)));
        for (int i = 1; i <= runTimes; i++) {
            log.info("実行{},{}回目.", i * 2 - 1, i * 2);
            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(urlBase + "/rss-feed")
                    .param("emailId", emailId).accept(MediaType.APPLICATION_JSON_UTF8)//
                    .contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();//

            if (i >= 1) { // 通知サービスからの1回のリクエストでフライトサービを2回アクセスします。
                if (mvcResult.getResolvedException().getCause() instanceof CallNotPermittedException) {
                    assertEquals("サーキットブレーカーのHALF_OPEN or OPEN時の例外であること", CallNotPermittedException.class,
                            mvcResult.getResolvedException().getCause().getClass());
                    // 1回目の実行で、フライトサービスを2回呼び出します。一回目失敗したらOpen状態となります。
                    verify(com.github.tomakehurst.wiremock.client.WireMock.exactly(1),
                            WireMock.getRequestedFor(WireMock.urlMatching("/msaref/airport/.+")));
                }
            }
        }
    }
}
