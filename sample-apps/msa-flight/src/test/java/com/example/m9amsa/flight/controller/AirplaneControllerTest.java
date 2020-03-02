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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flight.entity.Airplane;
import com.example.m9amsa.flight.entity.AirplaneRepository;
import com.example.m9amsa.flight.entity.FlightRepository;
import com.example.m9amsa.flight.model.AirplaneInfo;
import com.example.m9amsa.flight.model.topic.AirplaneTopic;
import com.example.m9amsa.flight.model.topic.FlightTopicSource;
import com.example.m9amsa.flight.service.AirplaneService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class AirplaneControllerTest {

    @Autowired
    private AirplaneController airplaneController;

    @Autowired
    private AirplaneRepository airplaneRepository;

    @Autowired
    private FlightRepository flightRepository;

    @SpyBean
    private AirplaneService airplaneService;

    @Autowired
    private FlightTopicSource flightTopicSource;

    @Captor
    private ArgumentCaptor<AirplaneInfo> airplaneCaptor;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private ObjectMapper jsonMapper;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(airplaneService);
        flightRepository.deleteAll();
        flightRepository.flush();
        airplaneRepository.deleteAll();
        airplaneRepository.flush();

    }

    /**
     * Test for addAirplane.
     * 
     * @throws Exception
     */
    @Test
    public void testAddAirplane() throws Exception {
        AirplaneInfo airplane = AirplaneInfo.builder().name("B777").standardSeats(300).specialSeats(100).build();

        when(airplaneService.addAirplane(airplane)).then(i -> {
            AirplaneInfo ai = i.getArgument(0);
            Airplane a = ai.asEntity();
            a.setId(1L);
            return a;
        });

        airplaneController.addAirplane(airplane);

        verify(airplaneService).addAirplane(airplaneCaptor.capture());

        AirplaneInfo expAirplaneInfo = AirplaneInfo.builder().name("B777").standardSeats(300).specialSeats(100).build();
        assertThat("airplaneServiceへ渡しているパラメータが正しいこと", airplaneCaptor.getValue(), equalTo(expAirplaneInfo));

        @SuppressWarnings("unchecked")
        Message<String> sendMessage = (Message<String>) messageCollector.forChannel(flightTopicSource.output()).poll();

        assertTrue("トピックヘッダに'x-payload-class'が設定されている事", sendMessage.getHeaders().containsKey("x-payload-class"));
        assertThat("トピックヘッダ'x-payload-class'の値が正しい事", sendMessage.getHeaders().get("x-payload-class"),
                equalTo(AirplaneTopic.class.getSimpleName()));
        AirplaneTopic expAirplane = AirplaneTopic.builder().id(1L).name("B777").standardSeats(300).specialSeats(100)
                .build();
        JSONAssert.assertEquals("トピックのメッセージが正しい事", jsonMapper.writeValueAsString(expAirplane), sendMessage.getPayload(),
                false);

    }

    /**
     * Test for findAirplaneList.
     */
    public void testFindAirplaneList() {
        Airplane airplane1 = Airplane.builder().id(1L).name("B777").standardSeats(100).specialSeats(10).build();
        Airplane airplane2 = Airplane.builder().id(2L).name("B888").standardSeats(200).specialSeats(20).build();
        List<Airplane> airplanes = Arrays.asList(airplane1, airplane2);

        when(airplaneService.findAirplaneList()).thenReturn(airplanes);

        List<Airplane> actualAirplanes = airplaneController.findAirplaneList();

        assertThat("結果リストが正しいこと", actualAirplanes, contains(airplane1, airplane2));
    }
}
