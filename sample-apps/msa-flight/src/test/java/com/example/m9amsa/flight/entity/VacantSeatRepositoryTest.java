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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
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
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class VacantSeatRepositoryTest {

    @Autowired
    private VacantSeatRepository vacantSeatRepository;

    @Before
    public void setUp() throws Exception {
        vacantSeatRepository.deleteAll();
        vacantSeatRepository.flush();
    }

    @After
    public void tearDown() throws Exception {
        vacantSeatRepository.deleteAll();
        vacantSeatRepository.flush();
    }

    /**
     * VacantSeatRepositoryをテスト。
     * 
     * <pre>
     * 正常系テスト。
     * -save
     * -findAll
     * </pre>
     */
    @Test
    public void testVacantSeatRepositoryCorrect() {
        VacantSeat vacantSeat = VacantSeat.builder().departureDate(LocalDate.of(2019, 9, 12)).flightName("NTT001")
                .vacantStandardSeatCount(20).vacantSpecialSeatCount(5).build();

        vacantSeatRepository.saveAndFlush(vacantSeat);

        List<VacantSeat> actualList = vacantSeatRepository.findAll();

        assertThat("取得されるレコード数は1件であること", actualList.size(), equalTo(1));
        assertThat("登録した値と取得した値の一致確認", actualList.get(0), equalTo(vacantSeat));
    }

}
