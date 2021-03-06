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
package com.example.m9amsa.reserveNotice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.resilience4j.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import io.github.resilience4j.retry.configure.RetryConfigurationProperties;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_RESERVE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
public class CircuitBreakerConfigTests {

    @Autowired
    private CircuitBreakerConfigurationProperties circuitBreakerConfigurationProperties;

    @Autowired
    private RetryConfigurationProperties retryConfigurationProperties;

    /**
     * application.yml 設定値取得テスト
     * 
     * <pre>
     * サーキットブレーカー
     * リトライ
     * </pre>
     */
    @Test
    public void testSettings() throws Exception {

        // サーキットブレーカー
        Map<String, CircuitBreakerConfigurationProperties.InstanceProperties> circuitBreakerBackends = circuitBreakerConfigurationProperties
                .getBackends();

        CircuitBreakerConfigurationProperties.InstanceProperties circuitBreakerInstanceProperties;
        String name;

        // default
        name = "default";
        assertNotNull("サーキットブレーカー(" + name + ")が存在すること。",
                circuitBreakerConfigurationProperties.getConfigs().containsKey(name));

        circuitBreakerInstanceProperties = circuitBreakerConfigurationProperties.getConfigs().get(name);
        assertEquals("「sliding-window-size = 1」であること", 1,
                circuitBreakerInstanceProperties.getSlidingWindowSize().intValue());
        assertEquals("「permitted-number-of-calls-in-half-open-state = 1」であること", 1,
                circuitBreakerInstanceProperties.getPermittedNumberOfCallsInHalfOpenState().intValue());
        assertEquals("「wait-duration-in-open-state  = 60000」であること", 60000,
                circuitBreakerInstanceProperties.getWaitDurationInOpenState().getSeconds() * 1000);
        assertEquals("「failure-rate-threshold = 100」であること", 100,
                circuitBreakerInstanceProperties.getFailureRateThreshold().intValue());
        assertTrue("「automatic-transition-from-open-to-half-open-enabled = true」であること",
                circuitBreakerInstanceProperties.getAutomaticTransitionFromOpenToHalfOpenEnabled());
        assertEquals(
                "「record-failure-predicate = com.example.m9amsa.reserveNotice.circuitbreaker.FeignClientExceptionPredicate」であること",
                "com.example.m9amsa.reserveNotice.circuitbreaker.FeignClientExceptionPredicate",
                circuitBreakerInstanceProperties.getRecordFailurePredicate().getName());
        assertTrue("「register-health-indicator = true」であること",
                circuitBreakerInstanceProperties.getRegisterHealthIndicator());

        // flightCircuitBreaker
        name = "flightCircuitBreaker";
        assertNotNull("サーキットブレーカー(" + name + ")が存在すること。", circuitBreakerBackends.get(name));

        circuitBreakerInstanceProperties = circuitBreakerBackends.get(name);
        assertEquals("「baseConfig = default」であること", "default", circuitBreakerInstanceProperties.getBaseConfig());

        // リトライ
        Map<String, RetryConfigurationProperties.InstanceProperties> retryBackends = retryConfigurationProperties
                .getBackends();
        RetryConfigurationProperties.InstanceProperties retryInstanceProperties;

        // default
        name = "default";
        assertTrue("リトライ(" + name + ")が存在すること。", retryConfigurationProperties.getConfigs().containsKey(name));

        retryInstanceProperties = retryConfigurationProperties.getConfigs().get(name);
        assertEquals("「max-retry-attempts = 3」であること", 3, retryInstanceProperties.getMaxRetryAttempts().intValue());
        assertEquals("「wait-duration = 500ms」であること", 500_000_000, retryInstanceProperties.getWaitDuration().getNano());
        assertTrue("「enable-exponential-backoff = true」であること", retryInstanceProperties.getEnableExponentialBackoff());
        assertEquals("「exponential-backoff-multiplier = 2」であること", 2,
                retryInstanceProperties.getExponentialBackoffMultiplier().intValue());
        assertEquals(
                "「retry-exception-predicate = com.example.m9amsa.reserveNotice.circuitbreaker.FeignClientExceptionPredicate」であること",
                "com.example.m9amsa.reserveNotice.circuitbreaker.FeignClientExceptionPredicate",
                retryInstanceProperties.getRetryExceptionPredicate().getName());

        // flightRetry
        name = "flightRetry";
        assertTrue("リトライ(" + name + ")が存在すること。", retryBackends.containsKey(name));

        retryInstanceProperties = retryBackends.get(name);
        assertEquals("「max-retry-attempts = 3」であること", "default", retryInstanceProperties.getBaseConfig());
    }
}
