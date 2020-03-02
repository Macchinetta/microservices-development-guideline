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
package com.example.m9amsa.reserve.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDate;
import java.time.LocalTime;

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

import com.example.m9amsa.reserve.model.topic.AirplaneTopic;
import com.example.m9amsa.reserve.model.topic.AirportTopic;
import com.example.m9amsa.reserve.model.topic.BasicFareTopic;
import com.example.m9amsa.reserve.model.topic.FlightTopic;
import com.example.m9amsa.reserve.model.topic.FlightVacantSeatTopic;
import com.example.m9amsa.reserve.service.FlightTopicService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "HOSTNAME_CALCULATE_FARE=localhost:28080",
        "HOSTNAME_FLIGHT=localhost:28081", "HOSTNAME_PURCHASE=localhost:28082", "DB_HOSTNAME_RESERVE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_ACCOUNT=localhost", "OAUTH2_CLIENT_ID=my-client", "OAUTH2_CLIENT_SECRET=1" })
public class FlightTopicListenerTest {

    @Mock
    FlightTopicService service;

    @InjectMocks
    FlightTopicListener listener;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * hadnleFlight()
     */
    @Test
    public void testHadnleFlight() throws Exception {
        // when
        FlightTopic input = FlightTopic.builder().name("TEST001").departureTime(LocalTime.of(9, 0))
                .arrivalTime(LocalTime.of(10, 30)).departureAirportId("HND").arrivalAirportId("KIX").airplaneId(1L)
                .build();

        ArgumentCaptor<FlightTopic> capture = ArgumentCaptor.forClass(FlightTopic.class);
        doNothing().when(service).registerFlightInfo(capture.capture());

        // do
        listener.handleFlight(input);

        // verify
        assertThat("引数がそのままserviceに渡っていることを確認", input.toString(), equalTo(capture.getValue().toString()));
    }

    /**
     * handleAirport()
     */
    @Test
    public void testHandleAirport() {
        // when
        AirportTopic input = AirportTopic.builder().id("HND").name("羽田").build();

        ArgumentCaptor<AirportTopic> capture = ArgumentCaptor.forClass(AirportTopic.class);
        doNothing().when(service).registerAirport(capture.capture());

        // do
        listener.handleAirport(input);

        // verify
        assertThat("引数がそのままserviceに渡っていることを確認", input.toString(), equalTo(capture.getValue().toString()));
    }

    /**
     * handleAirplane()
     */
    @Test
    public void testHandleAirplane() {
        // when
        AirplaneTopic input = AirplaneTopic.builder().id(1L).name("Boeing 777").specialSeats(96).standardSeats(270)
                .build();

        ArgumentCaptor<AirplaneTopic> capture = ArgumentCaptor.forClass(AirplaneTopic.class);
        doNothing().when(service).registerAirplane(capture.capture());

        // do
        listener.handleAirplane(input);

        // verify
        assertThat("引数がそのままserviceに渡っていることを確認", input.toString(), equalTo(capture.getValue().toString()));
    }

    /**
     * handleBasicFare()
     */
    @Test
    public void testHandleBasicFare() {
        // when
        BasicFareTopic input = BasicFareTopic.builder().arrivalAirportId("HND").departureAirportId("KIX").fare(10520)
                .build();

        ArgumentCaptor<BasicFareTopic> capture = ArgumentCaptor.forClass(BasicFareTopic.class);
        doNothing().when(service).registerBasicFare(capture.capture());

        // do
        listener.handleBasicFare(input);

        // verify
        assertThat("引数がそのままserviceに渡っていることを確認", input.toString(), equalTo(capture.getValue().toString()));
    }

    /**
     * handleFlightVacantSeatInfo()
     */
    @Test
    public void testHandleFlightVacantSeatInfo() {
        // when
        FlightVacantSeatTopic input = FlightVacantSeatTopic.builder().departureDate(LocalDate.now())
                .flightName("TEST001").standardSeats(20).specialSeats(10).build();

        ArgumentCaptor<FlightVacantSeatTopic> capture = ArgumentCaptor.forClass(FlightVacantSeatTopic.class);
        doNothing().when(service).registerFlightVacantSeat(capture.capture());

        // do
        listener.handleFlightVacantSeatInfo(input);

        // verify
        assertThat("引数がそのままserviceに渡っていることを確認", input.toString(), equalTo(capture.getValue().toString()));
    }
}
