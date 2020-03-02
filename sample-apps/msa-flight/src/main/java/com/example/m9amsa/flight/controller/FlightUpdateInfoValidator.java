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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.example.m9amsa.flight.entity.AirplaneRepository;
import com.example.m9amsa.flight.entity.AirportRepository;
import com.example.m9amsa.flight.entity.BasicFarePk;
import com.example.m9amsa.flight.entity.BasicFareRepository;
import com.example.m9amsa.flight.model.FlightUpdateInfo;

/**
 * フライト更新バリデータ。
 * 
 */
@Component
public class FlightUpdateInfoValidator implements Validator {
    /**
     * 機体レポジトリ。
     */
    @Autowired
    AirplaneRepository airplaneRepository;

    /**
     * 空港レポジトリ。
     */
    @Autowired
    AirportRepository airportRepository;

    /**
     * 区間運賃レポジトリ。
     */
    @Autowired
    BasicFareRepository basicFareRepository;

    @Override
    public boolean supports(Class<?> targetClass) {
        return FlightUpdateInfo.class.isAssignableFrom(targetClass);
    }

    /**
     * フライト更新バリデーションチェック。
     */
    @Override
    public void validate(Object target, Errors errors) {
        FlightUpdateInfo flightUpdateInfo = (FlightUpdateInfo) target;

        if ((flightUpdateInfo.getAirplaneId() == null) || //
                (flightUpdateInfo.getDepartureAirportId() == null) || //
                (flightUpdateInfo.getArrivalAirportId() == null)) {
            // 必須チェックは別で行うのでここでは判定しません
            return;
        }

        if (!airplaneRepository.existsById(flightUpdateInfo.getAirplaneId())) {
            errors.rejectValue("airplaneId", "flightUpdateInfo.airplaneId.isNotExists", "Airplane id is not exists.");
        }

        if (!airportRepository.existsById(flightUpdateInfo.getDepartureAirportId())) {
            errors.rejectValue("departureAirportId", "flightUpdateInfo.departureAirportId.isNotExists",
                    "Departure Airport id is not exists.");
        }

        if (!airportRepository.existsById(flightUpdateInfo.getArrivalAirportId())) {
            errors.rejectValue("arrivalAirportId", "flightUpdateInfo.arrivalAirportId.isNotExists",
                    "Arrival Airport id is not exists.");
        }

        if (!basicFareRepository.existsById(BasicFarePk.builder().departure(flightUpdateInfo.getDepartureAirportId())
                .arrival(flightUpdateInfo.getArrivalAirportId()).build())) {
            errors.rejectValue("departureAirportId", "flightUpdateInfo.basicFare.isNotExists",
                    "Basic Fare is not exists.");
            errors.rejectValue("arrivalAirportId", "flightUpdateInfo.basicFare.isNotExists",
                    "Basic Fare is not exists.");
        }
    }

}
