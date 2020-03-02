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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.m9amsa.flight.entity.Airplane;
import com.example.m9amsa.flight.model.AirplaneInfo;
import com.example.m9amsa.flight.model.topic.AirplaneTopic;
import com.example.m9amsa.flight.model.topic.FlightTopicSource;
import com.example.m9amsa.flight.service.AirplaneService;

/**
 * 機体情報コントローラークラス。
 * 
 */
@RestController
@RequestMapping("/${info.url.root-path}/airplane")
@Validated
@EnableBinding(FlightTopicSource.class)
public class AirplaneController {

    /**
     * 機体サービス。
     */
    @Autowired
    AirplaneService airplaneService;

    /**
     * フライトトピック。
     */
    @Autowired
    FlightTopicSource flightTopic;

    /**
     * 機体情報を登録します。
     * 
     * <pre>
     * 正常に登録された場合、フライトトピックに機体情報を通知します。
     * 機体情報のメッセージには、ヘッダ情報として x-payload-class: Airplane が設定されます。
     * </pre>
     * 
     * @param airplaneInfo 登録する機体情報。
     */
    @PostMapping
    public void addAirplane(@RequestBody @Valid AirplaneInfo airplaneInfo) {

        Airplane airplane = airplaneService.addAirplane(airplaneInfo);

        AirplaneTopic airplaneTopic = new AirplaneTopic();

        BeanUtils.copyProperties(airplane, airplaneTopic);
        flightTopic.output().send(MessageBuilder.withPayload(airplaneTopic)
                .setHeader("x-payload-class", AirplaneTopic.class.getSimpleName()).build());
    }

    /**
     * 機体情報を参照します。
     * 
     * <pre>
     * 全ての機体情報を取得します。
     * </pre>
     * 
     * @return 機体情報一覧。登録されている全ての機体情報。
     */
    @GetMapping("/list")
    public List<Airplane> findAirplaneList() {
        return airplaneService.findAirplaneList();

    }

}
