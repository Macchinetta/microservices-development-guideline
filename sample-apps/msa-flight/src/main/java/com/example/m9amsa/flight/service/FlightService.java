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

import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.flight.entity.Airplane;
import com.example.m9amsa.flight.entity.AirplaneRepository;
import com.example.m9amsa.flight.entity.Airport;
import com.example.m9amsa.flight.entity.AirportRepository;
import com.example.m9amsa.flight.entity.BasicFare;
import com.example.m9amsa.flight.entity.BasicFarePk;
import com.example.m9amsa.flight.entity.BasicFareRepository;
import com.example.m9amsa.flight.entity.Flight;
import com.example.m9amsa.flight.entity.FlightRepository;
import com.example.m9amsa.flight.model.FlightUpdateInfo;

/**
 * フライト情報ビジネスロジック。
 * 
 */
@Service
public class FlightService {

    /**
     * フライト情報リポジトリ。
     */
    @Autowired
    FlightRepository flightRepository;

    /**
     * 機体情報リポジトリ。
     */
    @Autowired
    AirplaneRepository airplaneRepository;

    /**
     * 空港情報リポジトリ。
     */
    @Autowired
    AirportRepository airportRepository;

    /**
     * 区間運賃情報リポジトリ。
     */
    @Autowired
    BasicFareRepository basicFareRepository;

    /**
     * フライト情報を登録します。
     * 
     * <pre>
     * 既に同じフライト情報が登録されている場合は更新を行います。
     * </pre>
     * 
     * @param flightUpdateInfo フライト情報。
     * @return フライト情報。DBに保存したフライト情報。
     */
    @Transactional
    public Flight addFlight(FlightUpdateInfo flightUpdateInfo) {
        Optional<Airplane> airplane = airplaneRepository.findById(flightUpdateInfo.getAirplaneId());
        Optional<Airport> departure = airportRepository.findById(flightUpdateInfo.getDepartureAirportId());
        Optional<Airport> arrival = airportRepository.findById(flightUpdateInfo.getArrivalAirportId());
        Optional<BasicFare> basicFare = basicFareRepository
                .findById(BasicFarePk.builder().departure(flightUpdateInfo.getDepartureAirportId())
                        .arrival(flightUpdateInfo.getArrivalAirportId()).build());

        Flight flight = new Flight();
        BeanUtils.copyProperties(flightUpdateInfo, flight);
        flight.setDepartureTime(flight.getDepartureTime().withYear(1901).withMonth(1).withDayOfMonth(1));
        flight.setArrivalTime(flight.getArrivalTime().withYear(1901).withMonth(1).withDayOfMonth(1));
        flight.setAirplane(airplane.orElseThrow());
        flight.setDepartureAirport(departure.orElseThrow());
        flight.setArrivalAirport(arrival.orElseThrow());
        flight.setBasicFare(basicFare.orElseThrow());

        return flightRepository.save(flight);
    }

    /**
     * フライト情報を参照します。
     * 
     * <pre>
     * 登録されているフライト情報をすべて取得します。
     * </pre>
     * 
     * @param departureId 出発空港Id。
     * @param arrivalId   到着空港Id。
     * 
     * @return フライト情報のリスト。フライト情報が存在しない場合は0件のリストを返却します。
     */
    @Transactional(readOnly = true)
    public List<Flight> findFlightList(Optional<String> departureId, Optional<String> arrivalId) {
        Example<Flight> example = Example.of(Flight.builder()
                .departureAirport(departureId.flatMap(id -> airportRepository.findById(id)).orElse(null))
                .arrivalAirport(arrivalId.flatMap(id -> airportRepository.findById(id)).orElse(null)).build());
        return flightRepository.findAll(example);
    }
}
