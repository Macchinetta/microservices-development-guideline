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
package com.example.m9amsa.purchaseNotice.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.purchaseNotice.constant.SeatClass;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_PURCHASE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
public class PurchaseNoticeInfoRepositoryTest {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Before
    public void setUp() throws Exception {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();
    }

    @After
    public void tearDown() throws Exception {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();
    }

    @Test
    public void testPurchaseNoticeInfoRepository() {

        Passenger passengerInfo = Passenger.builder().name("渡辺太郎").age(31).isMainPassenger(true).build();
        // System.out.println(passengerInfo);
        List<Passenger> passengers = new ArrayList<Passenger>();
        passengers.add(passengerInfo);
        Purchase purchase = Purchase.builder().reserveId(1L).departureDate(LocalDate.of(2019, 5, 7)).flightId("NTT01")
                .departureTime(LocalTime.of(10, 05)).arrivalTime(LocalTime.of(13, 05)).departureAirportId("HND")
                .arrivalAirportId("PVD").seatClass(SeatClass.N).fareType("片道").fare(13500).passengers(passengers)
                .emailId("0001@ntt.com").paymentId(1L).cardNo("0000-0000-0000-0000")
                .payDateTime(LocalDateTime.of(2019, 05, 01, 10, 5)).build();
        // System.out.println(purchase);
        purchaseRepository.saveAndFlush(purchase);

        List<Purchase> result = purchaseRepository.findAll();
        assertThat("取得されるレコード数は1件であること", result.size(), equalTo(1));

        Purchase resultOne = result.get(0);
        assertThat("Purchase.reserveId", resultOne.getReserveId(), equalTo(purchase.getReserveId()));
        assertThat("Purchase.departureDate", resultOne.getDepartureDate(), equalTo(purchase.getDepartureDate()));
        assertThat("Purchase.flightId", resultOne.getFlightId(), equalTo(purchase.getFlightId()));
        assertThat("Purchase.departureTime", resultOne.getDepartureTime(), equalTo(purchase.getDepartureTime()));
        assertThat("Purchase.arrivalTime", resultOne.getArrivalTime(), equalTo(purchase.getArrivalTime()));
        assertThat("Purchase.departureAirportId", resultOne.getDepartureAirportId(),
                equalTo(purchase.getDepartureAirportId()));
        assertThat("Purchase.arrivalAirportId", resultOne.getArrivalAirportId(),
                equalTo(purchase.getArrivalAirportId()));
        assertThat("Purchase.seatClass", resultOne.getSeatClass(), equalTo(purchase.getSeatClass()));
        assertThat("Purchase.fareType", resultOne.getFareType(), equalTo(purchase.getFareType()));
        assertThat("Purchase.fare", resultOne.getFare(), equalTo(purchase.getFare()));
        assertThat("Purchase.emailId", resultOne.getEmailId(), equalTo(purchase.getEmailId()));
        assertThat("Purchase.paymentId", resultOne.getPaymentId(), equalTo(purchase.getPaymentId()));
        assertThat("Purchase.cardNo", resultOne.getCardNo(), equalTo(purchase.getCardNo()));
        assertThat("Purchase.payDateTime", resultOne.getPayDateTime(), equalTo(purchase.getPayDateTime()));

    }
}
