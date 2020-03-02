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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.m9amsa.reserve.config.TestConfig;
import com.example.m9amsa.reserve.constant.FlightType;
import com.example.m9amsa.reserve.constant.SeatClass;
import com.example.m9amsa.reserve.controller.TicketSearchController;
import com.example.m9amsa.reserve.entity.Airplane;
import com.example.m9amsa.reserve.entity.AirplaneRepository;
import com.example.m9amsa.reserve.entity.Airport;
import com.example.m9amsa.reserve.entity.AirportRepository;
import com.example.m9amsa.reserve.entity.BasicFare;
import com.example.m9amsa.reserve.entity.BasicFareId;
import com.example.m9amsa.reserve.entity.BasicFareRepository;
import com.example.m9amsa.reserve.entity.Flight;
import com.example.m9amsa.reserve.entity.FlightRepository;
import com.example.m9amsa.reserve.entity.ReservationRepository;
import com.example.m9amsa.reserve.externalmicroservice.model.FlightFareForEx;
import com.example.m9amsa.reserve.model.FareInfo;
import com.example.m9amsa.reserve.model.VacantSeatInfo;
import com.example.m9amsa.reserve.model.VacantSeatQueryCondition;
import com.example.m9amsa.reserve.model.topic.AirplaneTopic;
import com.example.m9amsa.reserve.model.topic.AirportTopic;
import com.example.m9amsa.reserve.model.topic.BasicFareTopic;
import com.example.m9amsa.reserve.model.topic.FlightTopic;
import com.example.m9amsa.reserve.model.topic.FlightTopicSink;
import com.example.m9amsa.reserve.service.TicketSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "URL_ROOT=msaref", "HOSTNAME_CALCULATE_FARE=localhost:28080",
        "DB_HOSTNAME_RESERVE=localhost:5432", "HOSTNAME_FLIGHT=localhost:28081", "HOSTNAME_PURCHASE=localhost:28082",
        "JAEGER_HOST=localhost", "HOSTNAME_ACCOUNT=localhost", "OAUTH2_CLIENT_ID=my-client", "OAUTH2_CLIENT_SECRET=1" })
@Import(TestConfig.class)
@AutoConfigureWireMock(port = 9080)
@EnableResourceServer
public class ReserveApplicationTests {

    // listener
    @Autowired
    FlightTopicSink flightTopicSink;

    @Autowired
    AirportRepository airportRepository;
    @Autowired
    AirplaneRepository airplaneRepository;
    @Autowired
    BasicFareRepository basicFareRepository;
    @Autowired
    FlightRepository flightRepository;
    @Autowired
    ReservationRepository reservationRepository;

    @Mock
    ReservationRepository reservationInfoRepositoryM;

    @SpyBean
    TicketSearchService ticketSearchService;

    @InjectMocks
    TicketSearchController controller;

    // controller
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper jsonMapper;

    @Value("${info.url.root-path}")
    private String urlRoot;
    private String urlBase;

    @Autowired
    private OAuthHelper oauthHelper;

    @Before
    public void setUp() throws Exception {
        this.delete();

        MockitoAnnotations.initMocks(this);
        reset(ticketSearchService);

        WireMock.reset();

        urlBase = "/" + urlRoot + "/reserve";
    }

    /**
     * Kafka consumer
     */
    @Test
    public void testFlightInfoListener() throws Exception {

        // when
        Airport airport = Airport.builder().id("HND").name("羽田").build();
        Airplane airplane = Airplane.builder().id(1L).name("Boeing 777").specialSeats(96).standardSeats(270).build();
        BasicFare basicFare = BasicFare.builder().arrivalAirportId("KIX").departureAirportId("HND").fare(10520).build();
        FlightTopic flightTopic = FlightTopic.builder().name("TEST001").departureTime(LocalTime.of(9, 0))
                .arrivalTime(LocalTime.of(10, 30)).departureAirportId("HND").arrivalAirportId("KIX").airplaneId(1L)
                .build();

        // do Airport
        Map<String, Object> headers = new HashMap<>();
        headers.put("x-payload-class", AirportTopic.class.getSimpleName());
        AirportTopic airportTopic = AirportTopic.builder().id(airport.getId()).name(airport.getName()).build();
        Message<AirportTopic> airportMessage = new GenericMessage<AirportTopic>(airportTopic, headers);
        flightTopicSink.input().send(airportMessage);

        // verify Airport
        Optional<Airport> airportResult = airportRepository.findById("HND");
        assertThat("受信したairportをDB登録できること", airportResult.get().toString(), equalTo(airport.toString()));

        // do AirplaneTopic
        headers = new HashMap<>();
        headers.put("x-payload-class", AirplaneTopic.class.getSimpleName());
        AirplaneTopic airplaneTopic = AirplaneTopic.builder().id(airplane.getId()).name(airplane.getName())
                .specialSeats(airplane.getSpecialSeats()).standardSeats(airplane.getStandardSeats()).build();
        Message<AirplaneTopic> airplaneMessage = new GenericMessage<AirplaneTopic>(airplaneTopic, headers);
        flightTopicSink.input().send(airplaneMessage);

        // verify AirplaneTopic
        Optional<Airplane> airplaneResult = airplaneRepository.findById(1L);
        assertThat("受信したairplaneをDB登録できること", airplaneResult.get().toString(), equalTo(airplane.toString()));

        // do BasicFare
        headers = new HashMap<>();
        headers.put("x-payload-class", BasicFareTopic.class.getSimpleName());
        BasicFareTopic basicFareTopic = BasicFareTopic.builder().departureAirportId(basicFare.getDepartureAirportId())
                .arrivalAirportId(basicFare.getArrivalAirportId()).fare(basicFare.getFare()).build();
        Message<BasicFareTopic> fareMessage = new GenericMessage<BasicFareTopic>(basicFareTopic, headers);
        flightTopicSink.input().send(fareMessage);

        // verify BasicFare
        Optional<BasicFare> fareResult = basicFareRepository
                .findById(BasicFareId.builder().arrivalAirportId("KIX").departureAirportId("HND").build());
        assertThat("受信したbasicFareをDB登録できること", fareResult.get().toString(), equalTo(basicFare.toString()));

        // do FlightTopic
        headers = new HashMap<>();
        headers.put("x-payload-class", FlightTopic.class.getSimpleName());
        Message<FlightTopic> flightMessage = new GenericMessage<FlightTopic>(flightTopic, headers);
        flightTopicSink.input().send(flightMessage);

        // verify FlightTopic
        Optional<Flight> flightResult = flightRepository.findById("TEST001");
        Flight actual = flightResult.get();
        assertThat("受信したflightからflightInfoを生成・登録できること: airplaneName", actual.getAirplaneName(),
                equalTo(airplane.getName()));
        assertThat("受信したflightからflightInfoを生成・登録できること: arrvalAirport", actual.getArrivalAirportId(),
                equalTo(flightTopic.getArrivalAirportId()));
        assertThat("受信したflightからflightInfoを生成・登録できること: arrivalTime", actual.getArrivalTime(),
                equalTo(flightTopic.getArrivalTime()));
        assertThat("受信したflightからflightInfoを生成・登録できること: departureAirport", actual.getDepartureAirportId(),
                equalTo(flightTopic.getDepartureAirportId()));
        assertThat("受信したflightからflightInfoを生成・登録できること: departureTime", actual.getDepartureTime(),
                equalTo(flightTopic.getDepartureTime()));
        assertThat("受信したflightからflightInfoを生成・登録できること: fare", actual.getFare(), equalTo(basicFare.getFare()));
        assertThat("受信したflightからflightInfoを生成・登録できること: flightId", actual.getName(), equalTo(flightTopic.getName()));
        assertThat("受信したflightからflightInfoを生成・登録できること: specialSeats", actual.getSpecialSeats(),
                equalTo(airplane.getSpecialSeats()));
        assertThat("受信したflightからflightInfoを生成・登録できること: standardSeats", actual.getStandardSeats(),
                equalTo(airplane.getStandardSeats()));
    }

    /**
     * /reserve POST
     * 
     * <pre>
     * Correct pattern.
     * </pre>
     */
    @Test
    public void testGetVacantSeatInfoCorrect() throws Exception {
        // when
        VacantSeatQueryCondition condition = VacantSeatQueryCondition.builder().flightType(FlightType.OW)
                .departureAirportId("HND").arrivalAirportId("KIX").departureDate(LocalDate.of(2019, 7, 29))
                .seatClass(SeatClass.N).build();

        Flight flightInfo = Flight.builder().airplaneName("Boeing 777").arrivalAirportId("KIX")
                .arrivalTime(LocalTime.of(10, 30)).departureAirportId("HND").departureTime(LocalTime.of(9, 0))
                .fare(10520).name("TEST001").specialSeats(96).standardSeats(270).build();

        flightRepository.save(flightInfo);

        FlightFareForEx flightFare = FlightFareForEx.builder().name("早期割").fare(9470).build();
        List<FlightFareForEx> fareList = new ArrayList<>();
        fareList.add(flightFare);
        // 運賃計算
        stubFor(post(urlEqualTo("/msaref/flight-ticket-fare")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(jsonMapper.writeValueAsString(fareList))));

        FareInfo fareInfo = FareInfo.builder().fare(flightFare.getFare()).fareType(flightFare.getName()).build();
        List<FareInfo> fareExp = Arrays.asList(new FareInfo[] { fareInfo });
        VacantSeatInfo exp = VacantSeatInfo.builder().arrivalAirportId("KIX").arrivalTime(flightInfo.getArrivalTime())
                .departureAirportId("HND").departureTime(flightInfo.getDepartureTime()).fareList(fareExp)
                .name(flightInfo.getName()).seatClass(condition.getSeatClass())
                .vacantSeats(flightInfo.getStandardSeats()).build();
        List<VacantSeatInfo> expList = Arrays.asList(new VacantSeatInfo[] { exp });

        // do
        String json = jsonMapper.writeValueAsString(condition);
        System.out.println(json);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0000000000", "GUEST");
        mvc.perform(MockMvcRequestBuilders.post(urlBase + "/vacant-seat-info").content(json).with(postProcessor)
                .accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(jsonMapper.writeValueAsString(expList)));
    }

    /**
     * /reserve POST
     * 
     * <pre>
     * Error pattern.
     * </pre>
     */
    @Test
    public void testGetVacantSeatInfoError401() throws Exception {
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
        System.out.println(json);
        mvc.perform(MockMvcRequestBuilders.post(urlBase + "/vacant-seat-info").content(json)
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
    public void testGetVacantSeatInfoError() throws Exception {
        // when
        VacantSeatQueryCondition condition = VacantSeatQueryCondition.builder().flightType(FlightType.OW)
                .departureAirportId("HND").arrivalAirportId("KIX").departureDate(LocalDate.of(2019, 7, 29))
                .seatClass(SeatClass.N).build();

        Flight flightInfo = Flight.builder().airplaneName("Boeing 777").arrivalAirportId("KIX")
                .arrivalTime(LocalTime.of(10, 30)).departureAirportId("HND").departureTime(LocalTime.of(9, 0))
                .fare(10520).name("TEST001").specialSeats(96).standardSeats(270).build();

        flightRepository.save(flightInfo);

        // 運賃計算
        stubFor(post(urlEqualTo("/msaref/flight-ticket-fare")).willReturn(aResponse().withStatus(404)));

        // do
        String json = jsonMapper.writeValueAsString(condition);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0000000000", "GUEST");
        mvc.perform(MockMvcRequestBuilders.post(urlBase + "/vacant-seat-info").content(json).with(postProcessor)
                .accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    /**
     * /reserve POST
     * 
     * <pre>
     * Validation error pattern.
     * </pre>
     */
    @Test
    public void testGetVacantSeatInfoValidationError() throws Exception {
        // when
        VacantSeatQueryCondition condition = new VacantSeatQueryCondition();

        // do
        String json = jsonMapper.writeValueAsString(condition);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0000000000", "GUEST");
        mvc.perform(MockMvcRequestBuilders.post(urlBase + "/vacant-seat-info").content(json).with(postProcessor)
                .accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
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
