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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import com.example.m9amsa.flight.entity.ReserveVacantSeat;
import com.example.m9amsa.flight.entity.ReserveVacantSeatRepository;
import com.example.m9amsa.flight.entity.VacantSeat;
import com.example.m9amsa.flight.entity.VacantSeatPk;
import com.example.m9amsa.flight.entity.VacantSeatRepository;
import com.example.m9amsa.flight.model.ReserveVacantSeatInfo;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
@Slf4j
public class CancelReservedSeatServiceTest {

    @Autowired
    private ReserveVacantSeatService reserveVacantSeatService;

    @Autowired
    private CancelReservedSeatService cancelReservedSeatService;;

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
     * Test for cancelReservedSeat.
     */
    @Test
    public void tettcancelReservedSeat() {
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

        ReserveVacantSeatInfo reserveVacantSeat;
        VacantSeat vacantSeat;

        VacantSeatPk vacantSeatPk = VacantSeatPk.builder().departureDate(departureDate).flightName(flightName).build();
        int reserveSeatCount = 1;
        // N席を確保
        reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId).departureDate(departureDate)
                .flightName(flightName).seatClass(SeatClass.N).vacantSeatCount(reserveSeatCount).build();
        // 空席を確保します
        reserveVacantSeatService.reserveVacantSeat(reserveVacantSeat);
        vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
        log.info("残空席数。N:{}, S:{}", vacantSeat.getVacantStandardSeatCount(), vacantSeat.getVacantSpecialSeatCount());
        // 取り消し
        vacantSeatPk = VacantSeatPk.builder().departureDate(departureDate).flightName(flightName).build();
        VacantSeat vacantSeatBefore = vacantSeatRepository.findById(vacantSeatPk).get();
        assertNotNull("空席情報が存在すること。", vacantSeatBefore);
        log.info("取り消し前の残空席数。N:{}, S:{}", vacantSeatBefore.getVacantStandardSeatCount(),
                vacantSeatBefore.getVacantSpecialSeatCount());

        Optional<ReserveVacantSeat> reserveVacantSeatFromDb = reserveVacantSeatRepository.findById(reserveId);
        assertTrue("空席確保情報が存在すること。", reserveVacantSeatFromDb.isPresent());
        cancelReservedSeatService.cancelReservedSeat(reserveVacantSeat);
        reserveVacantSeatFromDb = reserveVacantSeatRepository.findById(reserveId);
        assertTrue("空席確保情報が削除されたこと。", reserveVacantSeatFromDb.isEmpty());
        vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
        log.info("取り消し後の残空席数。N:{}, S:{}", vacantSeat.getVacantStandardSeatCount(),
                vacantSeat.getVacantSpecialSeatCount());
        assertEquals("空席数(N)が確保取り消しされました。", flight.get().getAirplane().getStandardSeats().intValue(),
                vacantSeat.getVacantStandardSeatCount().intValue());
        assertEquals("空席数(S)が変わっていない。", flight.get().getAirplane().getSpecialSeats().intValue(),
                vacantSeat.getVacantSpecialSeatCount().intValue());

        // S席
        reserveId = 2L;
        reserveSeatCount = 2;
        reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId).departureDate(departureDate)
                .flightName(flightName).seatClass(SeatClass.S).vacantSeatCount(reserveSeatCount).build();
        // 空席を確保します
        reserveVacantSeatService.reserveVacantSeat(reserveVacantSeat);
        vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
        log.info("残空席数。N:{}, S:{}", vacantSeat.getVacantStandardSeatCount(), vacantSeat.getVacantSpecialSeatCount());
        // 取り消し
        vacantSeatPk = VacantSeatPk.builder().departureDate(departureDate).flightName(flightName).build();
        vacantSeatBefore = vacantSeatRepository.findById(vacantSeatPk).get();
        assertNotNull("空席情報が存在すること。", vacantSeatBefore);
        log.info("取り消し前の残空席数。N:{}, S:{}", vacantSeatBefore.getVacantStandardSeatCount(),
                vacantSeatBefore.getVacantSpecialSeatCount());

        reserveVacantSeatFromDb = reserveVacantSeatRepository.findById(reserveId);
        assertTrue("空席確保情報が存在すること。", reserveVacantSeatFromDb.isPresent());
        cancelReservedSeatService.cancelReservedSeat(reserveVacantSeat);
        reserveVacantSeatFromDb = reserveVacantSeatRepository.findById(reserveId);
        assertTrue("空席確保情報が削除されたこと。", reserveVacantSeatFromDb.isEmpty());
        vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
        log.info("取り消し後の残空席数。N:{}, S:{}", vacantSeat.getVacantStandardSeatCount(),
                vacantSeat.getVacantSpecialSeatCount());
        assertEquals("空席数(N)が変わっていない。", flight.get().getAirplane().getStandardSeats().intValue(),
                vacantSeat.getVacantStandardSeatCount().intValue());
        assertEquals("空席数(S)が確保取り消しされました。", flight.get().getAirplane().getSpecialSeats().intValue(),
                vacantSeat.getVacantSpecialSeatCount().intValue());

    }

    /**
     * 冪等性のテスト。
     */
    @Test
    public void testcancelReservedSeatForIdempotence() {
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
        VacantSeatPk vacantSeatPk = VacantSeatPk.builder().departureDate(departureDate).flightName(flightName).build();

        VacantSeat vacantSeat;
        int reserveSeatCount = 1;
        // N席を確保
        ReserveVacantSeatInfo reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId)
                .departureDate(departureDate).flightName(flightName).seatClass(SeatClass.N)
                .vacantSeatCount(reserveSeatCount).build();
        reserveVacantSeatService.reserveVacantSeat(reserveVacantSeat);
        vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();

        // 取り消し
        vacantSeatPk = VacantSeatPk.builder().departureDate(departureDate).flightName(flightName).build();

        int timesOfRetry = 5;
        for (int i = 1; i <= timesOfRetry; i++) {
            // 空席を確保します
            cancelReservedSeatService.cancelReservedSeat(reserveVacantSeat);
            vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
            assertEquals("第" + i + "回目の処理でも,空席数(N)が確保取り消しされました。",
                    flight.get().getAirplane().getStandardSeats().intValue(),
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
