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
package com.example.m9amsa.reserve.actuate;

import java.text.DecimalFormat;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.stereotype.Component;

import com.example.m9amsa.reserve.externalmicroservice.model.HealthCheckStatusForEx;
import com.example.m9amsa.reserve.externalmicroservice.service.CalculateFareExternalMicroService;
import com.example.m9amsa.reserve.externalmicroservice.service.FlightExternalMicroService;
import com.example.m9amsa.reserve.externalmicroservice.service.PurchaseExternalMicroService;

import feign.FeignException;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * メモリ・cpu使用率のヘルスチェック。
 */
@WebEndpoint(id = "ready")
@Component
public class CustomReadyIndicator {

    /**
     * 運賃計算サービスのFeignクライアント。
     */
    @Autowired
    private CalculateFareExternalMicroService calculateFareExternalMicroService;

    /**
     * フライトサービスのFeignクライアント。
     */
    @Autowired
    private FlightExternalMicroService flightExternalMicroService;

    /**
     * 購入サービスのFeignクライアント。
     */
    @Autowired
    private PurchaseExternalMicroService purchaseExternalMicroService;

    /**
     * メモリ使用率、cpu使用率を取得するクラス。
     */
    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * メモリ使用率の閾値。
     */
    @Value("${m9amsa.actuator.health.threshold.memory}")
    private double memoryThreshold;

    /**
     * cpu使用率の閾値。
     */
    @Value("${m9amsa.actuator.health.threshold.cpu}")
    private double cpuThreshold;

    /**
     * 小数桁数を設定するフォーマットクラス。
     */
    @Value(value = "0.00")
    private DecimalFormat decimalFormat;

    /**
     * メモリ・cpu使用率のヘルスチェックを行います。
     * 
     * <pre>
     * メモリ使用率、cpu使用率を取得します。
     * 両方も設定した閾値を超えない場合、upと返します。
     * いずれでも閾値を超えた場合、downと返します。
     * </pre>
     * 
     * @return Health ヘルスstatusを持っているクラス
     */
    @ReadOperation
    public Health ready() {
        Health.Builder builder = new Health.Builder();

        // for cpu and memory
        try {
            doHealthCheck(builder);
        } catch (Exception e) {
            builder.down(e);
        }

        // for Flight Ticket Fare Calculate
        try {
            doHealthCheckCalculateFare(builder);
        } catch (Exception e) {
            builder.down(e);
        }

        // for Flight
        try {
            doHealthCheckFlight(builder);
        } catch (Exception e) {
            builder.down(e);
        }

        // for Purchase
        try {
            doHealthCheckPurchase(builder);
        } catch (Exception e) {
            builder.down(e);
        }

        return builder.build();
    }

    private void doHealthCheck(Builder builder) throws Exception {

        double memoryUsage = sumValues("jvm.memory.used") / sumValues("jvm.memory.max");
        double cpuUsage = sumValues("system.cpu.usage");

        boolean memoryIsOk = (memoryUsage <= memoryThreshold);
        boolean cpuIsOk = (cpuUsage <= cpuThreshold);

        if (memoryIsOk && cpuIsOk) {
            builder.up()//
                    .withDetail("memory usage", "memory usage " + decimalFormat.format(memoryUsage) + "　is ok")//
                    .withDetail("cpu usage", "cpu usage " + cpuUsage + "　is ok");
        } else {
            builder.down();
            // memory使用率が閾値を超えた場合のディテール情報
            if (!memoryIsOk) {
                builder.withDetail("memory usage",
                        "memory usage " + decimalFormat.format(memoryUsage) + " is over " + memoryThreshold);
            }
            // cpu使用率が閾値を超えた場合のディテール情報
            if (!cpuIsOk) {
                builder.withDetail("cpu usage", "cpu usage " + cpuUsage + " is over " + cpuThreshold);
            }
        }

    }

    /**
     * ヘルスチェック。
     * 
     * <pre>
     * 運賃計算サービスのヘルスチェックの結果がUPの場合限り、予約サービスがUPです。
     * 運賃計算サービスのヘルスチェックの結果がdownの場合、予約サービスがdownです。
     * 同期連携失敗した場合、予約サービスがdownです。
     * </pre>
     * 
     * @param builder ヘルスのビルダー
     * @throws Exception 例外
     */
    private void doHealthCheckCalculateFare(Builder builder) throws Exception {

        try {

            HealthCheckStatusForEx healthCheckStatusForEx = calculateFareExternalMicroService.actuactorHealth();

            if (healthCheckStatusForEx.getStatus().equals("UP")) {
                builder.up().withDetail("Flight-Ticket-Fare-Calculation Service", "ok");
            } else {
                builder.down();
                builder.withDetail("Flight-Ticket-Fare-Calculation Service", "DOWN");
                builder.withException(new Exception("FlightTopic-Ticket-Fare-Calculation Service down"));
            }

        } catch (FeignException e) {
            builder.down(e);
        }
    }

    /**
     * ヘルスチェック。
     * 
     * <pre>
     * フライトサービスのヘルスチェックの結果がUPの場合限り、予約サービスがUPです。
     * フライトサービスのヘルスチェックの結果がdownの場合、予約サービスがdownです。
     * 同期連携失敗した場合、予約サービスがdownです。
     * </pre>
     * 
     * @param builder ヘルスのビルダー
     * @throws Exception 例外
     */
    private void doHealthCheckFlight(Builder builder) throws Exception {

        try {

            HealthCheckStatusForEx result = flightExternalMicroService.actuactorHealth();

            if (result.getStatus().equals("UP")) {
                builder.up().withDetail("Flight Service", "ok");
            } else {
                builder.down();
                builder.withDetail("Flight Service", "DOWN");
                builder.withException(new Exception("FlightTopic Service down"));
            }

        } catch (FeignException e) {
            builder.down(e);
        }
    }

    /**
     * ヘルスチェック。
     * 
     * <pre>
     * 購入サービスのヘルスチェックの結果がUPの場合限り、予約サービスがUPです。
     * 購入サービスのヘルスチェックの結果がdownの場合、予約サービスがdownです。
     * 同期連携失敗した場合、予約サービスがdownです。
     * </pre>
     * 
     * @param builder ヘルスのビルダー
     * @throws Exception 例外
     */
    private void doHealthCheckPurchase(Builder builder) throws Exception {

        try {

            HealthCheckStatusForEx result = purchaseExternalMicroService.actuactorHealth();

            if (result.getStatus().equals("UP")) {
                builder.up().withDetail("Purchase Service", "ok");
            } else {
                builder.down();
                builder.withDetail("Purchase Service", "DOWN");
                builder.withException(new Exception("Purchase Service down"));
            }

        } catch (FeignException e) {
            builder.down(e);
        }
    }

    private double sumValues(String key) {
        return meterRegistry.get(key).meters().stream()
                .flatMap(m -> StreamSupport.stream(m.measure().spliterator(), false)).filter(m -> m.getValue() > 0)
                .collect(Collectors.summarizingDouble(Measurement::getValue)).getSum();
    }

}
