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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.m9amsa.reserveNotice.externalmicroservice.model.AirportForEx;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

/**
 * 空港情報取得のFeignクライアントの実現クラス。
 * 
 * <pre>
 * サーキットブレーカーとエラー時の代替処理（フォールバック処理）を実現します。
 * FeignClientではフォールバック処理の実装ができない為、本クラスで実装します。
 * </pre>
 */
@Component
@Slf4j
public class FlightExternalMicroServiceWithFallBack implements FlightExternalMicroService {

    @Autowired
    private FlightExternalMicroService flightExternalMicroService;

    @Retry(name = "flightRetry")
    @CircuitBreaker(name = "flightCircuitBreaker", fallbackMethod = "getAirportFallBack")
    @Override
    public AirportForEx getAirport(String airportId) {
        // delegate to feign interface
        return flightExternalMicroService.getAirport(airportId);
    }

    /**
     * 空港情報取得のサーキットブレーカーのフォールバック処理。
     * 
     * <pre>
     * フライトサービスへの接続ができない場合、空港名を空白とした空港情報を使用します。
     * </pre>
     * 
     * @param airportId      空港Id。
     * @param feignException Feign例外。
     * @return 空港情報。
     */
    public AirportForEx getAirportFallBack(String airportId, FeignException feignException) {
        log.warn("call FlightExternalMicroServiceWithFallBack.getAirportFallBack(airportId = {})", airportId, feignException);
        return AirportForEx.builder().id(airportId).name("").build();
    }
}
