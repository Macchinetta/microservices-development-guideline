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
/**
 * 
 */
package com.example.m9amsa.reserveNotice.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.reserveNotice.constant.SeatClass;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_RESERVE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
public class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Before
    public void setUp() throws Exception {
        reservationRepository.deleteAll();
        reservationRepository.flush();
    }

    @After
    public void tearDown() throws Exception {
        reservationRepository.deleteAll();
        reservationRepository.flush();
    }

    @Test
    public void testreservationRepository() {

        Passenger passengerInfo = Passenger.builder().name("渡辺太郎").age(31).isMainPassenger(true).build();
        List<Passenger> passengers = new ArrayList<Passenger>();
        passengers.add(passengerInfo);
        Reservation reservation = Reservation.builder().reserveId(1L)
                .departureDate(LocalDate.of(2019, 5, 7)).flightId("NTT01").departureTime(LocalTime.of(10, 05))
                .arrivalTime(LocalTime.of(13, 05)).departureAirportId("HND").arrivalAirportId("ITM")
                .seatClass(SeatClass.N).fareType("片道").fare(13500).passengers(passengers).emailId("0001@ntt.com")
                .build();
        reservationRepository.saveAndFlush(reservation);

        List<Reservation> result = reservationRepository.findAll();
        assertThat("取得されるレコード数は1件であること", result.size(), equalTo(1));

        Reservation resultOne = result.get(0);
        assertThat("reservation.reserveId", resultOne.getReserveId(), equalTo(reservation.getReserveId()));
        assertThat("reservation.departureDate", resultOne.getDepartureDate(),
                equalTo(reservation.getDepartureDate()));
        assertThat("reservation.flightId", resultOne.getFlightId(), equalTo(reservation.getFlightId()));
        assertThat("reservation.departureTime", resultOne.getDepartureTime(),
                equalTo(reservation.getDepartureTime()));
        assertThat("reservation.arrivalTime", resultOne.getArrivalTime(),
                equalTo(reservation.getArrivalTime()));
        assertThat("reservation.departureAirport", resultOne.getDepartureAirportId(),
                equalTo(reservation.getDepartureAirportId()));
        assertThat("reservation.arrivalAirport", resultOne.getArrivalAirportId(),
                equalTo(reservation.getArrivalAirportId()));
        assertThat("reservation.seatClass", resultOne.getSeatClass(), equalTo(reservation.getSeatClass()));
        assertThat("reservation.fareType", resultOne.getFareType(), equalTo(reservation.getFareType()));
        assertThat("reservation.fare", resultOne.getFare(), equalTo(reservation.getFare()));
        assertThat("reservation.emailId", resultOne.getEmailId(), equalTo(reservation.getEmailId()));

    }
}
