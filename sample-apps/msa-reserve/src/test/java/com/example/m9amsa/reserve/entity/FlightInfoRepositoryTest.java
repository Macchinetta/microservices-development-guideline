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
import static org.junit.Assert.assertThat;

import java.time.LocalTime;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "HOSTNAME_CALCULATE_FARE=localhost:28080",
        "HOSTNAME_FLIGHT=localhost:28081", "HOSTNAME_PURCHASE=localhost:28082", "DB_HOSTNAME_RESERVE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_ACCOUNT=localhost", "OAUTH2_CLIENT_ID=my-client", "OAUTH2_CLIENT_SECRET=1" })
public class FlightInfoRepositoryTest {

    @Autowired
    FlightRepository flightRepository;

    @Before
    public void setUp() throws Exception {
        flightRepository.deleteAll();
        flightRepository.flush();
    }

    @After
    public void tearDown() throws Exception {
        flightRepository.deleteAll();
        flightRepository.flush();
    }

    /**
     * FlightInfoRepositoryをテスト。
     * 
     * <pre>
     * -save - findAll
     * </pre>
     */
    @Test
    public void testFlightInfoRepository() {

        Flight exp = Flight.builder().airplaneName("Boeing 777").arrivalAirportId("HND")
                .arrivalTime(LocalTime.of(10, 0)).departureAirportId("KIX").departureTime(LocalTime.of(11, 30))
                .fare(10520).name("TEST001").specialSeats(96).standardSeats(279).build();
        flightRepository.save(exp);

        List<Flight> result = flightRepository.findAll();
        assertThat("取得されるレコード数は1件であること", result.size(), equalTo(1));

        Flight resultOne = result.get(0);
        assertThat("Flight.flightId", resultOne.getName(), equalTo(exp.getName()));
        assertThat("Flight.departureAirport", resultOne.getDepartureAirportId(), equalTo(exp.getDepartureAirportId()));
        assertThat("Flight.arrivalAirport", resultOne.getArrivalAirportId(), equalTo(exp.getArrivalAirportId()));
        assertThat("Flight.departureTime", resultOne.getDepartureTime(), equalTo(exp.getDepartureTime()));
        assertThat("Flight.arrivalTime", resultOne.getArrivalTime(), equalTo(exp.getArrivalTime()));
        assertThat("Flight.airplaneName", resultOne.getAirplaneName(), equalTo(exp.getAirplaneName()));
        assertThat("Flight.standardSeats", resultOne.getStandardSeats(), equalTo(exp.getStandardSeats()));
        assertThat("Flight.specialSeats", resultOne.getSpecialSeats(), equalTo(exp.getSpecialSeats()));
        assertThat("Flight.fare", resultOne.getFare(), equalTo(exp.getFare()));
    }

}
