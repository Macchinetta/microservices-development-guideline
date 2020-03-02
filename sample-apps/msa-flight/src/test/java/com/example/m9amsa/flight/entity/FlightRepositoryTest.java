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
package com.example.m9amsa.flight.entity;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Example;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class FlightRepositoryTest {

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private AirplaneRepository airplaneRepository;

    @Autowired
    private BasicFareRepository basicFareRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Before
    public void before() {
        deleteAll();
    }

    @After
    public void after() {
        try {
            deleteAll();
        } catch (Exception e) {
            // 途中でロールバックが発生（例外発生）している場合があるので
            // @Afterでの例外は握りつぶす
        }
    }

    private void deleteAll() {
        flightRepository.deleteAll();
        flightRepository.flush();
        airportRepository.deleteAll();
        airportRepository.flush();
        airplaneRepository.deleteAll();
        airplaneRepository.flush();
        basicFareRepository.deleteAll();
        basicFareRepository.flush();
    }

    /**
     * FlightRepositoryをテスト。
     * 
     * <pre>
     * 正常系テスト。
     * -save
     * -findAll
     * </pre>
     */
    @Test
    public void testFlightRepositoryCorrect() {

        Throwable e = catchThrowable(() -> {
            Airport departureAirport = airportRepository.save(Airport.builder().id("OSA").name("大阪").build());
            Airport arrivalAirport = airportRepository.save(Airport.builder().id("HND").name("東京").build());
            Airplane airplane = airplaneRepository
                    .save(Airplane.builder().name("B777").standardSeats(200).specialSeats(50).build());
            BasicFare basicFare = basicFareRepository
                    .save(BasicFare.builder().departure("OSA").arrival("HND").fare(30000).build());

            LocalDateTime departureTime = LocalDateTime.parse("1901-01-01T10:00:00");
            LocalDateTime arrivalTime = LocalDateTime.parse("1901-01-01T11:30:00");
            Flight flight = Flight.builder() //
                    .name("MSA001") //
                    .departureTime(departureTime) //
                    .departureAirport(departureAirport) //
                    .arrivalTime(arrivalTime) //
                    .arrivalAirport(arrivalAirport) //
                    .airplane(airplane) //
                    .basicFare(basicFare) //
                    .build();

            flightRepository.save(flight);
            flightRepository.flush();

            List<Flight> actualFlights = flightRepository.findAll();

            assertThat("取得されるレコード数は1件であること", actualFlights.size(), equalTo(1));

            Flight actualFlight = actualFlights.get(0);
            assertThat("Flight.name", actualFlight.getName(), equalTo("MSA001"));
            assertThat("Flight.departureTime", actualFlight.getDepartureTime(), equalTo(departureTime));
            assertThat("Flight.departureAirport", actualFlight.getDepartureAirport(), equalTo(departureAirport));
            assertThat("Flight.arrivalTime", actualFlight.getArrivalTime(), equalTo(arrivalTime));
            assertThat("Flight.arrivaleAirport", actualFlight.getArrivalAirport(), equalTo(arrivalAirport));
            assertThat("Flight.airplane", actualFlight.getAirplane(), equalTo(airplane));
            assertThat("Flight.basicFare", actualFlight.getBasicFare(), equalTo(basicFare));

            // レコードを2件追加
            departureAirport = airportRepository.save(Airport.builder().id("HND").name("東京").build());
            arrivalAirport = airportRepository.save(Airport.builder().id("CTS").name("札幌").build());
            basicFare = basicFareRepository
                    .save(BasicFare.builder().departure("HND").arrival("CTS").fare(30000).build());

            flight = Flight.builder() //
                    .name("MSA002") //
                    .departureTime(departureTime) //
                    .departureAirport(departureAirport) //
                    .arrivalTime(arrivalTime) //
                    .arrivalAirport(arrivalAirport) //
                    .airplane(airplane) //
                    .basicFare(basicFare) //
                    .build();

            flightRepository.save(flight);
            flightRepository.flush();

            departureAirport = airportRepository.save(Airport.builder().id("CTS").name("札幌").build());
            arrivalAirport = airportRepository.save(Airport.builder().id("OSA").name("大阪").build());
            basicFare = basicFareRepository
                    .save(BasicFare.builder().departure("CTS").arrival("OSA").fare(30000).build());

            flight = Flight.builder() //
                    .name("MSA003") //
                    .departureTime(departureTime) //
                    .departureAirport(departureAirport) //
                    .arrivalTime(arrivalTime) //
                    .arrivalAirport(arrivalAirport) //
                    .airplane(airplane) //
                    .basicFare(basicFare) //
                    .build();

            flightRepository.save(flight);
            flightRepository.flush();

            // 出発空港の条件を確認
            Example<Flight> example = Example
                    .of(Flight.builder().departureAirport(Airport.builder().id("OSA").build()).build());
            actualFlights = flightRepository.findAll(example);

            assertThat("取得されるレコード数は1件であること", actualFlights.size(), equalTo(1));

            actualFlight = actualFlights.get(0);
            assertThat("Flight.name", actualFlight.getName(), equalTo("MSA001"));

            // 到着空港の条件を確認
            example = Example.of(Flight.builder().arrivalAirport(Airport.builder().id("CTS").build()).build());
            actualFlights = flightRepository.findAll(example);

            assertThat("取得されるレコード数は1件であること", actualFlights.size(), equalTo(1));

            actualFlight = actualFlights.get(0);
            assertThat("Flight.name", actualFlight.getName(), equalTo("MSA002"));

            // 出発／到着空港の条件を確認
            example = Example.of(Flight.builder().departureAirport(Airport.builder().id("CTS").build())
                    .arrivalAirport(Airport.builder().id("OSA").build()).build());
            actualFlights = flightRepository.findAll(example);

            assertThat("取得されるレコード数は1件であること", actualFlights.size(), equalTo(1));

            actualFlight = actualFlights.get(0);
            assertThat("Flight.name", actualFlight.getName(), equalTo("MSA003"));

        });

        if (e != null)
            e.printStackTrace();
        assertNull("予期しない例外が発生：", e);
    }

    /**
     * FlightRepositoryをテスト。
     * 
     * <pre>
     * 異常系テスト。
     * -save
     * -findAll
     * </pre>
     */
    @Test
    public void testFlightRepositoryError() {

        // 必須項目が足りない場合、エラーとなること
        ConstraintViolationException cve = catchThrowableOfType(() -> {
            Flight flight = Flight.builder() //
                    .name("MSA001") //
                    .build();

            flightRepository.save(flight);
            flightRepository.flush();
        }, ConstraintViolationException.class);

        assertNotNull("ConstraintViolationExceptionが発生しない", cve);

        cve.getConstraintViolations().forEach(v -> v.getPropertyPath().toString());
        Map<String, ConstraintViolation<?>> violations = cve.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v));

        // エラーが発生しているフィールドを確認
        assertThat("departureTime", violations.keySet(), hasItem("departureTime"));
        assertThat("departureAirport", violations.keySet(), hasItem("departureAirport"));
        assertThat("arrivalTime", violations.keySet(), hasItem("arrivalTime"));
        assertThat("arrivalAirport", violations.keySet(), hasItem("arrivalAirport"));
        assertThat("airplane", violations.keySet(), hasItem("airplane"));
        assertThat("basicFare", violations.keySet(), hasItem("basicFare"));

    }
}
