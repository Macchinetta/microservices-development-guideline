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
package com.example.m9amsa.flight.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flight.entity.Airplane;
import com.example.m9amsa.flight.entity.AirplaneRepository;
import com.example.m9amsa.flight.model.AirplaneInfo;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class AirplaneServiceTest {

    @Autowired
    private AirplaneService airplaneService;

    @MockBean
    private AirplaneRepository airplaneRepository;

    @Captor
    private ArgumentCaptor<Airplane> airplaneCaptor;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(airplaneRepository);
    }

    /**
     * Test for addAirplane.
     */
    @Test
    public void testAddAirplane() {
        AirplaneInfo airplaneInfo = AirplaneInfo.builder().name("B777").standardSeats(200).specialSeats(50).build();

        Airplane airplane = airplaneInfo.asEntity();
        airplane.setId(1L);
        when(airplaneRepository.save(airplaneInfo.asEntity())).then(i -> {
            return airplane;
        });

        Airplane actualAirplane = airplaneService.addAirplane(airplaneInfo);

        verify(airplaneRepository).save(airplaneCaptor.capture());

        assertThat("airplaneRepositoryへのパラメータが正しいこと", airplaneCaptor.getValue(), equalTo(airplaneInfo.asEntity()));

        assertThat("Airplane.id", actualAirplane.getId(), equalTo(1L));
        assertThat("Airplane.name", actualAirplane.getName(), equalTo("B777"));
        assertThat("Airplane.standardSeats", actualAirplane.getStandardSeats(), equalTo(200));
        assertThat("Airplane.specialSeats", actualAirplane.getSpecialSeats(), equalTo(50));
    }

    /**
     * Test for findAirplaneList.
     */
    @Test
    public void testFindAirplaneList() {
        Airplane airplane1 = Airplane.builder().id(1L).name("B777").standardSeats(200).specialSeats(50).build();
        Airplane airplane2 = Airplane.builder().id(1L).name("B888").standardSeats(300).specialSeats(150).build();
        when(airplaneRepository.findAll()).thenReturn(Arrays.asList(airplane1, airplane2));

        List<Airplane> actualList = airplaneService.findAirplaneList();

        assertThat("リストサイズは2であること", actualList.size(), equalTo(2));
        assertThat("リストの内容、順序が正しいこと", actualList, contains(airplane1, airplane2));
    }
}
