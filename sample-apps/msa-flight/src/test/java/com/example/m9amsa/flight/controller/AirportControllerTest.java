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
package com.example.m9amsa.flight.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flight.entity.Airport;
import com.example.m9amsa.flight.model.AirportInfo;
import com.example.m9amsa.flight.model.topic.AirportTopic;
import com.example.m9amsa.flight.model.topic.FlightTopicSource;
import com.example.m9amsa.flight.service.AirportService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class AirportControllerTest {

    @Autowired
    private AirportController airportController;

    @SpyBean
    private AirportService airportService;

    @Autowired
    private FlightTopicSource flightTopicSource;

    @Captor
    private ArgumentCaptor<AirportInfo> airportCaptor;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private ObjectMapper jsonMapper;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(airportService);
    }

    /**
     * Test for addAirport.
     * 
     * @throws Exception
     */
    @Test
    public void testAddAirport() throws Exception {
        AirportInfo airport = AirportInfo.builder().id("HND").name("東京").build();

        airportController.addAirport(airport);

        verify(airportService).addAirport(airportCaptor.capture());

        AirportInfo expAirport = AirportInfo.builder().id("HND").name("東京").build();
        assertThat("airportServiceへ渡しているパラメータが正しいこと", airportCaptor.getValue(), equalTo(expAirport));

        @SuppressWarnings("unchecked")
        Message<String> sendMessage = (Message<String>) messageCollector.forChannel(flightTopicSource.output()).poll();

        assertTrue("トピックヘッダに'x-payload-class'が設定されている事", sendMessage.getHeaders().containsKey("x-payload-class"));
        assertThat("トピックヘッダ'x-payload-class'の値が正しい事", sendMessage.getHeaders().get("x-payload-class"),
                equalTo(AirportTopic.class.getSimpleName()));
        JSONAssert.assertEquals("トピックのメッセージが正しい事", jsonMapper.writeValueAsString(expAirport), sendMessage.getPayload(),
                false);

    }

    /**
     * Test for findAirportList.
     */
    public void testFindAirportList() {
        Airport airport1 = Airport.builder().id("HND").name("東京").build();
        Airport airport2 = Airport.builder().id("OSA").name("大阪").build();
        List<Airport> airports = Arrays.asList(airport1, airport2);

        when(airportService.findAirportList()).thenReturn(airports);

        List<Airport> actualAirports = airportController.findAirportList();

        assertThat("結果リストが正しいこと", actualAirports, contains(airport1, airport2));
    }

    /**
     * Test for findAirport.
     */
    public void testFindAirport() {
        String airportId = "HND";
        Optional<Airport> airport = Optional.of(Airport.builder().id(airportId).name("東京").build());

        when(airportService.findAirport(airportId)).thenReturn(airport);

        Airport actualAirport = airportController.findAirport(airportId);
        assertEquals("空港情報のidが正しいこと", actualAirport.getId(), airport.get().getId());
        assertEquals("空港情報の名称が正しいこと", actualAirport.getName(), airport.get().getName());
    }
}
