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
package com.example.m9amsa.reserve.externalmicroservice.service;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.m9amsa.reserve.externalmicroservice.model.FareCalcInfoForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.FlightFareForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.HealthCheckStatusForEx;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * 運賃計算サービスのFeignクライアント。
 */
@FeignClient("flight-ticket-fare-calculation")
public interface CalculateFareExternalMicroService {

    /**
     * 運賃計算処理の同期処理リクエスト。
     * 
     * @param fareCalcInfoForEx 運賃計算リクエストパラメータ。
     * @return 運賃計算結果。
     */
    @Retry(name = "calculateFareRetry")
    @CircuitBreaker(name = "calculateFareCircuitBreaker")
    @PostMapping("${info.url.root-path}/flight-ticket-fare")
    List<FlightFareForEx> calcFare(FareCalcInfoForEx fareCalcInfoForEx);

    /**
     * フライトサービスヘルスチェックの同期処理リクエスト。
     * 
     * @return ヘルスチェック情報
     */
    @CircuitBreaker(name = "calculateFareCircuitBreakerForHealth")
    @GetMapping("/actuator/health")
    HealthCheckStatusForEx actuactorHealth();

}
