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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
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

import com.example.m9amsa.flight.entity.BasicFare;
import com.example.m9amsa.flight.model.BasicFareInfo;
import com.example.m9amsa.flight.model.topic.BasicFareTopic;
import com.example.m9amsa.flight.model.topic.FlightTopicSource;
import com.example.m9amsa.flight.service.BasicFareService;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.util.function.Tuples;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class BasicFareControllerTest {

    @Autowired
    private BasicFareController basicFareController;

    @SpyBean
    private BasicFareService basicFareService;

    @Autowired
    private FlightTopicSource flightTopicSource;

    @Captor
    private ArgumentCaptor<BasicFareInfo> basicFareCaptor;

    @Captor
    private ArgumentCaptor<Optional<String>> stringCaptor;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private ObjectMapper jsonMapper;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(basicFareService);
    }

    /**
     * Test for addBasicFare.
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAddBasicFare() throws Exception {
        BasicFareInfo basicFare = BasicFareInfo.builder().departure("HND").arrival("OSA").fare(30000).build();

        when(basicFareService.addBasicFare(basicFare)).then(i -> {
            BasicFareInfo bfi = i.getArgument(0);
            BasicFare fare1 = bfi.asEntity();
            BasicFare fare2 = BasicFare.builder().departure(fare1.getArrival()).arrival(fare1.getDeparture())
                    .fare(fare1.getFare()).build();
            return Tuples.of(fare1, fare2);
        });

        basicFareController.addBasicFare(basicFare);

        verify(basicFareService).addBasicFare(basicFareCaptor.capture());

        BasicFareInfo expBasicFare = BasicFareInfo.builder().departure("HND").arrival("OSA").fare(30000).build();
        assertThat("basicFareServiceへ渡しているパラメータが正しいこと", basicFareCaptor.getValue(), equalTo(expBasicFare));

        Message<String> sendMessage = (Message<String>) messageCollector.forChannel(flightTopicSource.output()).poll();

        assertTrue("トピックヘッダに'x-payload-class'が設定されている事", sendMessage.getHeaders().containsKey("x-payload-class"));
        assertThat("トピックヘッダ'x-payload-class'の値が正しい事", sendMessage.getHeaders().get("x-payload-class"),
                equalTo(BasicFareTopic.class.getSimpleName()));
        BasicFareTopic basicFareTopic = BasicFareTopic.builder().departureAirportId(expBasicFare.getDeparture())
                .arrivalAirportId(expBasicFare.getArrival()).fare(expBasicFare.getFare()).build();
        JSONAssert.assertEquals("トピックのメッセージが正しい事", jsonMapper.writeValueAsString(basicFareTopic),
                sendMessage.getPayload(), false);

        sendMessage = (Message<String>) messageCollector.forChannel(flightTopicSource.output()).poll();

        assertTrue("トピックヘッダに'x-payload-class'が設定されている事", sendMessage.getHeaders().containsKey("x-payload-class"));
        assertThat("トピックヘッダ'x-payload-class'の値が正しい事", sendMessage.getHeaders().get("x-payload-class"),
                equalTo(BasicFareTopic.class.getSimpleName()));

//        BasicFare returnBasicFare = BasicFare.builder().departure("OSA").arrival("HND").fare(30000).build();
        BasicFareTopic basicFareTopic2 = BasicFareTopic.builder().departureAirportId(expBasicFare.getArrival())
                .arrivalAirportId(expBasicFare.getDeparture()).fare(expBasicFare.getFare()).build();
        JSONAssert.assertEquals("2件目のトピックのメッセージが正しい事", jsonMapper.writeValueAsString(basicFareTopic2),
                sendMessage.getPayload(), false);

    }

    /**
     * Test for findBasicFareList.
     */
    @SuppressWarnings("unchecked")
    public void testFindBasicFareList() {
        BasicFare basicFare1 = BasicFare.builder().departure("HND").arrival("OSA").fare(30000).build();
        BasicFare basicFare2 = BasicFare.builder().departure("HND").arrival("CTS").fare(25000).build();
        List<BasicFare> basicFares = Arrays.asList(basicFare1, basicFare2);

        when(basicFareService.findBasicFareList(any(Optional.class), any(Optional.class))).thenReturn(basicFares);

        clearInvocations(basicFareService);
        List<BasicFare> actualBasicFares = basicFareController.findBasicFareList(null, null);

        verify(basicFareService).findBasicFareList(stringCaptor.capture(), stringCaptor.capture());

        assertThat("basicFareService#findBasicFareListのパラメータが正しいこと", stringCaptor.getAllValues(),
                contains(Optional.empty(), Optional.empty()));

        assertThat("結果リストが正しいこと", actualBasicFares, contains(basicFare1, basicFare2));

        clearInvocations(basicFareService);
        actualBasicFares = basicFareController.findBasicFareList("HND", "OSA");

        verify(basicFareService).findBasicFareList(stringCaptor.capture(), stringCaptor.capture());

        assertThat("basicFareService#findBasicFareListのパラメータが正しいこと", stringCaptor.getAllValues(),
                contains(Optional.of("HND"), Optional.of("OSA")));

        assertThat("結果リストが正しいこと", actualBasicFares, contains(basicFare1, basicFare2));
    }
}
