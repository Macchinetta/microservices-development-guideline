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
package com.example.m9amsa.reserve.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.reserve.constant.SeatClass;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "HOSTNAME_CALCULATE_FARE=localhost:28080",
        "DB_HOSTNAME_RESERVE=localhost:5432", "HOSTNAME_FLIGHT=localhost:28081", "HOSTNAME_PURCHASE=localhost:28082",
        "JAEGER_HOST=localhost", "HOSTNAME_ACCOUNT=localhost", "OAUTH2_CLIENT_ID=my-client", "OAUTH2_CLIENT_SECRET=1" })
public class ReservationInfoRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Before
    public void setUp() throws Exception {
        reservationRepository.deleteAll();
        reservationRepository.flush();
    }

    @After
    public void after() {
        reservationRepository.deleteAll();
        reservationRepository.flush();
    }

    @Test
    public void testReservationInfoRepository_save() {

        Passenger mainPassengerInfo = Passenger.builder().name("渡辺１").age(25).telephoneNo("080-1234-5678")
                .email("abc@example.com").isMainPassenger(true).build();

        Passenger subPassengerInfo = Passenger.builder().name("渡辺２").age(20).telephoneNo("080-5678-1234")
                .email("def@example.com").isMainPassenger(false).build();
        List<Passenger> subPassengerInfoList = new ArrayList<Passenger>();
        subPassengerInfoList.add(subPassengerInfo);

        Reservation exp = Reservation.builder().reserveTime(LocalDateTime.of(2019, 1, 1, 12, 0))
                .departureDate(LocalDate.of(2019, 1, 1)).departureTime(LocalTime.of(8, 0))
                .arrivalTime(LocalTime.of(12, 0)).flightId("TEST001").departureAirportId("HND").arrivalAirportId("KIX")
                .seatClass(SeatClass.N).fareType("早期割").fare(10520).mainPassenger(mainPassengerInfo)
                .subPassengers(subPassengerInfoList).build();
        reservationRepository.saveAndFlush(exp);

        List<Reservation> result = reservationRepository.findAll();
        assertThat("取得されるレコード数は1件であること", result.size(), equalTo(1));

        Reservation resultOne = result.get(0);
        assertNotNull("Reservation.id", resultOne.getReserveId());
        assertThat("Reservation.reserveTime", resultOne.getReserveTime(), equalTo(exp.getReserveTime()));
        assertThat("Reservation.departureDate", resultOne.getDepartureDate(), equalTo(exp.getDepartureDate()));
        assertThat("Reservation.departureTime", resultOne.getDepartureTime(), equalTo(exp.getDepartureTime()));
        assertThat("Reservation.arrivalTime", resultOne.getArrivalTime(), equalTo(exp.getArrivalTime()));
        assertThat("Reservation.flightId", resultOne.getFlightId(), equalTo(exp.getFlightId()));
        assertThat("Reservation.departureAirport", resultOne.getDepartureAirportId(),
                equalTo(exp.getDepartureAirportId()));
        assertThat("Reservation.arrivalAirport", resultOne.getArrivalAirportId(),
                equalTo(exp.getArrivalAirportId()));
        assertThat("Reservation.seatClass", resultOne.getSeatClass(), equalTo(exp.getSeatClass()));
        assertThat("Reservation.fareType", resultOne.getFareType(), equalTo(exp.getFareType()));
        assertThat("Reservation.fare", resultOne.getFare(), equalTo(exp.getFare()));

        List<Passenger> passengers = passengerRepository.findAll(new Sort(Direction.ASC, "passengerId"));
        assertNotNull("MainPassengerInfo is exists.", passengers.size() > 0);
        Passenger mainPassengerInfoResultOne = passengers.get(0);
        Passenger expMainPassengerInfo = exp.getMainPassenger();
        assertThat("Reservation.MainPassengerInfo.name", mainPassengerInfoResultOne.getName(),
                equalTo(expMainPassengerInfo.getName()));
        assertThat("Reservation.MainPassengerInfo.age", mainPassengerInfoResultOne.getAge(),
                equalTo(expMainPassengerInfo.getAge()));
        assertThat("Reservation.MainPassengerInfo.telephoneNo", mainPassengerInfoResultOne.getTelephoneNo(),
                equalTo(expMainPassengerInfo.getTelephoneNo()));
        assertThat("Reservation.MainPassengerInfo.email", mainPassengerInfoResultOne.getEmail(),
                equalTo(expMainPassengerInfo.getEmail()));

        assertNotNull("SubPassengerInfo is exists.", passengers.size() > 1);
        Passenger subPassengerInfoResultOne = passengers.get(1);
        Passenger expSubPassengerInfo = exp.getSubPassengers().get(0);
        assertThat("Reservation.SubPassengerInfo.name", subPassengerInfoResultOne.getName(),
                equalTo(expSubPassengerInfo.getName()));
        assertThat("Reservation.SubPassengerInfo.age", subPassengerInfoResultOne.getAge(),
                equalTo(expSubPassengerInfo.getAge()));
        assertThat("Reservation.SubPassengerInfo.telephoneNo", subPassengerInfoResultOne.getTelephoneNo(),
                equalTo(expSubPassengerInfo.getTelephoneNo()));
        assertThat("Reservation.SubPassengerInfo.email", subPassengerInfoResultOne.getEmail(),
                equalTo(expSubPassengerInfo.getEmail()));

    }

    @Test
    public void testReservationInfoRepository_delete() {
        Passenger mainPassengerInfo = Passenger.builder().name("渡辺１").age(25).telephoneNo("080-1234-5678")
                .email("abc@example.com").isMainPassenger(true).build();

        Passenger subPassengerInfo = Passenger.builder().name("渡辺２").age(20).telephoneNo("080-5678-1234")
                .email("def@example.com").isMainPassenger(false).build();
        List<Passenger> subPassengerInfoList = new ArrayList<Passenger>();
        subPassengerInfoList.add(subPassengerInfo);
        Reservation exp = Reservation.builder().reserveTime(LocalDateTime.now()).departureDate(LocalDate.now())
                .departureTime(LocalTime.now()).arrivalTime(LocalTime.now()).flightId("TEST001")
                .departureAirportId("HND").arrivalAirportId("KIX").seatClass(SeatClass.N).fareType("早期割").fare(10520)
                .mainPassenger(mainPassengerInfo).subPassengers(subPassengerInfoList).build();
        reservationRepository.saveAndFlush(exp);

        List<Reservation> result = reservationRepository.findAll();
        assertThat("取得されるレコード数は1件であること", result.size(), equalTo(1));

        Reservation resultOne = result.get(0);
        assertNotNull("Reservation.id", resultOne.getReserveId());
        reservationRepository.delete(resultOne);
        Optional<Reservation> noResult = reservationRepository.findById(resultOne.getReserveId());
        assertFalse(noResult.isPresent());
    }
}
