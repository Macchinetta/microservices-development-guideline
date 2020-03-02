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
package com.example.m9amsa.reserveNotice.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

import com.example.m9amsa.reserveNotice.constant.SeatClass;
import com.example.m9amsa.reserveNotice.model.topic.PassengerTopic;
import com.example.m9amsa.reserveNotice.model.topic.ReservationTopic;
import com.example.m9amsa.reserveNotice.service.ReservationTopicService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_RESERVE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
public class ReservationTopicListenerTest {

    @Mock
    private ReservationTopicService service;

    @InjectMocks
    private ReservationTopicListener listener;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleReserveTopic() {

        // when
        PassengerTopic passengerInfo1 = PassengerTopic.builder().name("渡辺太郎").age(31).isMainPassenger(true)
                .email("001@ntt.com").build();
        PassengerTopic passengerInfo2 = PassengerTopic.builder().name("渡辺花子").age(31).isMainPassenger(false)
                .build();

        List<PassengerTopic> passengers = new ArrayList<>();
        passengers.add(passengerInfo1);
        passengers.add(passengerInfo2);

        ReservationTopic reservationTopic = ReservationTopic.builder().reserveId(1L)
                .departureDate(LocalDate.of(2019, 5, 7)).flightId("NTT01").departureTime(LocalTime.of(10, 05))
                .arrivalTime(LocalTime.of(13, 05)).departureAirportId("HND").arrivalAirportId("ITM")
                .seatClass(SeatClass.N).fareType("片道").fare(13500).passenger(passengers).build();

        ArgumentCaptor<ReservationTopic> capture = ArgumentCaptor.forClass(ReservationTopic.class);
        doNothing().when(service).registerReservationInfo(capture.capture());

        // do
        listener.handleReserveTopic(reservationTopic);

        // verify
        assertThat("引数がそのままserviceに渡っていることを確認", reservationTopic.toString(), equalTo(capture.getValue().toString()));

    }
}
