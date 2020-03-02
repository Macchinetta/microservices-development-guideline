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
package com.example.m9amsa.flight.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flight.entity.Airplane;
import com.example.m9amsa.flight.entity.AirplaneRepository;
import com.example.m9amsa.flight.entity.Airport;
import com.example.m9amsa.flight.entity.AirportRepository;
import com.example.m9amsa.flight.entity.BasicFare;
import com.example.m9amsa.flight.entity.BasicFarePk;
import com.example.m9amsa.flight.entity.BasicFareRepository;
import com.example.m9amsa.flight.entity.Flight;
import com.example.m9amsa.flight.entity.FlightRepository;
import com.example.m9amsa.flight.model.FlightUpdateInfo;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class FlightServiceTest {

    @Autowired
    private FlightService flightService;

    @MockBean
    private FlightRepository flightRepository;

    @MockBean
    private AirplaneRepository airplaneRepository;

    @MockBean
    private AirportRepository airportRepository;

    @MockBean
    private BasicFareRepository basicFareRepository;

    @Captor
    private ArgumentCaptor<Flight> flightCaptor;

    @Captor
    private ArgumentCaptor<Example<Flight>> exampleCaptor;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(flightRepository);
        reset(airplaneRepository);
        reset(airportRepository);
        reset(basicFareRepository);
    }

    /**
     * Test for addFlight.
     */
    @Test
    public void testAddFlight() {
        LocalDateTime departureTime = LocalDateTime.parse("1901-01-01T10:00:00");
        LocalDateTime arrivalTime = LocalDateTime.parse("1901-01-01T11:30:00");

        Airplane airplane = Airplane.builder().id(1L).name("B777").standardSeats(200).specialSeats(50).build();
        when(airplaneRepository.findById(1L)).thenReturn(Optional.of(airplane));

        Airport airportHnd = Airport.builder().id("HND").name("東京").build();
        when(airportRepository.findById("HND")).thenReturn(Optional.of(airportHnd));

        Airport airportOsa = Airport.builder().id("OSA").name("大阪").build();
        when(airportRepository.findById("OSA")).thenReturn(Optional.of(airportOsa));

        BasicFare basicFare = BasicFare.builder().departure("HND").arrival("OSA").fare(30000).build();
        when(basicFareRepository.findById(BasicFarePk.builder().departure("HND").arrival("OSA").build()))
                .thenReturn(Optional.of(basicFare));

        Flight flight = Flight.builder().name("MSA001").airplane(airplane).departureAirport(airportHnd)
                .departureTime(departureTime).arrivalAirport(airportOsa).arrivalTime(arrivalTime).basicFare(basicFare)
                .build();

        when(flightRepository.save(any(Flight.class))).then(i -> i.getArgument(0));

        FlightUpdateInfo flightUpdateModel = FlightUpdateInfo.builder().name("MSA001").airplaneId(1L)
                .departureTime(departureTime).departureAirportId("HND").arrivalTime(arrivalTime).arrivalAirportId("OSA")
                .build();

        clearInvocations(flightRepository);
        Flight actualFlight = flightService.addFlight(flightUpdateModel);

        verify(flightRepository).save(flightCaptor.capture());

        assertThat("flightRepositoryへのパラメータが正しいこと", flightCaptor.getValue(), equalTo(flight));

        assertThat("Flight", actualFlight, equalTo(flight));
    }

    /**
     * Test for findFlightList.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testFindFlightList() {
        LocalDateTime departureTime = LocalDateTime.parse("1901-01-01T10:00:00");
        LocalDateTime arrivalTime = LocalDateTime.parse("1901-01-01T11:30:00");

        Airplane airplane = Airplane.builder().id(1L).name("B777").standardSeats(200).specialSeats(50).build();

        Airport airportHnd = Airport.builder().id("HND").name("東京").build();
        when(airportRepository.findById("HND")).thenReturn(Optional.of(airportHnd));

        Airport airportOsa = Airport.builder().id("OSA").name("大阪").build();
        when(airportRepository.findById("OSA")).thenReturn(Optional.of(airportOsa));

        BasicFare basicFare = BasicFare.builder().departure("HND").arrival("OSA").fare(30000).build();

        Flight flight = Flight.builder().name("MSA001").airplane(airplane).departureAirport(airportHnd)
                .departureTime(departureTime).arrivalAirport(airportOsa).arrivalTime(arrivalTime).basicFare(basicFare)
                .build();

        Airplane returnAirplane = Airplane.builder().id(1L).name("B888").standardSeats(300).specialSeats(150).build();

        BasicFare returnBasicFare = BasicFare.builder().departure("OSA").arrival("HND").fare(30000).build();

        Flight returnFlight = Flight.builder().name("MSA002").airplane(returnAirplane).departureAirport(airportOsa)
                .departureTime(arrivalTime).arrivalAirport(airportHnd).arrivalTime(departureTime)
                .basicFare(returnBasicFare).build();

        Example<Flight> dummyExp = Example.of(flight);
        when(flightRepository.findAll(any(dummyExp.getClass()))).thenReturn(Arrays.asList(flight, returnFlight));

        // パラメータ null, null
        clearInvocations(flightRepository);
        List<Flight> actualList = flightService.findFlightList(Optional.empty(), Optional.empty());

        verify(flightRepository).findAll(exampleCaptor.capture());

        assertThat("検索条件が正しいこと", exampleCaptor.getValue().getProbe(), equalTo(Flight.builder().build()));

        assertThat("リストサイズは2であること", actualList.size(), equalTo(2));
        assertThat("リストの内容、順序が正しいこと", actualList, contains(flight, returnFlight));

        // パラメータ HND, null
        clearInvocations(flightRepository);
        actualList = flightService.findFlightList(Optional.of("HND"), Optional.empty());

        verify(flightRepository).findAll(exampleCaptor.capture());

        assertThat("検索条件が正しいこと", exampleCaptor.getValue().getProbe(),
                equalTo(Flight.builder().departureAirport(airportHnd).build()));

        // パラメータ null, OSA
        clearInvocations(flightRepository);
        actualList = flightService.findFlightList(Optional.empty(), Optional.of("OSA"));

        verify(flightRepository).findAll(exampleCaptor.capture());

        assertThat("検索条件が正しいこと", exampleCaptor.getValue().getProbe(),
                equalTo(Flight.builder().arrivalAirport(airportOsa).build()));

        // パラメータ HND, OSA
        clearInvocations(flightRepository);
        actualList = flightService.findFlightList(Optional.of("HND"), Optional.of("OSA"));

        verify(flightRepository).findAll(exampleCaptor.capture());

        assertThat("検索条件が正しいこと", exampleCaptor.getValue().getProbe(),
                equalTo(Flight.builder().departureAirport(airportHnd).arrivalAirport(airportOsa).build()));
    }
}
