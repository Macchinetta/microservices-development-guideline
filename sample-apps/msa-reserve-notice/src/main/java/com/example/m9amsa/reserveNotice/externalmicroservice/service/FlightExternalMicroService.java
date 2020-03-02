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
package com.example.m9amsa.reserveNotice.externalmicroservice.service;

import javax.validation.constraints.Size;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.m9amsa.reserveNotice.externalmicroservice.model.AirportForEx;

/**
 * 空港情報取得のFeignクライアント。
 */
@FeignClient("flight")
public interface FlightExternalMicroService {

    /**
     * 空港情報取得の同期処理リクエスト。
     * 
     * @param airportId 空港Id。
     * @return AirportForEx 空港。
     */
    @GetMapping("${info.url.root-path}/airport/{airportId}")
    AirportForEx getAirport(@PathVariable @Size(min = 3, max = 3) String airportId);
}
