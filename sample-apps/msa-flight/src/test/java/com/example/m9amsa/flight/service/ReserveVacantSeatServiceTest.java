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
package com.example.m9amsa.flight.service;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flight.constant.SeatClass;
import com.example.m9amsa.flight.entity.Airplane;
import com.example.m9amsa.flight.entity.AirplaneRepository;
import com.example.m9amsa.flight.entity.Airport;
import com.example.m9amsa.flight.entity.AirportRepository;
import com.example.m9amsa.flight.entity.BasicFare;
import com.example.m9amsa.flight.entity.BasicFareRepository;
import com.example.m9amsa.flight.entity.Flight;
import com.example.m9amsa.flight.entity.FlightRepository;
import com.example.m9amsa.flight.entity.ReserveVacantSeatRepository;
import com.example.m9amsa.flight.entity.VacantSeat;
import com.example.m9amsa.flight.entity.VacantSeatPk;
import com.example.m9amsa.flight.entity.VacantSeatRepository;
import com.example.m9amsa.flight.exception.HttpStatus404Exception;
import com.example.m9amsa.flight.exception.BusinessException;
import com.example.m9amsa.flight.model.ReserveVacantSeatInfo;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
@Slf4j
public class ReserveVacantSeatServiceTest {

    @Autowired
    private ReserveVacantSeatService reserveVacantSeatService;

    @Autowired
    private ReserveVacantSeatRepository reserveVacantSeatRepository;

    @Autowired
    private VacantSeatRepository vacantSeatRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AirplaneRepository airplaneRepository;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private BasicFareRepository basicFareRepository;

    @Before
    public void before() {
        flightRepository.deleteAll();
        flightRepository.flush();
        airplaneRepository.deleteAll();
        airplaneRepository.flush();
        reserveVacantSeatRepository.deleteAll();
        reserveVacantSeatRepository.flush();
        vacantSeatRepository.deleteAll();
        vacantSeatRepository.flush();
    }

    @After
    public void after() {
        reserveVacantSeatRepository.deleteAll();
        reserveVacantSeatRepository.flush();
        vacantSeatRepository.deleteAll();
        vacantSeatRepository.flush();
    }

    /**
     * Test for reserveVacantSeat.
     */
    @Test
    public void testReserveVacantSeat() {

        Long reserveId = 1L;
        String flightName = "NTT01";
        LocalDate departureDate = LocalDate.of(2019, 12, 1);

        Flight flightForInsert = createFlight(flightName);
        basicFareRepository.saveAndFlush(flightForInsert.getBasicFare());
        airplaneRepository.saveAndFlush(flightForInsert.getAirplane());
        airportRepository.saveAndFlush(flightForInsert.getDepartureAirport());
        airportRepository.saveAndFlush(flightForInsert.getArrivalAirport());

        flightRepository.saveAndFlush(flightForInsert);
        Optional<Flight> flight = flightRepository.findById(flightName);
        assertNotNull("フライト情報が存在する。", flight.get());

        // 空席情報なし
        VacantSeatPk vacantSeatPk = VacantSeatPk.builder().departureDate(departureDate).flightName(flightName).build();
        assertTrue("空席情報なし。", vacantSeatRepository.findById(vacantSeatPk).isEmpty());
        assertTrue("空席確保情報なし。", reserveVacantSeatRepository.findById(reserveId).isEmpty());

        ReserveVacantSeatInfo reserveVacantSeat;
        VacantSeat vacantSeat;
        int reserveSeatCount = 1;
        // N席を確保
        reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId).departureDate(departureDate)
                .flightName(flightName).seatClass(SeatClass.N).vacantSeatCount(reserveSeatCount).build();
        // 空席を確保します
        reserveVacantSeatService.reserveVacantSeat(reserveVacantSeat);
        vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
        assertNotNull("空席情報が作成されました。", vacantSeat);
        assertTrue("空席確保情報が作成された。", reserveVacantSeatRepository.findById(reserveId).isPresent());

        log.info("残空席数。N:{}, S:{}", vacantSeat.getVacantStandardSeatCount(), vacantSeat.getVacantSpecialSeatCount());
        assertEquals("空席数(N)が確保されました。", flight.get().getAirplane().getStandardSeats().intValue() - reserveSeatCount,
                vacantSeat.getVacantStandardSeatCount().intValue());
        assertEquals("空席数(S)が変わっていない。", flight.get().getAirplane().getSpecialSeats().intValue(),
                vacantSeat.getVacantSpecialSeatCount().intValue());

        reserveId = 2L;
        reserveSeatCount = 20;
        vacantSeatPk = VacantSeatPk.builder().departureDate(departureDate).flightName(flightName).build();
        VacantSeat vacantSeatBefore = vacantSeatRepository.findById(vacantSeatPk).get().toBuilder().build();
        // S席を確保
        reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId).departureDate(departureDate)
                .flightName(flightName).seatClass(SeatClass.S).vacantSeatCount(reserveSeatCount).build();
        // 空席を確保します
        reserveVacantSeatService.reserveVacantSeat(reserveVacantSeat);
        assertTrue("空席確保情報が作成された。", reserveVacantSeatRepository.findById(reserveId).isPresent());

        vacantSeatPk = VacantSeatPk.builder().departureDate(departureDate).flightName(flightName).build();
        vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
        log.info("残空席数。N:{}, S:{}", vacantSeat.getVacantStandardSeatCount(), vacantSeat.getVacantSpecialSeatCount());
        assertEquals("空席数(N)が変わっていない。", vacantSeatBefore.getVacantStandardSeatCount().intValue(),
                vacantSeat.getVacantStandardSeatCount().intValue());
        assertEquals("空席数(S)が確保されました。", vacantSeatBefore.getVacantSpecialSeatCount().intValue() - reserveSeatCount,
                vacantSeat.getVacantSpecialSeatCount().intValue());

        // 異常テスト
        // 空席（N)不足
        reserveId = 3L;
        reserveSeatCount = 20;
        // N席を確保
        reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId).departureDate(departureDate)
                .flightName(flightName).seatClass(SeatClass.N).vacantSeatCount(reserveSeatCount).build();
        try {
            // 空席を確保します
            reserveVacantSeatService.reserveVacantSeat(reserveVacantSeat);
        } catch (Exception e) {
            assertThat("一般席(N)空席が不足であること。", e, is(instanceOf(BusinessException.class)));
        }

        // 空席（S)不足
        reserveId = 4L;
        reserveSeatCount = 1;
        vacantSeatPk = VacantSeatPk.builder().departureDate(departureDate).flightName(flightName).build();
        // S席を確保
        reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId).departureDate(departureDate)
                .flightName(flightName).seatClass(SeatClass.S).vacantSeatCount(reserveSeatCount).build();
        try {
            // 空席を確保します
            reserveVacantSeatService.reserveVacantSeat(reserveVacantSeat);
        } catch (Exception e) {
            assertThat("特別席(S)空席が不足であること。", e, is(instanceOf(BusinessException.class)));
        }

        // フライト情報が存在しません。
        flightName = "NTT02";
        departureDate = LocalDate.of(2019, 12, 1);
        flightRepository.findById(flightName).ifPresent(f -> flightRepository.deleteById(f.getName()));
        flight = flightRepository.findById(flightName);
        assertTrue("フライト情報が存在する。", flight.isEmpty());

        reserveId = 5L;
        reserveSeatCount = 1;
        // N席を確保
        reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId).departureDate(departureDate)
                .flightName(flightName).seatClass(SeatClass.N).vacantSeatCount(reserveSeatCount).build();
        try {
            // 空席を確保します
            reserveVacantSeatService.reserveVacantSeat(reserveVacantSeat);
        } catch (Exception e) {
            assertThat("フライト情報が存在しない。", e, is(instanceOf(HttpStatus404Exception.class)));
        }

    }

    /**
     * 冪等性のテスト。
     */
    @Test
    public void testReserveVacantSeatForIdempotence() {
        Long reserveId = 10L;
        String flightName = "NTT01";
        LocalDate departureDate = LocalDate.of(2019, 12, 1);

        Flight flightForInsert = createFlight(flightName);
        basicFareRepository.saveAndFlush(flightForInsert.getBasicFare());
        airplaneRepository.saveAndFlush(flightForInsert.getAirplane());
        airportRepository.saveAndFlush(flightForInsert.getDepartureAirport());
        airportRepository.saveAndFlush(flightForInsert.getArrivalAirport());

        flightRepository.saveAndFlush(flightForInsert);
        Optional<Flight> flight = flightRepository.findById(flightName);
        VacantSeatPk vacantSeatPk = VacantSeatPk.builder().departureDate(departureDate).flightName(flightName).build();

        int reserveSeatCount = 1;
        ReserveVacantSeatInfo reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId)
                .departureDate(departureDate).flightName(flightName).seatClass(SeatClass.N)
                .vacantSeatCount(reserveSeatCount).build();

        VacantSeat vacantSeat;
        int timesOfRetry = 5;
        for (int i = 1; i <= timesOfRetry; i++) {
            reserveVacantSeatService.reserveVacantSeat(reserveVacantSeat);
            vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
            assertEquals("第" + i + "回目の処理でも、空席数(N)が正しく確保されました。",
                    flight.get().getAirplane().getStandardSeats().intValue() - reserveSeatCount,
                    vacantSeat.getVacantStandardSeatCount().intValue());
        }
    }

    /**
     * フライト情報を作成します。
     * 
     * @param flightName 便名
     * @return フライト情報
     */
    private Flight createFlight(String flightName) {
        Airplane airplane = Airplane.builder()//
                .name(flightName)//
                .standardSeats(20)//
                .specialSeats(20)//
                .build();
        Airport departureAirport = Airport.builder()//
                .id("OSA")//
                .name("大阪")//
                .build();

        Airport arrivalAirport = Airport.builder()//
                .id("HND")//
                .name("東京")//
                .build();

        BasicFare basicFare = BasicFare.builder() //
                .departure("OSA")//
                .arrival("HND")//
                .fare(1000)//
                .build();

        // Flight
        return Flight.builder()//
                .name(flightName)//
                .departureTime(LocalDateTime.now())//
                .arrivalTime(LocalDateTime.now())//
                .airplane(airplane)//
                .departureAirport(departureAirport)//
                .arrivalAirport(arrivalAirport)//
                .basicFare(basicFare)//
                .build();
    }
}
