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
public class AirplaneRepositoryTest {

    @Autowired
    private AirplaneRepository airplaneRepository;

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
        airplaneRepository.deleteAll();
        airplaneRepository.flush();
    }

    /**
     * AirplaneRepositoryをテスト。
     * 
     * <pre>
     * 正常系テスト。
     * -save
     * -findAll
     * </pre>
     */
    @Test
    public void testAirplaneRepositoryCorrect() {

        Throwable e = catchThrowable(() -> {
            Airplane airplane = airplaneRepository
                    .save(Airplane.builder().name("B777").standardSeats(200).specialSeats(50).build());

            airplaneRepository.save(airplane);
            airplaneRepository.flush();

            List<Airplane> actualAirplanes = airplaneRepository.findAll();

            assertThat("取得されるレコード数は1件であること", actualAirplanes.size(), equalTo(1));

            Airplane actualAirplane = actualAirplanes.get(0);
            assertThat("Airplane.name", actualAirplane.getName(), equalTo("B777"));
            assertThat("Airplane.standardSeats", actualAirplane.getStandardSeats(), equalTo(200));
            assertThat("Airplane.specialSeats", actualAirplane.getSpecialSeats(), equalTo(50));
        });

        if (e != null)
            e.printStackTrace();
        assertNull("予期しない例外が発生：", e);
    }

    /**
     * AirplaneRepositoryをテスト。
     * 
     * <pre>
     * 異常系テスト。
     * -save
     * -findAll
     * </pre>
     */
    @Test
    public void testAirplaneRepositoryError() {

        // 必須項目が足りない場合、エラーとなること
        ConstraintViolationException cve = catchThrowableOfType(() -> {
            Airplane airplane = Airplane.builder() //
                    .name("B777") //
                    .build();

            airplaneRepository.save(airplane);
            airplaneRepository.flush();
        }, ConstraintViolationException.class);

        assertNotNull("ConstraintViolationExceptionが発生しない", cve);

        cve.getConstraintViolations().forEach(v -> v.getPropertyPath().toString());
        Map<String, ConstraintViolation<?>> violations = cve.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v));

        // エラーが発生しているフィールドを確認
        assertThat("standardSeats", violations.keySet(), hasItem("standardSeats"));
        assertThat("specialSeats", violations.keySet(), hasItem("specialSeats"));

    }
}
