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

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.reserve.entity.Airplane;
import com.example.m9amsa.reserve.entity.AirplaneRepository;
import com.example.m9amsa.reserve.entity.Airport;
import com.example.m9amsa.reserve.entity.AirportRepository;
import com.example.m9amsa.reserve.entity.BasicFare;
import com.example.m9amsa.reserve.entity.BasicFareId;
import com.example.m9amsa.reserve.entity.BasicFareRepository;
import com.example.m9amsa.reserve.entity.Flight;
import com.example.m9amsa.reserve.entity.FlightRepository;
import com.example.m9amsa.reserve.entity.FlightVacantSeat;
import com.example.m9amsa.reserve.entity.FlightVacantSeatRepository;
import com.example.m9amsa.reserve.exception.HttpStatus500Exception;
import com.example.m9amsa.reserve.model.topic.AirplaneTopic;
import com.example.m9amsa.reserve.model.topic.AirportTopic;
import com.example.m9amsa.reserve.model.topic.BasicFareTopic;
import com.example.m9amsa.reserve.model.topic.FlightTopic;
import com.example.m9amsa.reserve.model.topic.FlightVacantSeatTopic;

/**
 * FligthtTopicを処理するサービスクラス。
 *
 */
@Service
public class FlightTopicService {

    /**
     * フライト情報Repository。
     */
    @Autowired
    FlightRepository flightRepository;

    /**
     * 空港リポジトリ。
     */
    @Autowired
    AirportRepository airportRepository;

    /**
     * 機体リポジトリ。
     */
    @Autowired
    AirplaneRepository airplaneRepository;

    /**
     * 基本料金リポジトリ。
     */
    @Autowired
    BasicFareRepository basicFareRepository;

    /**
     * フライト空席情報リポジトリ。
     */
    @Autowired
    FlightVacantSeatRepository flightVacantSeatRepository;

    /**
     * FlightInfoを登録・更新します。
     * 
     * @param flightTopic フライト情報。
     * @throws InvocationTargetException 呼び出されたメソッドによってスローされた例外。
     * @throws IllegalAccessException    現在実行中のメソッドにはアクセス権がない場合、スローされた例外。
     */
    @Transactional
    public void registerFlightInfo(FlightTopic flightTopic) throws IllegalAccessException, InvocationTargetException {

        Optional<Airplane> airplane = airplaneRepository.findById(flightTopic.getAirplaneId());
        BasicFareId basicFareId = BasicFareId.builder().arrivalAirportId(flightTopic.getArrivalAirportId())
                .departureAirportId(flightTopic.getDepartureAirportId()).build();
        Optional<BasicFare> basicFare = basicFareRepository.findById(basicFareId);

        // flightに対応する機体、料金が無い場合はエラー
        String msg = airplane.isEmpty() ? String.format("[airplaneId=%s]", flightTopic.getAirplaneId()) : "";
        msg += basicFare.isEmpty() ? String.format("[%s]", basicFareId) : "";
        if (!msg.isEmpty()) {
            throw new HttpStatus500Exception("Invalid ID: " + msg);
        }

        Flight flight = new Flight();
        BeanUtils.copyProperties(airplane.get(), flight);
        flight.setAirplaneName(airplane.get().getName());
        BeanUtils.copyProperties(basicFare.get(), flight);
        BeanUtils.copyProperties(flightTopic, flight);
        flightRepository.save(flight);
    }

    /**
     * Airportを登録・更新します。
     * 
     * @param airportTopic 空港情報。
     */
    @Transactional
    public void registerAirport(AirportTopic airportTopic) {
        Airport airport = new Airport();
        BeanUtils.copyProperties(airportTopic, airport);
        airportRepository.save(airport);
    }

    /**
     * Airplaneを登録・更新します。
     * 
     * @param airplaneTopic 機体情報。
     */
    @Transactional
    public void registerAirplane(AirplaneTopic airplaneTopic) {
        Airplane airplane = new Airplane();
        BeanUtils.copyProperties(airplaneTopic, airplane);
        airplaneRepository.save(airplane);
    }

    /**
     * BasicFareを登録・更新します。
     * 
     * @param basicFareTopic 区間運賃。
     */
    @Transactional
    public void registerBasicFare(BasicFareTopic basicFareTopic) {
        BasicFare basicFare = new BasicFare();
        BeanUtils.copyProperties(basicFareTopic, basicFare);
        basicFareRepository.save(basicFare);
    }

    /**
     * FlightVacantSeatInfoを登録・更新します。
     * 
     * @param flightVacantSeatTopic フライト空席情報。
     */
    @Transactional
    public void registerFlightVacantSeat(FlightVacantSeatTopic flightVacantSeatTopic) {
        FlightVacantSeat flightVacantSeat = new FlightVacantSeat();
        BeanUtils.copyProperties(flightVacantSeatTopic, flightVacantSeat);
        flightVacantSeatRepository.save(flightVacantSeat);
    }
}
