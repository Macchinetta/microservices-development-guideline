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
package com.example.m9amsa.purchaseNotice.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.purchaseNotice.constant.SeatClass;
import com.example.m9amsa.purchaseNotice.model.topic.MemberTopic;
import com.example.m9amsa.purchaseNotice.model.topic.PassengerTopic;
import com.example.m9amsa.purchaseNotice.model.topic.PurchaseTopic;
import com.example.m9amsa.purchaseNotice.service.PurchaseTopicService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_PURCHASE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
public class PurchaseInfoListenerTest {

    @Mock
    private PurchaseTopicService service;

    @InjectMocks
    private PurchaseTopicListener listener;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandlePurchaseTopic() {
        // when
        PassengerTopic passengerInfo = PassengerTopic.builder().name("渡辺太郎").age(31).isMainPassenger(true).build();
        List<PassengerTopic> passengers = new ArrayList<PassengerTopic>();
        passengers.add(passengerInfo);
        MemberTopic memberInfo = MemberTopic.builder().emailId("001@ntt.com").build();
        PurchaseTopic purchaseInfo = PurchaseTopic.builder().reserveId(1L).departureDate(LocalDate.of(2019, 5, 7))
                .flightId("NTT01").departureTime(LocalTime.of(10, 05)).arrivalTime(LocalTime.of(13, 05))
                .departureAirportId("HND").arrivalAirportId("PVD").seatClass(SeatClass.N).fareType("片道").fare(13500)
                .passengers(passengers).purchaseMember(Optional.of(memberInfo)).paymentId(1L)
                .cardNo("0000-0000-0000-0000").payDateTime(LocalDateTime.of(2019, 05, 01, 10, 5)).build();

        ArgumentCaptor<PurchaseTopic> capture = ArgumentCaptor.forClass(PurchaseTopic.class);
        doNothing().when(service).registerPurchaseNoticeInfo(capture.capture());

        // do
        listener.handlePurchaseTopic(purchaseInfo);

        // verify
        assertThat("引数がそのままserviceに渡っていることを確認", purchaseInfo.toString(), equalTo(capture.getValue().toString()));

    }
}
