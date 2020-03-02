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
package com.example.m9amsa.reserve;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.lessThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.removeAllMappings;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.m9amsa.reserve.config.TestConfig;
import com.example.m9amsa.reserve.constant.FlightType;
import com.example.m9amsa.reserve.constant.SeatClass;
import com.example.m9amsa.reserve.entity.AirplaneRepository;
import com.example.m9amsa.reserve.entity.AirportRepository;
import com.example.m9amsa.reserve.entity.BasicFareRepository;
import com.example.m9amsa.reserve.entity.Flight;
import com.example.m9amsa.reserve.entity.FlightRepository;
import com.example.m9amsa.reserve.entity.ReservationRepository;
import com.example.m9amsa.reserve.model.PassengerInfoModel;
import com.example.m9amsa.reserve.model.ReservationRequest;
import com.example.m9amsa.reserve.model.VacantSeatQueryCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "URL_ROOT=msaref", "HOSTNAME_CALCULATE_FARE=localhost:28080",
        "DB_HOSTNAME_RESERVE=localhost:5432", "HOSTNAME_FLIGHT=localhost:28081", "HOSTNAME_PURCHASE=localhost:28082",
        "JAEGER_HOST=localhost", "HOSTNAME_ACCOUNT=localhost", "OAUTH2_CLIENT_ID=my-client", "OAUTH2_CLIENT_SECRET=1" })
@EnableResourceServer
@Import(TestConfig.class)
@AutoConfigureWireMock(port = 9080)
@Slf4j
public class ReserveApplicationCircuitBreakerTests {

    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private AirplaneRepository airplaneRepository;
    @Autowired
    private BasicFareRepository basicFareRepository;
    @Autowired
    private FlightRepository flightRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private OAuthHelper oauthHelper;

    // controller
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper jsonMapper;

    @Value("${info.url.root-path}")
    private String urlRoot;
    private String urlBase;

    @Before
    public void setUp() throws Exception {
        this.delete();

        WireMock.reset();

        MockitoAnnotations.initMocks(this);
        urlBase = "/" + urlRoot + "/reserve";
    }

    private void delete() {
        airportRepository.deleteAll();
        airportRepository.flush();
        airplaneRepository.deleteAll();
        airplaneRepository.flush();
        basicFareRepository.deleteAll();
        basicFareRepository.flush();
        flightRepository.deleteAll();
        flightRepository.flush();
        reservationRepository.deleteAll();
        reservationRepository.flush();
    }

    /**
     * /getVacantSeatInfo POST
     * 
     * <pre>
     * calculateFareCircuitBreaker CircuitBreaker OPEN
     * </pre>
     */
    @Test
    public void testGetVacantSeatInfoCircuitBreaker() throws Exception {

        // when
        VacantSeatQueryCondition condition = VacantSeatQueryCondition.builder().flightType(FlightType.OW)
                .departureAirportId("HND").arrivalAirportId("KIX").departureDate(LocalDate.of(2019, 7, 29))
                .seatClass(SeatClass.N).build();

        Flight flightInfo = Flight.builder().airplaneName("Boeing 777").arrivalAirportId("KIX")
                .arrivalTime(LocalTime.of(10, 30)).departureAirportId("HND").departureTime(LocalTime.of(9, 0))
                .fare(10520).name("TEST001").specialSeats(96).standardSeats(270).build();

        flightRepository.save(flightInfo);

        // do
        String json = jsonMapper.writeValueAsString(condition);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");

        int runTimes = 6;
        // 400系、6回目の実行でもサーキットブレーカーがOPENしません。
        // flight 空席確保
        stubFor(post(urlEqualTo("/msaref/flight-ticket-fare")).willReturn(aResponse().withStatus(401)));
        for (int i = 0; i < runTimes; i++) {
            log.info("実行{}回目.", (i + 1));
            mvc.perform(MockMvcRequestBuilders.post(urlBase + "/vacant-seat-info").content(json).with(postProcessor)
                    .accept(MediaType.APPLICATION_JSON_UTF8)//
                    .contentType(MediaType.APPLICATION_JSON_UTF8))//
                    .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
        verify(exactly(6), postRequestedFor(urlEqualTo("/msaref/flight-ticket-fare")));

        System.out.println("-----------------------------------------------------------------------------------");
        removeAllMappings();
        WireMock.reset();

        // 500系、4回目の実行でサーキットブレーカーがOPENします。
        stubFor(post(urlEqualTo("/msaref/flight-ticket-fare")).willReturn(aResponse().withStatus(500)));
        for (int i = 0; i < runTimes; i++) {
            try {
                log.info("実行{}回目.", (i + 1));
                mvc.perform(MockMvcRequestBuilders.post(urlBase + "/vacant-seat-info").content(json).with(postProcessor)
                        .accept(MediaType.APPLICATION_JSON_UTF8)//
                        .contentType(MediaType.APPLICATION_JSON_UTF8))//
                        .andExpect(status().is(500));
            } catch (Exception e) {
                log.info("Error: {}", e.getMessage());
                assertEquals("サーキットブレーカーのHALF_OPEN or OPEN時の例外であること", CallNotPermittedException.class,
                        e.getCause().getClass());
                // 実行時の状況次第で回数が変わるのでlessThanOrExactly
                verify(lessThanOrExactly(12), postRequestedFor(urlEqualTo("/msaref/flight-ticket-fare")));
                assertTrue("運賃計算のサーキットブレーカーがOPEN", e.getMessage()
                        .contains(String.format("CircuitBreaker '%s' is OPEN", "calculateFareCircuitBreaker")));
            }
        }
    }

    /**
     * /reserveFlight POST
     * 
     * <pre>
     * flightCircuitBreaker CircuitBreaker OPEN
     * </pre>
     */
    @Test
    public void testReserveFlightCircuitBreaker() throws Exception {

        // when
        List<PassengerInfoModel> passengers = new ArrayList<PassengerInfoModel>();
        ReservationRequest reservationRequest = ReservationRequest.builder().flightId("TEST001")
                .departureDate(LocalDate.now()).departureTime(LocalTime.of(9, 0)).arrivalTime(LocalTime.of(10, 30))
                .departureAirportId("HND").arrivalAirportId("KIX").seatClass(SeatClass.N).fareType("早期割").fare(10520)
                .passengers(passengers).build();

        passengers.add(PassengerInfoModel.builder().name("代表").age(20).telephoneNo("080-1234-5678")
                .email("abc@email.com").isMainPassenger(true).build());
        passengers.add(PassengerInfoModel.builder().name("同時１").age(21).isMainPassenger(false).build());
        passengers.add(PassengerInfoModel.builder().name("同時２").age(22).isMainPassenger(false).build());

        Flight flightInfo = Flight.builder().airplaneName("Boeing 777").arrivalAirportId("KIX")
                .arrivalTime(LocalTime.of(10, 30)).departureAirportId("HND").departureTime(LocalTime.of(9, 0))
                .fare(10520).name("TEST001").specialSeats(96).standardSeats(270).build();

        flightRepository.save(flightInfo);

        // do
        String json = jsonMapper.writeValueAsString(reservationRequest);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");

        int runTimes = 6;
        // 400系、6回目の実行でもサーキットブレーカーがOPENしません。
        // flight 空席確保
        stubFor(post(urlEqualTo("/msaref/flight/seat/reserve")).willReturn(aResponse().withStatus(400)));
        for (int i = 0; i < runTimes; i++) {
            log.info("実行{}回目.", (i + 1));
            mvc.perform(MockMvcRequestBuilders.post(urlBase + "/reserve-flight").content(json).with(postProcessor)
                    .accept(MediaType.APPLICATION_JSON_UTF8)//
                    .contentType(MediaType.APPLICATION_JSON_UTF8))//
                    .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
        // リトライ回数３×サーキットブレーカーのリングバッファサイズ５＋１
        verify(lessThanOrExactly(16), postRequestedFor(urlEqualTo("/msaref/flight/seat/reserve")));

        System.out.println("-----------------------------------------------------------------------------------");
        removeAllMappings();
        WireMock.reset();

        // 500系、6回目の実行でサーキットブレーカーがOPENします。
        // flight 空席確保
        stubFor(post(urlEqualTo("/msaref/flight/seat/reserve")).willReturn(aResponse().withStatus(500)));
        for (int i = 0; i < runTimes; i++) {
            try {
                log.info("実行{}回目.", (i + 1));
                mvc.perform(MockMvcRequestBuilders.post(urlBase + "/reserve-flight").content(json).with(postProcessor)
                        .accept(MediaType.APPLICATION_JSON_UTF8)//
                        .contentType(MediaType.APPLICATION_JSON_UTF8))//
                        .andExpect(status().is(500));
            } catch (Exception e) {
                log.info("Error: {}", e.getMessage());
                assertEquals("サーキットブレーカーのHALF_OPEN or OPEN時の例外であること", CallNotPermittedException.class,
                        e.getCause().getClass());
                // リトライ回数３×サーキットブレーカーのリングバッファサイズ５＋１
                verify(lessThanOrExactly(16), postRequestedFor(urlEqualTo("/msaref/flight/seat/reserve")));

                assertTrue("フライト空席確保・確保の取り消しのサーキットブレーカーがOPEN",
                        e.getMessage().contains(String.format("CircuitBreaker '%s' is OPEN", "flightCircuitBreaker")));
            }
        }
    }

    /**
     * /reserveFlight POST
     * 
     * <pre>
     * purchaseCircuitBreaker CircuitBreaker OPEN
     * </pre>
     */
    @Test
    public void testReserveFligh2CircuitBreaker() throws Exception {

        // when
        List<PassengerInfoModel> passengers = new ArrayList<PassengerInfoModel>();
        ReservationRequest reservationRequest = ReservationRequest.builder().flightId("TEST001")
                .departureDate(LocalDate.now()).departureTime(LocalTime.of(9, 0)).arrivalTime(LocalTime.of(10, 30))
                .departureAirportId("HND").arrivalAirportId("KIX").seatClass(SeatClass.N).fareType("早期割").fare(10520)
                .passengers(passengers).build();

        passengers.add(PassengerInfoModel.builder().name("代表").age(20).telephoneNo("080-1234-5678")
                .email("abc@email.com").isMainPassenger(true).build());
        passengers.add(PassengerInfoModel.builder().name("同時１").age(21).isMainPassenger(false).build());
        passengers.add(PassengerInfoModel.builder().name("同時２").age(22).isMainPassenger(false).build());

        Flight flightInfo = Flight.builder().airplaneName("Boeing 777").arrivalAirportId("KIX")
                .arrivalTime(LocalTime.of(10, 30)).departureAirportId("HND").departureTime(LocalTime.of(9, 0))
                .fare(10520).name("TEST001").specialSeats(96).standardSeats(270).build();

        flightRepository.save(flightInfo);

        // do
        String json = jsonMapper.writeValueAsString(reservationRequest);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");

        int runTimes = 6;
        // 500系、6回目の実行でサーキットブレーカーがOPENします。
        // flight 空席確保
        stubFor(post(urlEqualTo("/msaref/flight/seat/reserve")).willReturn(aResponse().withStatus(200)));
        // flight 空席取り消し(SAGA)
        stubFor(get(urlMatching("/msaref/flight/seat/cancel/.+")).willReturn(aResponse().withStatus(200)));
        // purchase 予約登録
        stubFor(post(urlEqualTo("/msaref/purchase/register")).willReturn(aResponse().withStatus(500)));
        for (int i = 0; i < runTimes; i++) {
            try {
                log.info("実行{}回目.", (i + 1));

                mvc.perform(MockMvcRequestBuilders.post(urlBase + "/reserve-flight").content(json).with(postProcessor)
                        .accept(MediaType.APPLICATION_JSON_UTF8)//
                        .contentType(MediaType.APPLICATION_JSON_UTF8))//
                        .andExpect(status().is(500));

            } catch (Exception e) {
                log.info("Error: {}", e.getMessage());
                assertEquals("サーキットブレーカーのHALF_OPEN or OPEN時の例外であること", CallNotPermittedException.class,
                        e.getCause().getClass());
                verify(lessThanOrExactly(8), postRequestedFor(urlEqualTo("/msaref/flight/seat/reserve")));
                verify(lessThanOrExactly(8), getRequestedFor(urlMatching("/msaref/flight/seat/cancel/.+")));
                // リトライ回数３×サーキットブレーカーのリングバッファサイズ５＋１
                verify(lessThanOrExactly(16), postRequestedFor(urlEqualTo("/msaref/purchase/register")));

                assertTrue("購入サービスのサーキットブレーカーがOPEN", e.getMessage()
                        .contains(String.format("CircuitBreaker '%s' is OPEN", "purchaseCircuitBreaker")));
            }
        }
    }

}
