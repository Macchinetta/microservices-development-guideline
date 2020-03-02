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
package com.example.m9amsa.reserve.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.reserve.constant.FlightType;
import com.example.m9amsa.reserve.constant.SeatClass;
import com.example.m9amsa.reserve.model.FareInfo;
import com.example.m9amsa.reserve.model.VacantSeatInfo;
import com.example.m9amsa.reserve.model.VacantSeatQueryCondition;
import com.example.m9amsa.reserve.service.TicketSearchService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "HOSTNAME_CALCULATE_FARE=localhost:28080",
        "HOSTNAME_FLIGHT=localhost:28081", "HOSTNAME_PURCHASE=localhost:28082", "DB_HOSTNAME_RESERVE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_ACCOUNT=localhost", "OAUTH2_CLIENT_ID=my-client", "OAUTH2_CLIENT_SECRET=1" })
public class TicketSearchControllerTest {

    @Mock
    TicketSearchService ticketSearchService;

    @InjectMocks
    TicketSearchController controller;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetVacantSeatInfo() throws Exception {

        // when
        List<FareInfo> fareList = new ArrayList<>();
        fareList.add(FareInfo.builder().fare(10520).fareType("基本料金").build());
        List<VacantSeatInfo> expList = new ArrayList<>();
        expList.add(VacantSeatInfo.builder().departureAirportId("HND").departureTime(LocalTime.now()).name("TEST001")
                .arrivalAirportId("KIX").arrivalTime(LocalTime.now()).seatClass(SeatClass.N).vacantSeats(30)
                .fareList(fareList).build());

        VacantSeatQueryCondition condition = VacantSeatQueryCondition.builder().flightType(FlightType.OW)
                .departureAirportId("HND").arrivalAirportId("KIX").departureDate(LocalDate.now()).seatClass(SeatClass.N)
                .build();
        VacantSeatQueryCondition conditionE = new VacantSeatQueryCondition();

        when(ticketSearchService.getVacantSeatInfo(condition)).thenReturn(expList);
        when(ticketSearchService.getVacantSeatInfo(conditionE)).thenThrow(new RuntimeException());

        // do
        List<VacantSeatInfo> actual = controller.getVacantSeatInfo(condition);
        assertThat("空席照会結果を取得できること", actual.size(), equalTo(1));
        assertThat("空席照会結果が正しいこと", actual.get(0).toString(), equalTo(expList.get(0).toString()));

        try {
            controller.getVacantSeatInfo(conditionE);
            fail("serviceでエラーが発生した場合、正常終了しないこと");
        } catch (Exception e) {
            assertThat("予期しない例外が発生した場合、そのままスローされること", e.getClass(), equalTo(RuntimeException.class));
        }
    }

}
