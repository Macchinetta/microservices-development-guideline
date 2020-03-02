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
package com.example.m9amsa.flightTicketFareCalculation.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.m9amsa.flightTicketFareCalculation.model.FareCalcInfo;
import com.example.m9amsa.flightTicketFareCalculation.model.FlightFareInfo;
import com.example.m9amsa.flightTicketFareCalculation.service.FlightTicketFareCalculationService;

/**
 * 運賃計算サービスコントローラクラス。
 * 
 */
@RestController
@RequestMapping("/${info.url.root-path}/flight-ticket-fare")
@Validated
public class FlightTicketFareCalculationController {

    /**
     * フライトチケット運賃計算サービス。
     */
    @Autowired
    private FlightTicketFareCalculationService flightTicketFareCalculationService;

    /**
     * 出発／到着空港をもとに運賃計算を行います。
     * 
     * @param fareCalcInfo 運賃計算入力パラメータ情報。
     * @return 運賃情報一覧。
     */
    @PostMapping
    public List<FlightFareInfo> calcFare(@RequestBody @Valid FareCalcInfo fareCalcInfo) {
        return flightTicketFareCalculationService.calcFare(fareCalcInfo);
    }
}
