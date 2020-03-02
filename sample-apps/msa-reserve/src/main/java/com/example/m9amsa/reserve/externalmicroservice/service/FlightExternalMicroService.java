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

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.m9amsa.reserve.externalmicroservice.model.HealthCheckStatusForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.ReserveVacantSeatForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.VacantSeatForEx;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * フライト空席確保サービスのFeignクライアント。
 */
@FeignClient("flight")
public interface FlightExternalMicroService {

    /**
     * フライト空席確保の同期処理リクエスト。
     * 
     * @param reservationInfo フライト空席確保情報。
     * @return フライト空席情報。
     */
    @Retry(name = "flightRetry")
    @CircuitBreaker(name = "flightCircuitBreaker")
    @PostMapping("${info.url.root-path}/flight/seat/reserve")
    VacantSeatForEx secureVacantSeat(ReserveVacantSeatForEx reservationInfo);

    /**
     * フライト空席取消の同期処理リクエスト。
     * 
     * @param reservationInfo フライト空席確保情報。
     */
    @Retry(name = "flightRetry")
    @CircuitBreaker(name = "flightCircuitBreaker")
    @PostMapping("${info.url.root-path}/flight/seat/cancel")
    void cancelReservedSeat(ReserveVacantSeatForEx reservationInfo);

    /**
     * フライトサービスヘルスチェックの同期処理リクエスト。
     * 
     * @return ヘルスチェック情報
     */
    @CircuitBreaker(name = "flightCircuitBreakerForHealth")
    @GetMapping("/actuator/health")
    HealthCheckStatusForEx actuactorHealth();

}
