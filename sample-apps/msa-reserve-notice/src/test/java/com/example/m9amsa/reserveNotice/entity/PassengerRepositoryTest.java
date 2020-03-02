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
package com.example.m9amsa.reserveNotice.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_RESERVE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
public class PassengerRepositoryTest {

    @Autowired
    private PassengerRepository passengerRepository;

    @Before
    public void setUp() throws Exception {
        passengerRepository.deleteAll();
        passengerRepository.flush();
    }

    @After
    public void tearDown() throws Exception {
        passengerRepository.deleteAll();
        passengerRepository.flush();
    }

    @Test
    public void testPassengerRepository() {

        Passenger exp = Passenger.builder().name("渡辺太郎").age(31).isMainPassenger(true).email("0001@ntt.com").build();
        passengerRepository.saveAndFlush(exp);

        List<Passenger> result = passengerRepository.findAll();
        assertThat("取得されるレコード数は1件であること", result.size(), equalTo(1));
        Passenger resultOne = result.get(0);
        assertThat("Passenger.name", resultOne.getName(), equalTo(exp.getName()));
        assertThat("Passenger.age", resultOne.getAge(), equalTo(exp.getAge()));
        assertThat("Passenger.isMainPassenger", resultOne.isMainPassenger(), equalTo(exp.isMainPassenger()));
        assertThat("Passenger.email", resultOne.getEmail(), equalTo(exp.getEmail()));

    }
}
