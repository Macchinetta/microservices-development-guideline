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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import com.example.m9amsa.flight.constant.SeatClass;
import com.example.m9amsa.flight.entity.Flight;
import com.example.m9amsa.flight.entity.FlightRepository;
import com.example.m9amsa.flight.entity.ReserveVacantSeat;
import com.example.m9amsa.flight.exception.HttpStatus404Exception;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class ReserveVacantSeatValidatorTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private ReserveVacantSeatValidator reserveVacantSeatValidator;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSupports() {

        boolean actual = reserveVacantSeatValidator.supports(ReserveVacantSeat.class);

        assertTrue("ReserveVacantSeatが対象であること", actual);

        actual = reserveVacantSeatValidator.supports(Flight.class);

        assertFalse("ReserveVacantSeat以外は対象でないこと", actual);
    }

    @Test
    public void testValidate() {

        LocalDate departureDate = LocalDate.parse("1901-01-01");
        String flightName = "NTT01";

        ReserveVacantSeat reserveVacantSeat = ReserveVacantSeat.builder()//
                .reserveId(1L)//
                .departureDate(departureDate)//
                .flightName(flightName)//
                .seatClass(SeatClass.N)//
                .vacantSeatCount(20)//
                .build();

        Errors errors = new BeanPropertyBindingResult(reserveVacantSeat, "reserveVacantSeat");
        doReturn(false).when(flightRepository).existsById(flightName);
        try {
            reserveVacantSeatValidator.validate(reserveVacantSeat, errors);
        } catch (Exception e) {
            assertThat("フライト情報が存在しない。", e, is(instanceOf(HttpStatus404Exception.class)));
        }

        errors = new BeanPropertyBindingResult(reserveVacantSeat, "reserveVacantSeat");
        doReturn(true).when(flightRepository).existsById(flightName);
        reserveVacantSeatValidator.validate(reserveVacantSeat, errors);

        assertFalse("エラーが発生していないこと", errors.hasErrors());

    }

}
