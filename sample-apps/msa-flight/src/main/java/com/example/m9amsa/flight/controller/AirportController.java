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

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.m9amsa.flight.entity.Airport;
import com.example.m9amsa.flight.model.AirportInfo;
import com.example.m9amsa.flight.model.topic.AirportTopic;
import com.example.m9amsa.flight.model.topic.FlightTopicSource;
import com.example.m9amsa.flight.service.AirportService;

/**
 * 空港情報コントローラークラス。
 * 
 */
@RestController
@RequestMapping("/${info.url.root-path}/airport")
@Validated
@EnableBinding(FlightTopicSource.class)
public class AirportController {

    /**
     * 空港サービス。
     */
    @Autowired
    AirportService airportService;

    /**
     * フライトトピック。
     */
    @Autowired
    FlightTopicSource flightTopicSource;

    /**
     * 空港情報を登録します。
     * 
     * <pre>
     * 正常に登録された場合、フライトトピックに空港情報を通知します。
     * 空港情報のメッセージには、ヘッダ情報として x-payload-class: Airport が設定されます。
     * </pre>
     * 
     * @param airportInfo 登録する空港情報。
     */
    @PostMapping
    public void addAirport(@RequestBody @Valid AirportInfo airportInfo) {

        Airport airport = airportService.addAirport(airportInfo);

        AirportTopic airportTopic = new AirportTopic();
        BeanUtils.copyProperties(airport, airportTopic);

        flightTopicSource.output().send(MessageBuilder.withPayload(airportTopic)
                .setHeader("x-payload-class", AirportTopic.class.getSimpleName()).build());

    }

    /**
     * 空港情報のリストを取得します。
     * 
     * <pre>
     * 全ての空港情報を取得します。
     * </pre>
     * 
     * @return 登録されている全ての空港情報。
     */
    @GetMapping("/list")
    public List<Airport> findAirportList() {
        return airportService.findAirportList();

    }

    /**
     * 空港情報を取得します。
     * 
     * <pre>
     * 空港Idに対応する空港情報を取得します。
     * </pre>
     * 
     * @param airportId 空港Id
     * @return 空港情報。
     */
    @GetMapping("/{airportId}")
    public Airport findAirport(@PathVariable @Size(min = 3, max = 3) String airportId) {
        return airportService.findAirport(airportId).get();
    }
}
