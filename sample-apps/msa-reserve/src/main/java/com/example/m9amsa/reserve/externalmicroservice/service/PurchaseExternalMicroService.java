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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.m9amsa.reserve.externalmicroservice.model.HealthCheckStatusForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.PurchaseInfoForEx;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * 購入情報登録サービスのFeignクライアント。
 */
@FeignClient("purchase")
public interface PurchaseExternalMicroService {

    /**
     * 購入情報登録の同期処理リクエスト。
     * 
     * @param purchaseInfoForEx 予約情報。
     */
    @Retry(name = "purchaseRetry")
    @CircuitBreaker(name = "purchaseCircuitBreaker")
    @PostMapping("${info.url.root-path}/purchase/register")
    void registerPurchaseInfo(PurchaseInfoForEx purchaseInfoForEx);

    /**
     * 購入情報削除の同期処理リクエスト。
     * 
     * @param reserveId 予約Id。
     */
    @Retry(name = "purchaseRetry")
    @CircuitBreaker(name = "purchaseCircuitBreaker")
    @GetMapping("${info.url.root-path}/purchase/delete/{reserveId}")
    void deleteByReserveId(@PathVariable("reserveId") Long reserveId);

    /**
     * フライトサービスヘルスチェックの同期処理リクエスト。
     * 
     * @return ヘルスチェック情報
     */
    @CircuitBreaker(name = "purchaseCircuitBreakerForHealth")
    @GetMapping("/actuator/health")
    HealthCheckStatusForEx actuactorHealth();

}
