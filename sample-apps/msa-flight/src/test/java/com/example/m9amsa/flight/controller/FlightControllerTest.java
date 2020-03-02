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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flight.constant.SeatClass;
import com.example.m9amsa.flight.entity.Airplane;
import com.example.m9amsa.flight.entity.Airport;
import com.example.m9amsa.flight.entity.BasicFare;
import com.example.m9amsa.flight.entity.Flight;
import com.example.m9amsa.flight.entity.VacantSeat;
import com.example.m9amsa.flight.model.FlightUpdateInfo;
import com.example.m9amsa.flight.model.ReserveVacantSeatInfo;
import com.example.m9amsa.flight.model.topic.FlightTopic;
import com.example.m9amsa.flight.model.topic.FlightTopicSource;
import com.example.m9amsa.flight.model.topic.FlightVacantSeatTopic;
import com.example.m9amsa.flight.service.CancelReservedSeatService;
import com.example.m9amsa.flight.service.FlightService;
import com.example.m9amsa.flight.service.ReserveVacantSeatService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class FlightControllerTest {

    @Autowired
    private FlightController flightController;

    @SpyBean
    private FlightService flightService;

    @SpyBean
    private ReserveVacantSeatService reserveVacantSeatService;

    @SpyBean
    private CancelReservedSeatService cancelReservedSeatService;

    @Autowired
    private FlightTopicSource flightTopicSource;

    @Captor
    private ArgumentCaptor<FlightUpdateInfo> flightUpdateModelCaptor;

    @Captor
    private ArgumentCaptor<Optional<String>> stringCaptor;

    @Captor
    private ArgumentCaptor<ReserveVacantSeatInfo> reserveVacantSeatCaptor;

    @Captor
    private ArgumentCaptor<Long> longCaptor;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private ObjectMapper jsonMapper;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(flightService);
    }

    /**
     * Test for addFlight.
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAddFlight() throws Exception {
        LocalDateTime departureTime = LocalDateTime.parse("1901-01-01T10:00:00");
        LocalDateTime arrivalTime = LocalDateTime.parse("1901-01-01T11:30:00");

        FlightUpdateInfo flightUpdateModel = FlightUpdateInfo.builder().name("MSA001").departureAirportId("HND")
                .departureTime(departureTime).arrivalAirportId("OSA").arrivalTime(arrivalTime).airplaneId(1L).build();

        Airplane airplane = Airplane.builder().id(1L).name("B777").standardSeats(200).specialSeats(50).build();

        Airport airportHnd = Airport.builder().id("HND").name("東京").build();

        Airport airportOsa = Airport.builder().id("OSA").name("大阪").build();

        BasicFare basicFare = BasicFare.builder().departure("HND").arrival("OSA").fare(30000).build();

        Flight flight = Flight.builder().name(flightUpdateModel.getName()).airplane(airplane)
                .departureAirport(airportHnd).departureTime(departureTime).arrivalAirport(airportOsa)
                .arrivalTime(arrivalTime).basicFare(basicFare).build();

        doReturn(flight).when(flightService).addFlight(flightUpdateModel);

        flightController.addFlight(flightUpdateModel);

        verify(flightService).addFlight(flightUpdateModelCaptor.capture());

        assertThat("flightServiceへ渡しているパラメータが正しいこと", flightUpdateModelCaptor.getValue(), equalTo(flightUpdateModel));

        Message<String> sendMessage = (Message<String>) messageCollector.forChannel(flightTopicSource.output()).poll();

        assertTrue("トピックヘッダに'x-payload-class'が設定されている事", sendMessage.getHeaders().containsKey("x-payload-class"));
        assertThat("トピックヘッダ'x-payload-class'の値が正しい事", sendMessage.getHeaders().get("x-payload-class"),
                equalTo(FlightTopic.class.getSimpleName()));
        FlightTopic flightTopic = new FlightTopic();
        BeanUtils.copyProperties(flightUpdateModel, flightTopic);
        LocalDateTime departureDateTime = flightUpdateModel.getDepartureTime();
        flightTopic.setDepartureTime(LocalTime.of(departureDateTime.getHour(), departureDateTime.getMinute(),
                departureDateTime.getSecond()));
        LocalDateTime arrivalDateTime = flightUpdateModel.getArrivalTime();
        flightTopic.setArrivalTime(
                LocalTime.of(arrivalDateTime.getHour(), arrivalDateTime.getMinute(), arrivalDateTime.getSecond()));
        JSONAssert.assertEquals("トピックのメッセージが正しい事", jsonMapper.writeValueAsString(flightTopic), sendMessage.getPayload(),
                false);

    }

    /**
     * Test for findFlightList.
     */
    @SuppressWarnings("unchecked")
    public void testFindFlightList() {
        LocalDateTime departureTime = LocalDateTime.parse("1901-01-01T10:00:00");
        LocalDateTime arrivalTime = LocalDateTime.parse("1901-01-01T11:30:00");

        Airplane airplane = Airplane.builder().id(1L).name("B777").standardSeats(200).specialSeats(50).build();

        Airport airportHnd = Airport.builder().id("HND").name("東京").build();

        Airport airportOsa = Airport.builder().id("OSA").name("大阪").build();

        BasicFare basicFare = BasicFare.builder().departure("HND").arrival("OSA").fare(30000).build();

        Flight flight1 = Flight.builder().airplane(airplane).departureAirport(airportHnd).departureTime(departureTime)
                .arrivalAirport(airportOsa).arrivalTime(arrivalTime).basicFare(basicFare).build();

        Flight flight2 = Flight.builder().airplane(airplane).departureAirport(airportOsa).departureTime(arrivalTime)
                .arrivalAirport(airportHnd).arrivalTime(departureTime).basicFare(basicFare).build();

        List<Flight> flights = Arrays.asList(flight1, flight2);

        when(flightService.findFlightList(any(Optional.class), any(Optional.class))).thenReturn(flights);

        clearInvocations(flightService);
        List<Flight> actualFlights = flightController.findFlightList(null, null);

        verify(flightService).findFlightList(stringCaptor.capture(), stringCaptor.capture());

        assertThat("flightService#findFlightListのパラメータが正しいこと", stringCaptor.getAllValues(),
                contains(Optional.empty(), Optional.empty()));

        assertThat("結果リストが正しいこと", actualFlights, contains(flight1, flight2));

        clearInvocations(flightService);
        actualFlights = flightController.findFlightList("HND", "OSA");

        verify(flightService).findFlightList(stringCaptor.capture(), stringCaptor.capture());

        assertThat("flightService#findFlightListのパラメータが正しいこと", stringCaptor.getAllValues(),
                contains(Optional.of("HND"), Optional.of("OSA")));

        assertThat("結果リストが正しいこと", actualFlights, contains(flight1, flight2));
    }

    /**
     * Test for reserveVacantSeat.
     * 
     * @throws Exception
     */
    @Test
    public void testReserveVacantSeat() throws Exception {

        Long reserveId = 1L;
        LocalDate departureDate = LocalDate.of(2019, 12, 1);
        String flightName = "B777";
        VacantSeat vacantSeat = createVacantSeat(flightName);
        ReserveVacantSeatInfo reserveVacantSeat = createReservVacantSeat(reserveId, departureDate, flightName);

        doReturn(vacantSeat).when(reserveVacantSeatService).reserveVacantSeat(reserveVacantSeat);

        flightController.reserveVacantSeat(reserveVacantSeat);

        verify(reserveVacantSeatService, times(1)).reserveVacantSeat(reserveVacantSeatCaptor.capture());

        assertThat("フライト空席確保サービスへ渡しているパラメータが正しいこと", reserveVacantSeatCaptor.getValue(), equalTo(reserveVacantSeat));

        @SuppressWarnings("unchecked")
        Message<String> sendMessage = (Message<String>) messageCollector.forChannel(flightTopicSource.output()).poll();

        assertTrue("トピックヘッダに'x-payload-class'が設定されていること", sendMessage.getHeaders().containsKey("x-payload-class"));
        assertThat("トピックヘッダ'x-payload-class'の値が正しいこと", sendMessage.getHeaders().get("x-payload-class"),
                equalTo(FlightVacantSeatTopic.class.getSimpleName()));
        FlightVacantSeatTopic flightVacantSeatTopic = new FlightVacantSeatTopic();
        BeanUtils.copyProperties(vacantSeat, flightVacantSeatTopic);
        JSONAssert.assertEquals("トピックのメッセージが正しいこと", jsonMapper.writeValueAsString(flightVacantSeatTopic),
                sendMessage.getPayload(), false);
    }

    /**
     * Test for cancelReservedSeat.
     * 
     * @throws Exception
     */
    @Test
    public void testCancelReservedSeat() throws Exception {

        Long reserveId = 1L;
        LocalDate departureDate = LocalDate.of(2019, 12, 1);
        String flightName = "B777";
        VacantSeat vacantSeat = createVacantSeat(flightName);
        ReserveVacantSeatInfo reserveVacantSeat = createReservVacantSeat(reserveId, departureDate, flightName);

        doReturn(vacantSeat).when(cancelReservedSeatService).cancelReservedSeat(reserveVacantSeat);

        flightController.cancelReservedSeat(reserveVacantSeat);

        verify(cancelReservedSeatService, times(1)).cancelReservedSeat(reserveVacantSeatCaptor.capture());

        assertThat("フライト空席確保取り消しサービスへ渡しているパラメータが正しいこと", reserveVacantSeatCaptor.getValue(), equalTo(reserveVacantSeat));

        @SuppressWarnings("unchecked")
        Message<String> sendMessage = (Message<String>) messageCollector.forChannel(flightTopicSource.output()).poll();

        assertTrue("トピックヘッダに'x-payload-class'が設定されていること", sendMessage.getHeaders().containsKey("x-payload-class"));
        assertThat("トピックヘッダ'x-payload-class'の値が正しいこと", sendMessage.getHeaders().get("x-payload-class"),
                equalTo(FlightVacantSeatTopic.class.getSimpleName()));
        FlightVacantSeatTopic flightVacantSeatTopic = new FlightVacantSeatTopic();
        BeanUtils.copyProperties(vacantSeat, flightVacantSeatTopic);
        JSONAssert.assertEquals("トピックのメッセージが正しいこと", jsonMapper.writeValueAsString(flightVacantSeatTopic),
                sendMessage.getPayload(), false);
    }

    /**
     * 空席情報を作成します。
     * 
     */
    private VacantSeat createVacantSeat(String flightName) {
        return VacantSeat.builder() //
                .departureDate(LocalDate.of(2019, 12, 1))//
                .flightName(flightName)//
                .vacantStandardSeatCount(20)//
                .vacantSpecialSeatCount(20)//
                .build();
    }

    /**
     * 空席確保情報を作成します。
     * 
     */
    private ReserveVacantSeatInfo createReservVacantSeat(Long reserveId, LocalDate departureDate, String flightName) {
        return ReserveVacantSeatInfo.builder()//
                .reserveId(reserveId)//
                .departureDate(departureDate)//
                .flightName(flightName)//
                .seatClass(SeatClass.N)//
                .vacantSeatCount(1)//
                .build();
    }

}
