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
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.m9amsa.flight.entity.BasicFare;
import com.example.m9amsa.flight.model.BasicFareInfo;
import com.example.m9amsa.flight.model.topic.BasicFareTopic;
import com.example.m9amsa.flight.model.topic.FlightTopicSource;
import com.example.m9amsa.flight.service.BasicFareService;

import reactor.util.function.Tuple2;

/**
 * 区間運賃情報コントローラークラス。
 * 
 */
@RestController
@RequestMapping("/${info.url.root-path}/basic-fare")
@Validated
@EnableBinding(FlightTopicSource.class)
public class BasicFareController {

    /**
     * 区間運賃サービス。
     */
    @Autowired
    BasicFareService basicFareService;

    /**
     * フライトトピック。
     */
    @Autowired
    FlightTopicSource flightTopicSource;

    /**
     * 機体情報を登録します。
     * 
     * <pre>
     * 正常に登録された場合、フライトトピックに機体情報を通知します。
     * 機体情報のメッセージには、ヘッダ情報として x-payload-class: BasicFareが設定されます。
     * </pre>
     * 
     * @param basicFare 登録する機体情報。
     */
    @PostMapping
    public void addBasicFare(@RequestBody @Valid BasicFareInfo basicFare) {

        Tuple2<BasicFare, BasicFare> result = basicFareService.addBasicFare(basicFare);

        BasicFareTopic basicFareTopic1 = new BasicFareTopic();
        basicFareTopic1.setFare(result.getT1().getFare());
        basicFareTopic1.setArrivalAirportId(result.getT1().getArrival());
        basicFareTopic1.setDepartureAirportId(result.getT1().getDeparture());
        flightTopicSource.output().send(MessageBuilder.withPayload(basicFareTopic1)
                .setHeader("x-payload-class", BasicFareTopic.class.getSimpleName()).build());
        BasicFareTopic basicFareTopic2 = new BasicFareTopic();
        basicFareTopic2.setFare(result.getT2().getFare());
        basicFareTopic2.setArrivalAirportId(result.getT2().getArrival());
        basicFareTopic2.setDepartureAirportId(result.getT2().getDeparture());
        flightTopicSource.output().send(MessageBuilder.withPayload(basicFareTopic2)
                .setHeader("x-payload-class", BasicFareTopic.class.getSimpleName()).build());
    }

    /**
     * 機体情報を参照します。
     * 
     * <pre>
     * 全ての機体情報を取得します。
     * </pre>
     * 
     * @param departureId 出発空港Id。3桁。
     * @param arrivalId   到着空港Id。3桁。
     * 
     * @return 登録されている全ての機体情報。
     */
    @GetMapping("/list")
    public List<BasicFare> findBasicFareList( //
            @RequestParam(name = "d", required = false) @Size(min = 3, max = 3) @Valid String departureId,
            @RequestParam(name = "a", required = false) @Size(min = 3, max = 3) @Valid String arrivalId) {
        return basicFareService.findBasicFareList(Optional.ofNullable(departureId), Optional.ofNullable(arrivalId));

    }

}
