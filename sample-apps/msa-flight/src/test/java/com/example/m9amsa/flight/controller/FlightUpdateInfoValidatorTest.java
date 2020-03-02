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
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import com.example.m9amsa.flight.entity.AirplaneRepository;
import com.example.m9amsa.flight.entity.AirportRepository;
import com.example.m9amsa.flight.entity.BasicFareRepository;
import com.example.m9amsa.flight.entity.Flight;
import com.example.m9amsa.flight.model.FlightUpdateInfo;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class FlightUpdateInfoValidatorTest {

    @Autowired
    private FlightUpdateInfoValidator flightUpdateInfoValidator;

    @MockBean
    private AirplaneRepository airplaneRepository;

    @MockBean
    private AirportRepository airportRepository;

    @MockBean
    private BasicFareRepository basicFareRepository;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(airplaneRepository);
        reset(airportRepository);
        reset(basicFareRepository);
    }

    @Test
    public void testSupports() {

        boolean actual = flightUpdateInfoValidator.supports(FlightUpdateInfo.class);

        assertTrue("FlightUpdateInfoが対象であること", actual);

        actual = flightUpdateInfoValidator.supports(Flight.class);

        assertFalse("FlightUpdateInfo以外は対象でないこと", actual);
    }

    @Test
    public void testValidate() {
        LocalDateTime departureTime = LocalDateTime.parse("1901-01-01T10:00:00");
        LocalDateTime arrivalTime = LocalDateTime.parse("1901-01-01T11:30:00");

        FlightUpdateInfo flightUpdateInfo = FlightUpdateInfo.builder().name("MSA001").departureAirportId("HND")
                .departureTime(departureTime).arrivalAirportId("OSA").arrivalTime(arrivalTime).airplaneId(1L).build();

        Errors errors = new BeanPropertyBindingResult(flightUpdateInfo, "flightUpdateInfo");
        flightUpdateInfoValidator.validate(flightUpdateInfo, errors);

        assertTrue("エラーが発生していること", errors.hasErrors());

        assertThat("エラー件数が正しいこと", errors.getErrorCount(), equalTo(5));

        assertNotNull("FieldError/airplaneId", errors.getFieldError("airplaneId"));
        assertThat("FieldError/airplaneId/code", errors.getFieldError("airplaneId").getCode(),
                equalTo("flightUpdateInfo.airplaneId.isNotExists"));

        assertThat("FieldError/departureAirportId", errors.getFieldErrors("departureAirportId").size(), equalTo(2));
        // エラーコードを収集
        List<String> errorCodes = errors.getFieldErrors("departureAirportId").stream().map(FieldError::getCode)
                .collect(Collectors.toList());
        assertThat("FieldError/departureAirportId/code", errorCodes,
                hasItems("flightUpdateInfo.departureAirportId.isNotExists", "flightUpdateInfo.basicFare.isNotExists"));

        errorCodes = errors.getFieldErrors("arrivalAirportId").stream().map(FieldError::getCode)
                .collect(Collectors.toList());
        assertThat("FieldError/arrivalAirportId", errors.getFieldErrors("arrivalAirportId").size(), equalTo(2));
        assertThat("FieldError/arrivalAirportId/code", errorCodes,
                hasItems("flightUpdateInfo.arrivalAirportId.isNotExists", "flightUpdateInfo.basicFare.isNotExists"));

        // パラメータのnullチェック
        errors = new BeanPropertyBindingResult(flightUpdateInfo, "flightUpdateInfo");

        flightUpdateInfo.setAirplaneId(null);
        flightUpdateInfoValidator.validate(flightUpdateInfo, errors);
        assertFalse("エラーが発生していないこと", errors.hasErrors());

        flightUpdateInfo.setAirplaneId(1L);
        flightUpdateInfo.setDepartureAirportId(null);
        flightUpdateInfoValidator.validate(flightUpdateInfo, errors);
        assertFalse("エラーが発生していないこと", errors.hasErrors());

        flightUpdateInfo.setDepartureAirportId("HND");
        flightUpdateInfo.setArrivalAirportId(null);
        flightUpdateInfoValidator.validate(flightUpdateInfo, errors);
        assertFalse("エラーが発生していないこと", errors.hasErrors());

    }

}
