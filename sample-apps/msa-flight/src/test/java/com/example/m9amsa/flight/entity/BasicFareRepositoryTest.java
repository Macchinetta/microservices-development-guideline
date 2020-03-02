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
import org.springframework.data.domain.Example;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class BasicFareRepositoryTest {

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
        basicFareRepository.deleteAll();
        basicFareRepository.flush();
    }

    /**
     * BasicFareRepositoryをテスト。
     * 
     * <pre>
     * 正常系テスト。
     * -save
     * -findAll
     * </pre>
     */
    @Test
    public void testBasicFareRepositoryCorrect() {

        Throwable e = catchThrowable(() -> {
            BasicFare basicFare = basicFareRepository
                    .save(BasicFare.builder().departure("OSA").arrival("HND").fare(30000).build());

            basicFareRepository.save(basicFare);
            basicFareRepository.flush();

            List<BasicFare> actualBasicFares = basicFareRepository.findAll();

            assertThat("取得されるレコード数は1件であること", actualBasicFares.size(), equalTo(1));

            BasicFare actualBasicFare = actualBasicFares.get(0);
            assertThat("BasicFare.departure", actualBasicFare.getDeparture(), equalTo("OSA"));
            assertThat("BasicFare.arrival", actualBasicFare.getArrival(), equalTo("HND"));
            assertThat("BasicFare.fare", actualBasicFare.getFare(), equalTo(30000));

            // レコードを2件追加
            basicFareRepository.save(BasicFare.builder().departure("HND").arrival("CTS").fare(40000).build());
            basicFareRepository.flush();

            basicFareRepository.save(BasicFare.builder().departure("CTS").arrival("OSA").fare(50000).build());
            basicFareRepository.flush();

            // 出発空港の条件を確認
            Example<BasicFare> example = Example.of(BasicFare.builder().departure("OSA").build());
            actualBasicFares = basicFareRepository.findAll(example);

            assertThat("取得されるレコード数は1件であること", actualBasicFares.size(), equalTo(1));

            actualBasicFare = actualBasicFares.get(0);
            assertThat("BasicFare.departure", actualBasicFare.getDeparture(), equalTo("OSA"));
            assertThat("BasicFare.arrival", actualBasicFare.getArrival(), equalTo("HND"));
            assertThat("BasicFare.fare", actualBasicFare.getFare(), equalTo(30000));

            // 到着空港の条件を確認
            example = Example.of(BasicFare.builder().arrival("CTS").build());
            actualBasicFares = basicFareRepository.findAll(example);

            assertThat("取得されるレコード数は1件であること", actualBasicFares.size(), equalTo(1));

            actualBasicFare = actualBasicFares.get(0);
            assertThat("BasicFare.departure", actualBasicFare.getDeparture(), equalTo("HND"));
            assertThat("BasicFare.arrival", actualBasicFare.getArrival(), equalTo("CTS"));
            assertThat("BasicFare.fare", actualBasicFare.getFare(), equalTo(40000));

            // 出発／到着空港の条件を確認
            example = Example.of(BasicFare.builder().departure("CTS").arrival("OSA").build());
            actualBasicFares = basicFareRepository.findAll(example);

            assertThat("取得されるレコード数は1件であること", actualBasicFares.size(), equalTo(1));

            actualBasicFare = actualBasicFares.get(0);
            assertThat("BasicFare.departure", actualBasicFare.getDeparture(), equalTo("CTS"));
            assertThat("BasicFare.arrival", actualBasicFare.getArrival(), equalTo("OSA"));
            assertThat("BasicFare.fare", actualBasicFare.getFare(), equalTo(50000));

        });

        if (e != null)
            e.printStackTrace();
        assertNull("予期しない例外が発生：", e);
    }

    /**
     * BasicFareRepositoryをテスト。
     * 
     * <pre>
     * 異常系テスト。
     * -save
     * -findAll
     * </pre>
     */
    @Test
    public void testBasicFareRepositoryError() {

        // 必須項目が足りない場合、エラーとなること
        ConstraintViolationException cve = catchThrowableOfType(() -> {
            BasicFare basicFare = BasicFare.builder() //
                    .departure("HND") //
                    .departure("CTS") //
                    .build();

            basicFareRepository.save(basicFare);
            basicFareRepository.flush();
        }, ConstraintViolationException.class);

        assertNotNull("ConstraintViolationExceptionが発生しない", cve);

        cve.getConstraintViolations().forEach(v -> v.getPropertyPath().toString());
        Map<String, ConstraintViolation<?>> violations = cve.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v));

        // エラーが発生しているフィールドを確認
        assertThat("fare", violations.keySet(), hasItem("fare"));

    }
}
