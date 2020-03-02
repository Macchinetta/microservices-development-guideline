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
package com.example.m9amsa.reserve.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.reserve.entity.Airplane;
import com.example.m9amsa.reserve.entity.AirplaneRepository;
import com.example.m9amsa.reserve.entity.Airport;
import com.example.m9amsa.reserve.entity.AirportRepository;
import com.example.m9amsa.reserve.entity.BasicFare;
import com.example.m9amsa.reserve.entity.BasicFareId;
import com.example.m9amsa.reserve.entity.BasicFareRepository;
import com.example.m9amsa.reserve.entity.Flight;
import com.example.m9amsa.reserve.entity.FlightRepository;
import com.example.m9amsa.reserve.entity.FlightVacantSeat;
import com.example.m9amsa.reserve.entity.FlightVacantSeatRepository;
import com.example.m9amsa.reserve.exception.HttpStatus500Exception;
import com.example.m9amsa.reserve.model.topic.AirplaneTopic;
import com.example.m9amsa.reserve.model.topic.AirportTopic;
import com.example.m9amsa.reserve.model.topic.BasicFareTopic;
import com.example.m9amsa.reserve.model.topic.FlightTopic;
import com.example.m9amsa.reserve.model.topic.FlightVacantSeatTopic;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "HOSTNAME_CALCULATE_FARE=localhost:28080",
        "HOSTNAME_FLIGHT=localhost:28081", "HOSTNAME_PURCHASE=localhost:28082", "DB_HOSTNAME_RESERVE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_ACCOUNT=localhost", "OAUTH2_CLIENT_ID=my-client", "OAUTH2_CLIENT_SECRET=1" })
public class FlightTopicServiceTest {

    @Mock
    FlightRepository flightRepository;

    @Mock
    AirportRepository airportRepository;

    @Mock
    AirplaneRepository airplaneRepository;

    @Mock
    BasicFareRepository basicFareRepository;

    @Mock
    FlightVacantSeatRepository flightVacantSeatRepository;

    @InjectMocks
    FlightTopicService flightTopicService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * registerFlightInfo() 正常系
     */
    @Test
    public void testRegisterFlightInfo() throws Exception {

        // when

        Airplane airplane = Airplane.builder().id(1L).name("Boeing 777").specialSeats(96).standardSeats(279).build();
        when(airplaneRepository.findById(1L)).thenReturn(Optional.of(airplane));

        BasicFare basicFare = BasicFare.builder().departureAirportId("HND").arrivalAirportId("KIX").fare(10520).build();
        ArgumentCaptor<BasicFareId> basicFareCapture = ArgumentCaptor.forClass(BasicFareId.class);
        when(basicFareRepository.findById(basicFareCapture.capture())).thenReturn(Optional.of(basicFare));

        ArgumentCaptor<Flight> flightInfoCapture = ArgumentCaptor.forClass(Flight.class);
        when(flightRepository.save(flightInfoCapture.capture())).thenReturn(null); // 戻り値を処理しないのでnullにしておく

        FlightTopic flightTopic = FlightTopic.builder().name("TEST001").departureTime(LocalTime.now())
                .arrivalTime(LocalTime.now()).departureAirportId("HND").arrivalAirportId("KIX").airplaneId(1L).build();

        // do
        flightTopicService.registerFlightInfo(flightTopic);

        // verify
        BasicFareId basicFareActual = basicFareCapture.getValue();
        assertThat("basicFareRepositoryの引数BasicFareId.arrival", basicFareActual.getArrivalAirportId(),
                equalTo(flightTopic.getArrivalAirportId()));
        assertThat("basicFareRepositoryの引数BasicFareId.departure", basicFareActual.getDepartureAirportId(),
                equalTo(flightTopic.getDepartureAirportId()));

        Flight flightInfoActual = flightInfoCapture.getValue();
        assertThat("flightInfoRepositoryの引数FlightInfo.name", flightInfoActual.getName(),
                equalTo(flightTopic.getName()));
        assertThat("flightInfoRepositoryの引数FlightInfo.departureAirport", flightInfoActual.getDepartureAirportId(),
                equalTo(flightTopic.getDepartureAirportId()));
        assertThat("flightInfoRepositoryの引数FlightInfo.arrivalAirport", flightInfoActual.getArrivalAirportId(),
                equalTo(flightTopic.getArrivalAirportId()));
        assertThat("flightInfoRepositoryの引数FlightInfo.departureTime", flightInfoActual.getDepartureTime(),
                equalTo(flightTopic.getDepartureTime()));
        assertThat("flightInfoRepositoryの引数FlightInfo.arrivalTime", flightInfoActual.getArrivalTime(),
                equalTo(flightTopic.getArrivalTime()));
        assertThat("flightInfoRepositoryの引数FlightInfo.airplaneName", flightInfoActual.getAirplaneName(),
                equalTo(airplane.getName()));
        assertThat("flightInfoRepositoryの引数FlightInfo.standardSeatNum", flightInfoActual.getStandardSeats(),
                equalTo(airplane.getStandardSeats()));
        assertThat("flightInfoRepositoryの引数FlightInfo.specialSeatNum", flightInfoActual.getSpecialSeats(),
                equalTo(airplane.getSpecialSeats()));
        assertThat("flightInfoRepositoryの引数FlightInfo.fare", flightInfoActual.getFare(), equalTo(basicFare.getFare()));
    }

    /**
     * registerFlightInfo() 異常系
     */
    @Test
    public void testRegisterFlightInfoError() {
        // when
        BasicFare basicFare = BasicFare.builder().departureAirportId("HND").arrivalAirportId("KIX").fare(10520).build();

        when(airplaneRepository.findById(1L)).thenReturn(Optional.empty());

        when(basicFareRepository.findById(any(BasicFareId.class))).thenReturn(Optional.of(basicFare));

        FlightTopic flight = FlightTopic.builder().name("TEST001").departureTime(LocalTime.now())
                .arrivalTime(LocalTime.now()).departureAirportId("HND").arrivalAirportId("KIX").airplaneId(1L).build();

        // do
        try {
            flightTopicService.registerFlightInfo(flight);
            fail("Airplaneを取得できない場合、正常終了しないこと");
        } catch (Exception e) {
            System.out.println(e);
            assertThat("HttpStatus500Exceptionをそのままスローすること", e.getClass(), equalTo(HttpStatus500Exception.class));
        }

        MockitoAnnotations.initMocks(this);

        // when
        Airplane airplane = Airplane.builder().id(1L).name("Boeing 777").specialSeats(96).standardSeats(279).build();
        when(airplaneRepository.findById(1L)).thenReturn(Optional.of(airplane));

        when(basicFareRepository.findById(any(BasicFareId.class))).thenReturn(Optional.empty());

        // do
        try {
            flightTopicService.registerFlightInfo(flight);
            fail("BasicFareを取得できない場合、正常終了しないこと");
        } catch (Exception e) {
            System.out.println(e);
            assertThat("HttpStatus500Exceptionをそのままスローすること", e.getClass(), equalTo(HttpStatus500Exception.class));
        }
    }

    /**
     * registerAirport()のテスト
     */
    @Test
    public void testRegisterAirport() {
        // when
        AirportTopic in = AirportTopic.builder().id("HND").name("羽田").build();
        Airport exp = Airport.builder().id("HND").name("羽田").build();
        ArgumentCaptor<Airport> capture = ArgumentCaptor.forClass(Airport.class);
        when(airportRepository.save(capture.capture())).thenReturn(null);

        // do
        flightTopicService.registerAirport(in);

        // verify
        Airport actual = capture.getValue();
        assertThat("serviceの引数とrepositoryの引数の値が一致すること", actual.toString(), equalTo(exp.toString()));
    }

    /**
     * registerAirplane()のテスト
     */
    @Test
    public void testRegisterAirplane() {
        // when
        AirplaneTopic in = AirplaneTopic.builder().id(1L).name("Boeing 777").specialSeats(96).standardSeats(279)
                .build();
        Airplane exp = Airplane.builder().id(1L).name("Boeing 777").specialSeats(96).standardSeats(279).build();
        ArgumentCaptor<Airplane> capture = ArgumentCaptor.forClass(Airplane.class);
        when(airplaneRepository.save(capture.capture())).thenReturn(null);

        // do
        flightTopicService.registerAirplane(in);

        // verify
        Airplane actual = capture.getValue();
        assertThat("serviceの引数とrepositoryの引数の値が一致すること", actual.toString(), equalTo(exp.toString()));
    }

    /**
     * registerBasicFare()のテスト
     */
    @Test
    public void testRegisterBasicFare() {
        // when
        BasicFareTopic in = BasicFareTopic.builder().departureAirportId("HND").arrivalAirportId("KIX").fare(10520)
                .build();
        BasicFare exp = BasicFare.builder().departureAirportId("HND").arrivalAirportId("KIX").fare(10520).build();
        ArgumentCaptor<BasicFare> capture = ArgumentCaptor.forClass(BasicFare.class);
        when(basicFareRepository.save(capture.capture())).thenReturn(null);

        // do
        flightTopicService.registerBasicFare(in);

        // verify
        BasicFare actual = capture.getValue();
        assertThat("serviceの引数とrepositoryの引数の値が一致すること", actual.toString(), equalTo(exp.toString()));
    }

    /**
     * registerFlightVacantSeatInfo()のテスト
     */
    @Test
    public void testRegisterFlightVacantSeatInfo() {
        // when
        FlightVacantSeatTopic in = FlightVacantSeatTopic.builder().departureDate(LocalDate.now()).flightName("TEST001")
                .standardSeats(20).specialSeats(10).build();
        FlightVacantSeat exp = FlightVacantSeat.builder().departureDate(LocalDate.now()).flightName("TEST001")
                .standardSeats(20).specialSeats(10).build();

        ArgumentCaptor<FlightVacantSeat> capture = ArgumentCaptor.forClass(FlightVacantSeat.class);
        when(flightVacantSeatRepository.save(capture.capture())).thenReturn(null);

        // do
        flightTopicService.registerFlightVacantSeat(in);

        // verify
        FlightVacantSeat actual = capture.getValue();
        assertThat("repositoryの引数が正しいこと", actual.toString(), equalTo(exp.toString()));
    }
}
