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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

import com.example.m9amsa.flight.entity.Airport;
import com.example.m9amsa.flight.entity.AirportRepository;
import com.example.m9amsa.flight.model.AirportInfo;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class AirportServiceTest {

    @Autowired
    private AirportService airportService;

    @MockBean
    private AirportRepository airportRepository;

    @Captor
    private ArgumentCaptor<Airport> airportCaptor;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(airportRepository);
    }

    /**
     * Test for addAirport.
     */
    @Test
    public void testAddAirport() {
        AirportInfo airport = AirportInfo.builder().name("HND").name("東京").build();

        when(airportRepository.save(airport.asEntity())).thenReturn(airport.asEntity());

        airportService.addAirport(airport);

        verify(airportRepository).save(airportCaptor.capture());

        assertThat("airportRepositoryへのパラメータが正しいこと", airportCaptor.getValue(), equalTo(airport.asEntity()));

    }

    /**
     * Test for findAirportList.
     */
    @Test
    public void testFindAirportList() {
        Airport airport1 = Airport.builder().name("HND").name("東京").build();
        Airport airport2 = Airport.builder().name("OSA").name("大阪").build();
        when(airportRepository.findAll()).thenReturn(Arrays.asList(airport1, airport2));

        List<Airport> actualList = airportService.findAirportList();

        assertThat("リストサイズは2であること", actualList.size(), equalTo(2));
        assertThat("リストの内容、順序が正しいこと", actualList, contains(airport1, airport2));
    }

    /**
     * Test for testFindAirport.
     */
    @Test
    public void testFindAirport() {
        String airportId = "HND";
        Airport airport = Airport.builder().name(airportId).name("東京").build();
        when(airportRepository.findById(airportId)).thenReturn(Optional.of(airport));

        Optional<Airport> actualAirport = airportService.findAirport(airportId);
        assertEquals("空港情報のidが正しいこと", actualAirport.get().getId(), airport.getId());
        assertEquals("空港情報の名称が正しいこと", actualAirport.get().getName(), airport.getName());
    }
}
