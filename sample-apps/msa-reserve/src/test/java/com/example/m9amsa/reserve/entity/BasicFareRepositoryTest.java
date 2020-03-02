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
public class BasicFareRepositoryTest {

    @Autowired
    BasicFareRepository basicFareRepository;

    @Before
    public void setUp() {
        basicFareRepository.deleteAll();
        basicFareRepository.flush();
    }

    @After
    public void tearDown() {
        basicFareRepository.deleteAll();
        basicFareRepository.flush();
    }

    /**
     * BasiFareRepositoryをテスト。
     * 
     * <pre>
     * -save - findAll
     * </pre>
     */
    @Test
    public void testBasicFareRepository() {

        BasicFare exp = BasicFare.builder().arrivalAirportId("HND").departureAirportId("KIX").fare(10520).build();
        basicFareRepository.save(exp);

        List<BasicFare> result = basicFareRepository.findAll();
        assertThat("取得されるレコード数は1件であること", result.size(), equalTo(1));

        BasicFare resultOne = result.get(0);
        assertThat("BasicFare.id.arrival", resultOne.getArrivalAirportId(), equalTo(exp.getArrivalAirportId()));
        assertThat("BasicFare.id.departure", resultOne.getDepartureAirportId(), equalTo(exp.getDepartureAirportId()));
        assertThat("BasicFare.fare", resultOne.getFare(), equalTo(exp.getFare()));
    }

}
