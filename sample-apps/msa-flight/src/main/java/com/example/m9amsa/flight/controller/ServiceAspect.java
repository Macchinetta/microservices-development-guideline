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
import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.m9amsa.flight.entity.Airport;
import com.example.m9amsa.flight.exception.HttpStatus404Exception;

/**
 * 業務処理結果に対する共通処理を行うAspectクラス。
 * 
 * <pre>
 * 正常終了時に共通処理を行います。
 * 
 * 戻り値の型がList型の場合:    リストが0件の時はHttpStatus404Exceptionを発生させます。
 * 戻り値の型がAirport型の場合: Airportが取得できていない場合はHttpStatus404Exceptionを発生させます。
 * </pre>
 * 
 */
@Aspect
@Component
public class ServiceAspect {

    /**
     * 正常終了時の共通処理。
     * 
     * <pre>
     * 戻り値の型がList型の場合、リストが0件の時はHttpStatus404Exceptionを発生させます。
     * </pre>
     * 
     * @param jp   JoinPoint。
     * @param sa   <code>@Service</code>アノテーション。
     * @param list List&#60;?&#62;。
     * @throws HttpStatus404Exception HTTP status 404 の例外。
     */
    @AfterReturning(pointcut = "@within(sa)", returning = "list")
    public void correctReturn(JoinPoint jp, Service sa, List<?> list) throws HttpStatus404Exception {
        // 対象メソッドの戻り値型をチェック
        Signature signature = jp.getSignature();
        Class<?> returnType = ((MethodSignature) signature).getReturnType();
        if (!List.class.isAssignableFrom(returnType)) {
            return; // List型以外は何もしません
        }

        if (list.isEmpty()) {
            // リストが空の場合、404を返却
            throw new HttpStatus404Exception();
        }
    }

    /**
     * 正常終了時の共通処理。
     * 
     * <pre>
     * 戻り値が空港情報(<code>Optional&#60;Airport&#62;</code>)の場合に実行される後処理です。
     * 空港情報が空の時はHttpStatus404Exceptionを発生させます。
     * </pre>
     * 
     * @param jp      JoinPoint。
     * @param sa      <code>@Service</code>アノテーション。
     * @param airport 空港情報。
     * @throws HttpStatus404Exception 空港情報が空の場合、HttpStatus404Exceptionを発生させます。
     */
    @AfterReturning(pointcut = "@within(sa)", returning = "airport")
    public void correctReturnAirport(JoinPoint jp, Service sa, Optional<Airport> airport)
            throws HttpStatus404Exception {
        Signature signature = jp.getSignature();
        Class<?> returnType = ((MethodSignature) signature).getReturnType();
        if (!Optional.class.isAssignableFrom(returnType)) {
            return; // Optional型以外は何もしない
        }
        if (airport.isEmpty()) {
            // Airportを取得できない場合、404を返却
            throw new HttpStatus404Exception();
        }
    }

}
