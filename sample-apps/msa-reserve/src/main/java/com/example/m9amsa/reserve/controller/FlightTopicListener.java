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
package com.example.m9amsa.reserve.controller;

import java.lang.reflect.InvocationTargetException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import com.example.m9amsa.reserve.model.topic.AirplaneTopic;
import com.example.m9amsa.reserve.model.topic.AirportTopic;
import com.example.m9amsa.reserve.model.topic.BasicFareTopic;
import com.example.m9amsa.reserve.model.topic.FlightTopic;
import com.example.m9amsa.reserve.model.topic.FlightTopicSink;
import com.example.m9amsa.reserve.model.topic.FlightVacantSeatTopic;
import com.example.m9amsa.reserve.service.FlightTopicService;

/**
 * ｛@code flight-topic}のリスナークラス。
 *
 */
@EnableBinding(FlightTopicSink.class)
public class FlightTopicListener {

    /**
     * FligthtInfoテーブルを操作するサービスクラス。
     */
    @Autowired
    FlightTopicService service;

    /**
     * ｛@code flight-topic.FlightTopic}を参照します。
     * 
     * @param flightTopic フライト情報。 ｛@code flight-topic.FlightTopic}に対応するエンティティ。
     * @throws InvocationTargetException 呼び出されたメソッドによってスローされた例外。
     * @throws IllegalAccessException    現在実行中のメソッドにはアクセス権がない場合、スローされた例外。
     */
    @StreamListener(value = FlightTopicSink.INPUT, condition = "headers['x-payload-class']=='FlightTopic'")
    public void handleFlight(FlightTopic flightTopic) throws IllegalAccessException, InvocationTargetException {
        service.registerFlightInfo(flightTopic);
    }

    /**
     * ｛@code flight-topic.Airport}を参照します。
     * 
     * @param airportTopic 空港情報。｛@code flight-topic.AirportTopic}に対応するエンティティ。
     */
    @StreamListener(value = FlightTopicSink.INPUT, condition = "headers['x-payload-class']=='AirportTopic'")
    public void handleAirport(AirportTopic airportTopic) {
        service.registerAirport(airportTopic);
    }

    /**
     * ｛@code flight-topic.AirplaneTopic}を参照します。
     * 
     * @param airplaneTopic 機体情報。｛@code flight-topic.AirplaneTopic}に対応するエンティティ。
     * 
     */
    @StreamListener(value = FlightTopicSink.INPUT, condition = "headers['x-payload-class']=='AirplaneTopic'")
    public void handleAirplane(AirplaneTopic airplaneTopic) {
        service.registerAirplane(airplaneTopic);
    }

    /**
     * ｛@code flight-topic.BasicFare}を参照します。
     * 
     * @param basicFareTopic 区間運賃情報。｛@code flight-topic.BasicFare}に対応するエンティティ。
     */
    @StreamListener(value = FlightTopicSink.INPUT, condition = "headers['x-payload-class']=='BasicFareTopic'")
    public void handleBasicFare(BasicFareTopic basicFareTopic) {
        service.registerBasicFare(basicFareTopic);
    }

    /**
     * ｛@code flight-topic.FlightVacantSeatInfo}を参照します。
     * 
     * @param flightVacantSeatTopic フライト空席情報。｛@code
     *                              flight-topic.FlightVacantSeatInfo}に対応するエンティティ。
     */
    @StreamListener(value = FlightTopicSink.INPUT, condition = "headers['x-payload-class']=='FlightVacantSeatTopic'")
    public void handleFlightVacantSeatInfo(FlightVacantSeatTopic flightVacantSeatTopic) {
        service.registerFlightVacantSeat(flightVacantSeatTopic);
    }
}
