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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class AirportRepositoryTest {

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Before
    public void before() {
        flightRepository.deleteAll();
        flightRepository.flush();
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
        airportRepository.deleteAll();
        airportRepository.flush();
    }

    /**
     * AirportRepositoryをテスト。
     * 
     * <pre>
     * 正常系テスト。
     * -save
     * -findAll
     * </pre>
     */
    @Test
    public void testAirportRepositoryCorrect() {

        Throwable e = catchThrowable(() -> {
            Airport airport = airportRepository.save(Airport.builder().id("HND").name("東京").build());

            airportRepository.save(airport);
            airportRepository.flush();

            List<Airport> actualAirports = airportRepository.findAll();

            assertThat("取得されるレコード数は1件であること", actualAirports.size(), equalTo(1));

            Airport actualAirport = actualAirports.get(0);
            assertThat("Airport.id", actualAirport.getId(), equalTo("HND"));
            assertThat("Airport.name", actualAirport.getName(), equalTo("東京"));

            // test findById
            actualAirport = airportRepository.findById("HND").get();
            assertThat("Airport.id", actualAirport.getId(), equalTo("HND"));
            assertThat("Airport.name", actualAirport.getName(), equalTo("東京"));

        });

        if (e != null)
            e.printStackTrace();
        assertNull("予期しない例外が発生：", e);
    }

    /**
     * AirportRepositoryをテスト。
     * 
     * <pre>
     * 異常系テスト。
     * -save
     * -findAll
     * </pre>
     */
    @Test
    public void testAirportRepositoryError() {

        // 必須項目が足りない場合、エラーとなること
        ConstraintViolationException cve = catchThrowableOfType(() -> {
            Airport airport = Airport.builder() //
                    .id("HND") //
                    .build();

            airportRepository.save(airport);
            airportRepository.flush();
        }, ConstraintViolationException.class);

        assertNotNull("ConstraintViolationExceptionが発生しない", cve);

        cve.getConstraintViolations().forEach(v -> v.getPropertyPath().toString());
        Map<String, ConstraintViolation<?>> violations = cve.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v));

        // エラーが発生しているフィールドを確認
        assertThat("name", violations.keySet(), hasItem("name"));

    }
}
