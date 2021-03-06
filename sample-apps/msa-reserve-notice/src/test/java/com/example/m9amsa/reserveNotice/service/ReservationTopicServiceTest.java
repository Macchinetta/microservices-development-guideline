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
package com.example.m9amsa.reserveNotice.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.reserveNotice.constant.SeatClass;
import com.example.m9amsa.reserveNotice.entity.Reservation;
import com.example.m9amsa.reserveNotice.entity.ReservationRepository;
import com.example.m9amsa.reserveNotice.model.topic.PassengerTopic;
import com.example.m9amsa.reserveNotice.model.topic.ReservationTopic;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_RESERVE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
public class ReservationTopicServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTopicService reservationTopicService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        reservationRepository.deleteAll();
        reservationRepository.flush();
    }

    @After
    public void tearDown() throws Exception {
        MockitoAnnotations.initMocks(this);
        reservationRepository.deleteAll();
        reservationRepository.flush();
    }

    @Test
    public void testRegisterReservationInfo() {
        PassengerTopic passengerInfo1 = PassengerTopic.builder().name("渡辺太郎").age(31).isMainPassenger(true)
                .email("001@ntt.com").telephoneNo("123456789").build();
        PassengerTopic passengerInfo2 = PassengerTopic.builder().name("渡辺花子").age(31).isMainPassenger(false).build();

        List<PassengerTopic> passengers = new ArrayList<>();
        passengers.add(passengerInfo1);
        passengers.add(passengerInfo2);

        ReservationTopic reservation = ReservationTopic.builder().reserveId(1L).departureDate(LocalDate.of(2019, 5, 7))
                .flightId("NTT01").departureTime(LocalTime.of(10, 05)).arrivalTime(LocalTime.of(13, 05))
                .departureAirportId("HND").arrivalAirportId("PVD").seatClass(SeatClass.N).fareType("片道").fare(13500)
                .passenger(passengers).build();

        reservationTopicService.registerReservationInfo(reservation);

        List<Reservation> result = reservationRepository.findAll();
        Reservation result0 = result.get(0);

        assertThat("取得されるレコード数は1件であること", result.size(), equalTo(1));

        assertThat("予約Idが作成されていること", result0.getReserveId(), equalTo(1L));
        assertThat("出発時刻が作成されていること", result0.getDepartureTime(), equalTo(LocalTime.of(10, 05)));
        assertThat("出発日が作成されていること", result0.getDepartureDate(), equalTo(LocalDate.of(2019, 5, 7)));
        assertThat("便Idが作成されていること", result0.getFlightId(), equalTo("NTT01"));
        assertThat("到着時刻が作成されていること", result0.getArrivalTime(), equalTo(LocalTime.of(13, 05)));
        assertThat("出発空港が作成されていること", result0.getDepartureAirportId(), equalTo("HND"));
        assertThat("到着空港が作成されていること", result0.getArrivalAirportId(), equalTo("PVD"));
        assertThat("搭乗クラス種別が作成されていること", result0.getSeatClass(), equalTo(SeatClass.N));
        assertThat("運賃種別が作成されていること", result0.getFareType(), equalTo("片道"));
        assertThat("運賃が作成されていること", result0.getFare(), equalTo(13500));
        assertThat("搭乗者名前が作成されていること", result0.getPassengers().get(0).getName(), equalTo("渡辺太郎"));
        assertThat("搭乗者年齢が作成されていること", result0.getPassengers().get(0).getAge(), equalTo(31));
        assertThat("電話番号が作成されていること", result0.getPassengers().get(0).getTelephoneNo(), equalTo("123456789"));
        assertThat("代表搭乗者フラグが作成されていること", result0.getPassengers().get(0).isMainPassenger(), equalTo(true));
        assertNotNull("搭乗者Idが作成されていること", result0.getPassengers().get(0).getPassengerInfoId());
        assertThat("搭乗者名前が作成されていること", result0.getPassengers().get(1).getName(), equalTo("渡辺花子"));
        assertThat("搭乗者年齢が作成されていること", result0.getPassengers().get(1).getAge(), equalTo(31));
        assertThat("同時搭乗者フラグが作成されていること", result0.getPassengers().get(1).isMainPassenger(), equalTo(false));
        assertNotNull("搭乗者Idが作成されていること", result0.getPassengers().get(1).getPassengerInfoId());
        assertThat("メールアドレスが作成されていること", result0.getEmailId(), equalTo("001@ntt.com"));


    }
}
