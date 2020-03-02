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
package com.example.m9amsa.flightTicketFareCalculation.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flightTicketFareCalculation.constant.FlightType;
import com.example.m9amsa.flightTicketFareCalculation.constant.SeatClass;
import com.example.m9amsa.flightTicketFareCalculation.model.FareCalcInfo;
import com.example.m9amsa.flightTicketFareCalculation.model.FlightFareInfo;
import com.example.m9amsa.flightTicketFareCalculation.service.FlightTicketFareCalculationService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_CALCULATE_FARE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=localhost" })
public class FlightTicketFareCalculationControllerTest {

    @MockBean
    private FlightTicketFareCalculationService flightTicketFareCalculationService;

    @InjectMocks
    private FlightTicketFareCalculationController flightTicketFareCalculationController;

    @Captor
    private ArgumentCaptor<FareCalcInfo> fareCalcInputCaptor;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCalcFare() throws Exception {
        List<FlightFareInfo> expResponse = Arrays.asList( //
                FlightFareInfo.builder().discountId("oneWayDiscount").name("片道運賃")
                        .description("一般席を利用する航空チケット予約システム利用者の通常運賃。").fare(42000).build(), //
                FlightFareInfo.builder().discountId("reserve1Discount").name("予約割１")
                        .description("一般席を利用する航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。").fare(37800).build(), //
                FlightFareInfo.builder().discountId("ladiesDiscount").name("レディース割")
                        .description("一般席を利用する女性の航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。").fare(29400).build() //
        );

        doReturn(expResponse).when(flightTicketFareCalculationService).calcFare(any(FareCalcInfo.class));

        FareCalcInfo input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now()).flightType(FlightType.OW).seatClass(SeatClass.N).totalPassengers(2)
                .basicFare(30000).build();

        List<FlightFareInfo> actResponse = flightTicketFareCalculationController.calcFare(input);

        assertThat("戻り値が正しいこと：インスタンス", actResponse, equalTo(expResponse));
        assertThat("戻り値が正しいこと：サイズ", actResponse.size(), equalTo(expResponse.size()));
        assertThat("戻り値が正しいこと：内容", actResponse.toArray(), arrayContainingInAnyOrder(expResponse.toArray()));

        verify(flightTicketFareCalculationService, times(1)).calcFare(fareCalcInputCaptor.capture());

        assertThat("サービスのメソッドが正しく呼ばれていること", fareCalcInputCaptor.getValue(), equalTo(input));
    }
}
