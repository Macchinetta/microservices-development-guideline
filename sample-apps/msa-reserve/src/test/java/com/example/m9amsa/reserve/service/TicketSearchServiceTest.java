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
package com.example.m9amsa.reserve.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;

import com.example.m9amsa.reserve.constant.FlightType;
import com.example.m9amsa.reserve.constant.SeatClass;
import com.example.m9amsa.reserve.entity.Flight;
import com.example.m9amsa.reserve.entity.FlightRepository;
import com.example.m9amsa.reserve.entity.FlightVacantSeat;
import com.example.m9amsa.reserve.entity.FlightVacantSeatId;
import com.example.m9amsa.reserve.entity.FlightVacantSeatRepository;
import com.example.m9amsa.reserve.entity.ReservationRepository;
import com.example.m9amsa.reserve.externalmicroservice.model.FareCalcInfoForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.FlightFareForEx;
import com.example.m9amsa.reserve.externalmicroservice.service.CalculateFareExternalMicroService;
import com.example.m9amsa.reserve.model.FareInfo;
import com.example.m9amsa.reserve.model.VacantSeatInfo;
import com.example.m9amsa.reserve.model.VacantSeatQueryCondition;

import feign.FeignException;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "HOSTNAME_CALCULATE_FARE=localhost:28080",
        "HOSTNAME_FLIGHT=localhost:28081", "HOSTNAME_PURCHASE=localhost:28082", "DB_HOSTNAME_RESERVE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_ACCOUNT=localhost", "OAUTH2_CLIENT_ID=my-client", "OAUTH2_CLIENT_SECRET=1" })
public class TicketSearchServiceTest {

    @Mock
    CalculateFareExternalMicroService calculateFareExternalMicroService;

    @Mock
    FlightRepository flightRepository;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    FlightVacantSeatRepository flightVacantSeatRepository;

    @InjectMocks
    TicketSearchService service;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * getVacantSeatInfo()
     * 
     * <pre>
     * - 登場クラス'N'
     * - 空席照会結果なし
     * - 運賃計算サービス結果なし(FeignException 404)
     * </pre>
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetVacantSeatInfo_seatTypeN() throws Exception {

        // when
        VacantSeatQueryCondition condition = VacantSeatQueryCondition.builder().flightType(FlightType.OW)
                .departureAirportId("HND").arrivalAirportId("KIX").departureDate(LocalDate.now()).seatClass(SeatClass.N)
                .build();

        List<Flight> flightInfoList = new ArrayList<>();
        Flight exp = Flight.builder().name("TEST001").departureAirportId(condition.getDepartureAirportId())
                .arrivalAirportId(condition.getArrivalAirportId()).departureTime(LocalTime.of(8, 30))
                .arrivalTime(LocalTime.of(10, 0)).airplaneName("Boeing 777").standardSeats(217).specialSeats(96)
                .fare(10520).build();
        flightInfoList.add(exp);

        ArgumentCaptor<Example<Flight>> flightInfoCapture = ArgumentCaptor.forClass(Example.class);
        ArgumentCaptor<Sort> sortCapture = ArgumentCaptor.forClass(Sort.class);
        when(flightRepository.findAll(flightInfoCapture.capture(), sortCapture.capture())).thenReturn(flightInfoList);

        List<FlightFareForEx> fareList = new ArrayList<>();
        FlightFareForEx fareExp = FlightFareForEx.builder().name("早期割").fare(9470).build();
        fareList.add(fareExp);
        ArgumentCaptor<FareCalcInfoForEx> calcCapture = ArgumentCaptor.forClass(FareCalcInfoForEx.class);
        when(calculateFareExternalMicroService.calcFare(calcCapture.capture())).thenReturn(fareList);

//        PassengerTopic passenger = PassengerTopic.builder().name("taro").age(20).dateOfBirth(LocalDate.of(1999, 7, 26)).reserveId("xxx001").build();
//        Reservation reservation = Reservation.builder().reserveId("xxx001").reserveTime(LocalDateTime.now())
//                .departureDate(LocalDate.now()).departureTime(LocalTime.now()).arrivalTime(LocalTime.now())
//                .flightId("TEST001").departureAireport("HND").arrivelAireport("KIX").seatClass(SeatClass.N)
//                .fareType("早期割").fare(10520).passenger(Arrays.asList(passenger)).build();
//        ArgumentCaptor<Example<Reservation>> exCapture = ArgumentCaptor.forClass(Example.class);
//        when(reservationRepository.findAll(exCapture.capture())).thenReturn(Arrays.asList(reservation));

        ArgumentCaptor<FlightVacantSeatId> fvsinfoCapture = ArgumentCaptor.forClass(FlightVacantSeatId.class);

        FlightVacantSeat flightVacantSeatInfo = FlightVacantSeat.builder().departureDate(LocalDate.now())
                .flightName("TEST001").standardSeats(20).specialSeats(10).build();
        when(flightVacantSeatRepository.findById(fvsinfoCapture.capture()))
                .thenReturn(Optional.of(flightVacantSeatInfo));

        // do
        List<VacantSeatInfo> actualList = service.getVacantSeatInfo(condition);

        // verify
        assertThat("空席情報が1件であること", actualList.size(), equalTo(1));
        VacantSeatInfo actual = actualList.get(0);
        assertThat("VacantSeatInfo.arrivalAirport", actual.getArrivalAirportId(), equalTo(exp.getArrivalAirportId()));
        assertThat("VacantSeatInfo.arrivalTime", actual.getArrivalTime(), equalTo(exp.getArrivalTime()));
        assertThat("VacantSeatInfo.departureAirport", actual.getDepartureAirportId(),
                equalTo(exp.getDepartureAirportId()));
        assertThat("VacantSeatInfo.departureTime", actual.getDepartureTime(), equalTo(exp.getDepartureTime()));
        assertThat("VacantSeatInfo.flightId", actual.getName(), equalTo(exp.getName()));
        assertThat("VacantSeatInfo.seatClass", actual.getSeatClass(), equalTo(condition.getSeatClass()));
        assertThat("VacantSeatInfo.vacantSeats", actual.getVacantSeats(),
                equalTo(flightVacantSeatInfo.getStandardSeats()));
        assertThat("VacantSeatInfo.fare.size()", actual.getFareList().size(), equalTo(1));
        FareInfo fareActual = actual.getFareList().get(0);
        assertThat("FareInfo.fare", fareActual.getFare(), equalTo(fareExp.getFare()));
        assertThat("FareInfo.fareType", fareActual.getFareType(), equalTo(fareExp.getName()));

        // フライト空席情報の検索結果＝0件
        when(flightVacantSeatRepository.findById(fvsinfoCapture.capture())).thenReturn(Optional.empty());

        // do
        actualList = service.getVacantSeatInfo(condition);

        // verify
        assertThat("空席情報が1件であること", actualList.size(), equalTo(1));
        actual = actualList.get(0);
        assertThat("VacantSeatInfo.arrivalAirport", actual.getArrivalAirportId(), equalTo(exp.getArrivalAirportId()));
        assertThat("VacantSeatInfo.arrivalTime", actual.getArrivalTime(), equalTo(exp.getArrivalTime()));
        assertThat("VacantSeatInfo.departureAirport", actual.getDepartureAirportId(),
                equalTo(exp.getDepartureAirportId()));
        assertThat("VacantSeatInfo.departureTime", actual.getDepartureTime(), equalTo(exp.getDepartureTime()));
        assertThat("VacantSeatInfo.flightId", actual.getName(), equalTo(exp.getName()));
        assertThat("VacantSeatInfo.seatClass", actual.getSeatClass(), equalTo(condition.getSeatClass()));
        assertThat("VacantSeatInfo.vacantSeats", actual.getVacantSeats(), equalTo(exp.getStandardSeats()));
        assertThat("VacantSeatInfo.fare.size()", actual.getFareList().size(), equalTo(1));
        fareActual = actual.getFareList().get(0);
        assertThat("FareInfo.fare", fareActual.getFare(), equalTo(fareExp.getFare()));
        assertThat("FareInfo.fareType", fareActual.getFareType(), equalTo(fareExp.getName()));

        // フライト情報の検索結果＝0件
        when(flightRepository.findAll(flightInfoCapture.capture(), sortCapture.capture()))
                .thenReturn(new ArrayList<>());
        actualList = service.getVacantSeatInfo(condition);
        assertThat("空席情報が0件であること", actualList.size(), equalTo(0));

        // 同期呼出しエラー
        when(flightRepository.findAll(flightInfoCapture.capture(), sortCapture.capture())).thenReturn(flightInfoList);
        Constructor<FeignException> feConstructor = ReflectionUtils.accessibleConstructor(FeignException.class,
                int.class, String.class);
        feConstructor.setAccessible(true);
        when(calculateFareExternalMicroService.calcFare(calcCapture.capture()))
                .thenThrow(feConstructor.newInstance(404, "Not Found"));
        try {
            service.getVacantSeatInfo(condition);
            fail("同期呼出しエラーの場合、正常終了しないこと");
        } catch (Exception e) {
            assertThat("同期呼出しエラーの場合、FeignExceptionをそのまま返すこと", e.getClass(), equalTo(FeignException.class));
        }
    }

    /**
     * getVacantSeatInfo(): 登場クラス'S'
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetVacantSeatInfo_seatTypeS() throws Exception {

        // when
        VacantSeatQueryCondition condition = VacantSeatQueryCondition.builder().flightType(FlightType.OW)
                .departureAirportId("HND").arrivalAirportId("KIX").departureDate(LocalDate.now()).seatClass(SeatClass.S)
                .build();

        List<Flight> flightInfoList = new ArrayList<>();
        Flight exp = Flight.builder().name("TEST001").departureAirportId(condition.getDepartureAirportId())
                .arrivalAirportId(condition.getArrivalAirportId()).departureTime(LocalTime.of(8, 30))
                .arrivalTime(LocalTime.of(10, 0)).airplaneName("Boeing 777").standardSeats(217).specialSeats(96)
                .fare(10520).build();
        flightInfoList.add(exp);

        ArgumentCaptor<Example<Flight>> flightInfoCapture = ArgumentCaptor.forClass(Example.class);
        ArgumentCaptor<Sort> sortCapture = ArgumentCaptor.forClass(Sort.class);
        when(flightRepository.findAll(flightInfoCapture.capture(), sortCapture.capture())).thenReturn(flightInfoList);

        List<FlightFareForEx> fareList = new ArrayList<>();
        FlightFareForEx fareExp = FlightFareForEx.builder().name("早期割").fare(9470).build();
        fareList.add(fareExp);
        ArgumentCaptor<FareCalcInfoForEx> calcCapture = ArgumentCaptor.forClass(FareCalcInfoForEx.class);
        when(calculateFareExternalMicroService.calcFare(calcCapture.capture())).thenReturn(fareList);

        ArgumentCaptor<FlightVacantSeatId> fvsinfoCapture = ArgumentCaptor.forClass(FlightVacantSeatId.class);

        FlightVacantSeat flightVacantSeatInfo = FlightVacantSeat.builder().departureDate(LocalDate.now())
                .flightName("TEST001").standardSeats(20).specialSeats(10).build();
        when(flightVacantSeatRepository.findById(fvsinfoCapture.capture()))
                .thenReturn(Optional.of(flightVacantSeatInfo));

        // do
        List<VacantSeatInfo> actualList = service.getVacantSeatInfo(condition);

        // verify
        assertThat("空席情報が1件であること", actualList.size(), equalTo(1));
        VacantSeatInfo actual = actualList.get(0);
        assertThat("VacantSeatInfo.arrivalAirport", actual.getArrivalAirportId(), equalTo(exp.getArrivalAirportId()));
        assertThat("VacantSeatInfo.arrivalTime", actual.getArrivalTime(), equalTo(exp.getArrivalTime()));
        assertThat("VacantSeatInfo.departureAirport", actual.getDepartureAirportId(),
                equalTo(exp.getDepartureAirportId()));
        assertThat("VacantSeatInfo.departureTime", actual.getDepartureTime(), equalTo(exp.getDepartureTime()));
        assertThat("VacantSeatInfo.flightId", actual.getName(), equalTo(exp.getName()));
        assertThat("VacantSeatInfo.seatClass", actual.getSeatClass(), equalTo(condition.getSeatClass()));
        assertThat("VacantSeatInfo.vacantSeats", actual.getVacantSeats(),
                equalTo(flightVacantSeatInfo.getSpecialSeats()));
        assertThat("VacantSeatInfo.fare.size()", actual.getFareList().size(), equalTo(1));
        FareInfo fareActual = actual.getFareList().get(0);
        assertThat("FareInfo.fare", fareActual.getFare(), equalTo(fareExp.getFare()));
        assertThat("FareInfo.fareType", fareActual.getFareType(), equalTo(fareExp.getName()));

        // フライト空席情報の検索結果＝0件
        when(flightVacantSeatRepository.findById(fvsinfoCapture.capture())).thenReturn(Optional.empty());

        // do
        actualList = service.getVacantSeatInfo(condition);

        // verify
        assertThat("空席情報が1件であること", actualList.size(), equalTo(1));
        actual = actualList.get(0);
        assertThat("VacantSeatInfo.arrivalAirport", actual.getArrivalAirportId(), equalTo(exp.getArrivalAirportId()));
        assertThat("VacantSeatInfo.arrivalTime", actual.getArrivalTime(), equalTo(exp.getArrivalTime()));
        assertThat("VacantSeatInfo.departureAirport", actual.getDepartureAirportId(),
                equalTo(exp.getDepartureAirportId()));
        assertThat("VacantSeatInfo.departureTime", actual.getDepartureTime(), equalTo(exp.getDepartureTime()));
        assertThat("VacantSeatInfo.flightId", actual.getName(), equalTo(exp.getName()));
        assertThat("VacantSeatInfo.seatClass", actual.getSeatClass(), equalTo(condition.getSeatClass()));
        assertThat("VacantSeatInfo.vacantSeats", actual.getVacantSeats(), equalTo(exp.getSpecialSeats()));
        assertThat("VacantSeatInfo.fare.size()", actual.getFareList().size(), equalTo(1));
        fareActual = actual.getFareList().get(0);
        assertThat("FareInfo.fare", fareActual.getFare(), equalTo(fareExp.getFare()));
        assertThat("FareInfo.fareType", fareActual.getFareType(), equalTo(fareExp.getName()));

        // フライト情報の検索結果＝0件
        when(flightRepository.findAll(flightInfoCapture.capture(), sortCapture.capture()))
                .thenReturn(new ArrayList<>());
        actualList = service.getVacantSeatInfo(condition);
        assertThat("空席情報が0件であること", actualList.size(), equalTo(0));
    }

}
