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
package com.example.m9amsa.purchaseNotice.circuitbreaker;

import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.http.HttpStatus;

import feign.FeignException;

/**
 * FeignClient呼び出し用のpredicateクラス。
 */
public class FeignClientExceptionPredicate implements Predicate<Throwable> {

    @Override
    public boolean test(Throwable throwable) {
        if (throwable instanceof FeignException) {
            FeignException feignException = (FeignException) throwable;
            Optional<HttpStatus> httpStatus = Optional.ofNullable(HttpStatus.resolve(feignException.status()));

            // 400系コードをエラーとしません
            if (httpStatus.isPresent() && httpStatus.get().is4xxClientError()) {
                return false;
            }
        }
        return true;
    }
}
