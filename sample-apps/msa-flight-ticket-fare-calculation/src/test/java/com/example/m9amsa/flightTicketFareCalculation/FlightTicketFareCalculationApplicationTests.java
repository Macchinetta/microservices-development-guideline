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
package com.example.m9amsa.flightTicketFareCalculation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.m9amsa.flightTicketFareCalculation.constant.FlightType;
import com.example.m9amsa.flightTicketFareCalculation.constant.SeatClass;
import com.example.m9amsa.flightTicketFareCalculation.model.FareCalcInfo;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.DiscountCalculator;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.GroupDiscount;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.LadiesDiscount;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.OneWayDiscount;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.Reserve1Discount;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.Reserve30Discount;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.Reserve7Discount;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.RoundTripDiscount;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.SpecialOneWayDiscount;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.SpecialReserve1Discount;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.SpecialRoundTripDiscount;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_CALCULATE_FARE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=localhost" })
@Slf4j
public class FlightTicketFareCalculationApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Value("${info.url.root-path}")
    private String urlRoot;

    @Autowired
    private ObjectMapper jsonMapper;

    @SpyBean
    private GroupDiscount groupDiscount;

    @SpyBean
    private LadiesDiscount ladiesDiscount;

    @SpyBean
    private OneWayDiscount oneWayDiscount;

    @SpyBean
    private Reserve1Discount reserve1Discount;

    @SpyBean
    private Reserve30Discount reserve30Discount;

    @SpyBean
    private Reserve7Discount reserve7Discount;

    @SpyBean
    private RoundTripDiscount roundTripDiscount;

    @SpyBean
    private SpecialOneWayDiscount specialOneWayDiscount;

    @SpyBean
    private SpecialReserve1Discount specialReserve1Discount;

    @SpyBean
    private SpecialRoundTripDiscount specialRoundTripDiscount;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    private void setClock(Clock clock) {
        doReturn(clock).when(groupDiscount).getClock();
        doReturn(clock).when(ladiesDiscount).getClock();
        doReturn(clock).when(oneWayDiscount).getClock();
        doReturn(clock).when(reserve1Discount).getClock();
        doReturn(clock).when(reserve30Discount).getClock();
        doReturn(clock).when(reserve7Discount).getClock();
        doReturn(clock).when(roundTripDiscount).getClock();
        doReturn(clock).when(specialOneWayDiscount).getClock();
        doReturn(clock).when(specialReserve1Discount).getClock();
        doReturn(clock).when(specialRoundTripDiscount).getClock();

    }

    /**
     * Test for POST /flight-ticket-fare
     */
    @Test
    public void testCalcFareCorrect() throws Exception {
        Clock baseClock = Clock.fixed(Instant.parse("2019-01-01T00:00:00Z"), ZoneId.systemDefault());

        setClock(baseClock);

        // グループ割なし
        // 片道運賃

        FareCalcInfo input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now(baseClock).plusDays(0)).flightType(FlightType.OW).seatClass(SeatClass.N)
                .totalPassengers(2).basicFare(30000).build();

        String jsonInput = jsonMapper.writeValueAsString(input);

        ResultActions result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight-ticket-fare") //
                        .content(jsonInput) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        log.info("response = {}", result.andReturn().getResponse().getContentAsString());

        List<TestFare> expFareList = new ArrayList<>();
        expFareList.add( //
                TestFare.builder().discountId("oneWayDiscount").name("片道運賃")
                        .description("一般席を利用する航空チケット予約システム利用者の通常運賃。").fare(42000).build() //
        );

        List<TestFare> actFareList = jsonMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                new TypeReference<List<TestFare>>() {
                });
        assertThat("運賃の配列数が同じこと", actFareList.size(), equalTo(expFareList.size()));
        assertThat("取得した運賃が正しいこと", actFareList.toArray(), arrayContainingInAnyOrder(expFareList.toArray()));

        // グループ割なし
        // 片道運賃、予約割１、レディース割

        input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now(baseClock).plusDays(1)).flightType(FlightType.OW).seatClass(SeatClass.N)
                .totalPassengers(2).basicFare(30000).build();

        jsonInput = jsonMapper.writeValueAsString(input);

        result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight-ticket-fare") //
                        .content(jsonInput) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        log.info("response = {}", result.andReturn().getResponse().getContentAsString());

        expFareList.add(//
                TestFare.builder().discountId("reserve1Discount").name("予約割１")
                        .description("一般席を利用する航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。").fare(37800).build() //
        );
        expFareList.add(//
                TestFare.builder().discountId("ladiesDiscount").name("レディース割")
                        .description("一般席を利用する女性の航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。").fare(29400).build() //
        );

        actFareList = jsonMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                new TypeReference<List<TestFare>>() {
                });
        assertThat("運賃の配列数が同じこと", actFareList.size(), equalTo(expFareList.size()));
        assertThat("取得した運賃が正しいこと", actFareList.toArray(), arrayContainingInAnyOrder(expFareList.toArray()));

        // グループ割なし
        // 片道運賃、予約割１、予約割７、レディース割

        input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now(baseClock).plusDays(7)).flightType(FlightType.OW).seatClass(SeatClass.N)
                .totalPassengers(2).basicFare(30000).build();

        jsonInput = jsonMapper.writeValueAsString(input);

        result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight-ticket-fare") //
                        .content(jsonInput) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        log.info("response = {}", result.andReturn().getResponse().getContentAsString());

        // 1月7日のフライトはピーク時期積算比率が1.0になるため、運賃基準が30000になります
        expFareList.clear();
        expFareList.add( //
                TestFare.builder().discountId("oneWayDiscount").name("片道運賃")
                        .description("一般席を利用する航空チケット予約システム利用者の通常運賃。").fare(30000).build() //
        );
        expFareList.add(//
                TestFare.builder().discountId("reserve1Discount").name("予約割１")
                        .description("一般席を利用する航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。").fare(27000).build() //
        );
        expFareList.add(//
                TestFare.builder().discountId("ladiesDiscount").name("レディース割")
                        .description("一般席を利用する女性の航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。").fare(21000).build() //
        );
        expFareList.add(//
                TestFare.builder().discountId("reserve7Discount").name("予約割７")
                        .description("一般席を利用する航空チケット予約システム利用者が、搭乗日の 7 日前までに予約する場合に利用できる運賃。").fare(24000).build() //
        );

        actFareList = jsonMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                new TypeReference<List<TestFare>>() {
                });
        assertThat("運賃の配列数が同じこと", actFareList.size(), equalTo(expFareList.size()));
        assertThat("取得した運賃が正しいこと", actFareList.toArray(), arrayContainingInAnyOrder(expFareList.toArray()));

        // グループ割なし
        // 片道運賃、予約割１、予約割７、早期割、レディース割

        input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now(baseClock).plusDays(30)).flightType(FlightType.OW).seatClass(SeatClass.N)
                .totalPassengers(2).basicFare(30000).build();

        jsonInput = jsonMapper.writeValueAsString(input);

        result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight-ticket-fare") //
                        .content(jsonInput) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        log.info("response = {}", result.andReturn().getResponse().getContentAsString());

        expFareList.add(//
                TestFare.builder().discountId("reserve30Discount").name("早期割")
                        .description("一般席を利用する航空チケット予約システム利用者が、搭乗日の 30 日前までに予約する場合に利用できる運賃。").fare(21000).build() //
        );

        actFareList = jsonMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                new TypeReference<List<TestFare>>() {
                });
        assertThat("運賃の配列数が同じこと", actFareList.size(), equalTo(expFareList.size()));
        assertThat("取得した運賃が正しいこと", actFareList.toArray(), arrayContainingInAnyOrder(expFareList.toArray()));

        // グループ割あり
        // 片道運賃、予約割１、予約割７、早期割、レディース割、グループ割

        input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now(baseClock).plusDays(30)).flightType(FlightType.OW).seatClass(SeatClass.N)
                .totalPassengers(3).basicFare(30000).build();

        jsonInput = jsonMapper.writeValueAsString(input);

        result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight-ticket-fare") //
                        .content(jsonInput) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        log.info("response = {}", result.andReturn().getResponse().getContentAsString());

        expFareList.add(//
                TestFare.builder().discountId("groupDiscount").name("グループ割")
                        .description("一般席を利用する航空チケット予約システム利用者が、3 名以上で搭乗日の前日までに予約する場合に利用できる運賃。").fare(21000).build() //
        );

        actFareList = jsonMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                new TypeReference<List<TestFare>>() {
                });
        assertThat("運賃の配列数が同じこと", actFareList.size(), equalTo(expFareList.size()));
        assertThat("取得した運賃が正しいこと", actFareList.toArray(), arrayContainingInAnyOrder(expFareList.toArray()));

        // 往復割

        input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now(baseClock).plusDays(50)).flightType(FlightType.RT).seatClass(SeatClass.N)
                .totalPassengers(3).basicFare(30000).build();

        jsonInput = jsonMapper.writeValueAsString(input);

        result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight-ticket-fare") //
                        .content(jsonInput) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        log.info("response = {}", result.andReturn().getResponse().getContentAsString());

        expFareList.clear();
        expFareList.add(//
                TestFare.builder().discountId("roundTripDiscount").name("往復運賃")
                        .description("同一路線を往復する場合の運賃。(一般席、特別席の混合不可)").fare(28500).build() //
        );

        actFareList = jsonMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                new TypeReference<List<TestFare>>() {
                });
        assertThat("運賃の配列数が同じこと", actFareList.size(), equalTo(expFareList.size()));
        assertThat("取得した運賃が正しいこと", actFareList.toArray(), arrayContainingInAnyOrder(expFareList.toArray()));

        // 特別席
        // 片道運賃

        input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now(baseClock).plusDays(0)).flightType(FlightType.OW).seatClass(SeatClass.S)
                .totalPassengers(1).basicFare(30000).build();

        jsonInput = jsonMapper.writeValueAsString(input);

        result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight-ticket-fare") //
                        .content(jsonInput) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        log.info("response = {}", result.andReturn().getResponse().getContentAsString());

        expFareList.clear();
        expFareList.add(//
                TestFare.builder().discountId("specialOneWayDiscount").name("特別片道運賃")
                        .description("特別席を利用する航空チケット予約システム利用者の通常運賃。").fare(49000).build() //
        );

        actFareList = jsonMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                new TypeReference<List<TestFare>>() {
                });
        assertThat("運賃の配列数が同じこと", actFareList.size(), equalTo(expFareList.size()));
        assertThat("取得した運賃が正しいこと", actFareList.toArray(), arrayContainingInAnyOrder(expFareList.toArray()));

        // 特別席
        // 片道運賃、予約割

        input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now(baseClock).plusDays(1)).flightType(FlightType.OW).seatClass(SeatClass.S)
                .totalPassengers(1).basicFare(30000).build();

        jsonInput = jsonMapper.writeValueAsString(input);

        result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight-ticket-fare") //
                        .content(jsonInput) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        log.info("response = {}", result.andReturn().getResponse().getContentAsString());

        expFareList.add(//
                TestFare.builder().discountId("specialReserve1Discount").name("特別予約割")
                        .description("特別席を利用する航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。").fare(44100).build() //
        );

        actFareList = jsonMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                new TypeReference<List<TestFare>>() {
                });
        assertThat("運賃の配列数が同じこと", actFareList.size(), equalTo(expFareList.size()));
        assertThat("取得した運賃が正しいこと", actFareList.toArray(), arrayContainingInAnyOrder(expFareList.toArray()));

        // 特別席
        // 往復割

        input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now(baseClock).plusDays(0)).flightType(FlightType.RT).seatClass(SeatClass.S)
                .totalPassengers(3).basicFare(30000).build();

        jsonInput = jsonMapper.writeValueAsString(input);

        result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight-ticket-fare") //
                        .content(jsonInput) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        log.info("response = {}", result.andReturn().getResponse().getContentAsString());

        expFareList.clear();
        expFareList.add(//
                TestFare.builder().discountId("specialRoundTripDiscount").name("特別往復運賃")
                        .description("同一路線を往復する場合の運賃。(一般席、特別席の混合不可)").fare(46600).build() //
        );

        actFareList = jsonMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                new TypeReference<List<TestFare>>() {
                });
        assertThat("運賃の配列数が同じこと", actFareList.size(), equalTo(expFareList.size()));
        assertThat("取得した運賃が正しいこと", actFareList.toArray(), arrayContainingInAnyOrder(expFareList.toArray()));

        // 割引コード指定
        // 早期割

        input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now(baseClock).plusDays(30)).flightType(FlightType.OW).seatClass(SeatClass.N)
                .totalPassengers(3).basicFare(30000) //
                .discountId(DiscountCalculator.DiscountType.RESERVE30.getDiscountId()) // 早期割
                .build();

        jsonInput = jsonMapper.writeValueAsString(input);

        result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight-ticket-fare") //
                        .content(jsonInput) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        log.info("response = {}", result.andReturn().getResponse().getContentAsString());

        expFareList.clear();
        expFareList.add(//
                TestFare.builder().discountId("reserve30Discount").name("早期割")
                        .description("一般席を利用する航空チケット予約システム利用者が、搭乗日の 30 日前までに予約する場合に利用できる運賃。").fare(21000).build() //
        );

        actFareList = jsonMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                new TypeReference<List<TestFare>>() {
                });
        assertThat("運賃の配列数が同じこと", actFareList.size(), equalTo(expFareList.size()));
        assertThat("取得した運賃が正しいこと", actFareList.toArray(), arrayContainingInAnyOrder(expFareList.toArray()));

        // 該当割引なし（搭乗日の91前の予約）

        input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .travelDate(LocalDate.now(baseClock).plusDays(91)).flightType(FlightType.OW).seatClass(SeatClass.N)
                .totalPassengers(2).basicFare(30000).build();

        jsonInput = jsonMapper.writeValueAsString(input);

        result = mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight-ticket-fare") //
                        .content(jsonInput) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isNotFound()) //
        ;

    }

}
