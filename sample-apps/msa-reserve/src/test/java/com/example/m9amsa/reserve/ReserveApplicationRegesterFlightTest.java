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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.QueryTimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.m9amsa.reserve.config.TestConfig;
import com.example.m9amsa.reserve.constant.SeatClass;
import com.example.m9amsa.reserve.controller.ReserveFlightController;
import com.example.m9amsa.reserve.entity.AirplaneRepository;
import com.example.m9amsa.reserve.entity.AirportRepository;
import com.example.m9amsa.reserve.entity.BasicFareRepository;
import com.example.m9amsa.reserve.entity.Flight;
import com.example.m9amsa.reserve.entity.FlightRepository;
import com.example.m9amsa.reserve.entity.Reservation;
import com.example.m9amsa.reserve.entity.ReservationRepository;
import com.example.m9amsa.reserve.externalmicroservice.model.PurchaseInfoForEx;
import com.example.m9amsa.reserve.model.PassengerInfoModel;
import com.example.m9amsa.reserve.model.ReservationRequest;
import com.example.m9amsa.reserve.service.ReserveFlightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "URL_ROOT=msaref", "HOSTNAME_CALCULATE_FARE=localhost:9080",
        "DB_HOSTNAME_RESERVE=localhost:5432", "HOSTNAME_FLIGHT=localhost:9080", "HOSTNAME_PURCHASE=localhost:9080",
        "JAEGER_HOST=localhost", "HOSTNAME_ACCOUNT=localhost", "OAUTH2_CLIENT_ID=my-client", "OAUTH2_CLIENT_SECRET=1" })
@EnableResourceServer
@Import(TestConfig.class)
@AutoConfigureWireMock(port = 9080)
public class ReserveApplicationRegesterFlightTest {

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

    @SpyBean
    private ReserveFlightService reserveFlightService;

    @InjectMocks
    private ReserveFlightController reserveFlightController;

    // controller
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper jsonMapper;

    @Value("${info.url.root-path}")
    private String urlRoot;
    private String urlBase;

    @Autowired
    private OAuthHelper oauthHelper;

    @Before
    public void setUp() throws Exception {
        this.delete();

        MockitoAnnotations.initMocks(this);
        reset(reserveFlightService);

        WireMock.reset();

        urlBase = "/" + urlRoot + "/reserve";
    }

    /**
     * /reserve POST
     * 
     * <pre>
     * Correct pattern.
     * </pre>
     */
    @Test
    public void testReserveFlightCorrect() throws Exception {
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

        // flight 空席確保
        stubFor(post(urlEqualTo("/msaref/flight/seat/reserve")).willReturn(aResponse().withStatus(200)));

        // purchase 予約登録
        stubFor(post(urlEqualTo("/msaref/purchase/register")).willReturn(aResponse().withStatus(200)));

        // do
        String json = jsonMapper.writeValueAsString(reservationRequest);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0000000000", "GUEST");
        System.out.println("-----------------------------------------------------");
        System.out.println(json);
        mvc.perform(MockMvcRequestBuilders.post(urlBase + "/reserve-flight").content(json).with(postProcessor)
                .accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }

    /**
     * /reserve POST
     * 
     * <pre>
     * Error pattern.
     * </pre>
     */
    @Test
    public void testReserveFlighError401() throws Exception {
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

        // flight 空席確保
        stubFor(post(urlEqualTo("/msaref/flight/seat/reserve")).willReturn(aResponse().withStatus(200)));

        // purchase 予約登録
        stubFor(post(urlEqualTo("/msaref/purchase/register")).willReturn(aResponse().withStatus(200)));

        // do
        String json = jsonMapper.writeValueAsString(reservationRequest);
        mvc.perform(MockMvcRequestBuilders.post(urlBase + "/reserve-flight").content(json)
                .accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is(401));
    }

    /**
     * /reserve POST
     * 
     * <pre>
     * Error pattern.
     * </pre>
     */
    @Test
    public void testReserveFlightError() throws Exception {
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

        // flight 空席確保
        stubFor(post(urlEqualTo("/msaref/flight/seat/reserve")).willReturn(aResponse().withStatus(200)));

        // flight 空席取り消し(SAGA)
        stubFor(post(urlMatching("/msaref/flight/seat/cancel")).willReturn(aResponse().withStatus(200)));

        // purchase 予約登録
        stubFor(post(urlEqualTo("/msaref/purchase/register")).willReturn(aResponse().withStatus(500)));

        // purchase 予約登録取り消し(SAGA)
        stubFor(get(urlMatching("/msaref/purchase/delete/.+")).willReturn(aResponse().withStatus(200)));

        // do
        String json = jsonMapper.writeValueAsString(reservationRequest);
        System.out.println(json);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0000000000", "GUEST");
        mvc.perform(MockMvcRequestBuilders.post(urlBase + "/reserve-flight").content(json).with(postProcessor)
                .accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isInternalServerError());

        verify(exactly(1), postRequestedFor(urlEqualTo("/msaref/flight/seat/reserve")));
        verify(exactly(1), postRequestedFor(urlMatching("/msaref/flight/seat/cancel")));
        verify(exactly(3), postRequestedFor(urlEqualTo("/msaref/purchase/register"))); // リトライが設定されているので3回リクエスト
        verify(exactly(0), getRequestedFor(urlMatching("/msaref/purchase/delete/.+")));
    }

    /**
     * /reserve POST
     * 
     * <pre>
     * Error pattern.
     * </pre>
     */
    @Test
    public void testReserveFlightInternalError() throws Exception {
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

        // flight 空席確保
        stubFor(post(urlEqualTo("/msaref/flight/seat/reserve")).willReturn(aResponse().withStatus(200)));

        // flight 空席取り消し(SAGA)
        stubFor(get(urlMatching("/msaref/flight/seat/cancel")).willReturn(aResponse().withStatus(200)));

        // purchase 予約登録
        stubFor(post(urlEqualTo("/msaref/purchase/register")).willReturn(aResponse().withStatus(200)));

        // purchase 予約登録取り消し(SAGA)
        stubFor(get(urlMatching("/msaref/purchase/delete/.+")).willReturn(aResponse().withStatus(200)));

        doThrow(QueryTimeoutException.class).when(reserveFlightService).registerReservationInfo(any(Reservation.class),
                any(PurchaseInfoForEx.class));

        // do
        String json = jsonMapper.writeValueAsString(reservationRequest);
        System.out.println(json);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0000000000", "GUEST");
        mvc.perform(MockMvcRequestBuilders.post(urlBase + "/reserve-flight").content(json).with(postProcessor)
                .accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isInternalServerError());

        verify(exactly(1), postRequestedFor(urlEqualTo("/msaref/flight/seat/reserve")));
        verify(exactly(1), postRequestedFor(urlMatching("/msaref/flight/seat/cancel")));
        verify(exactly(1), postRequestedFor(urlEqualTo("/msaref/purchase/register")));
        verify(exactly(1), getRequestedFor(urlMatching("/msaref/purchase/delete/.+")));
    }

    /**
     * /reserve POST
     * 
     * <pre>
     * Validation error pattern.
     * </pre>
     */
    @Test
    public void testReserveFlightValidationError() throws Exception {
        // when
        ReservationRequest reservationRequest = new ReservationRequest();

        // do
        String json = jsonMapper.writeValueAsString(reservationRequest);
        System.out.println(json);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0000000000", "GUEST");
        mvc.perform(MockMvcRequestBuilders.post(urlBase + "/reserve-flight").content(json).with(postProcessor)
                .accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());
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
}
