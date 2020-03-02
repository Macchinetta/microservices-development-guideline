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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.m9amsa.flightTicketFareCalculation.exception.HttpStatus404Exception;

/**
 * 業務処理結果に対する共通処理を行うAspectクラス。
 * 
 * <pre>
 * 正常終了時に共通処理を行います。
 * 
 * 戻り値の型が<code>List</code>型の場合、リストが0件の時は<code>HttpStatus404Exception</code>を発生させます。
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
     * @param list 戻り値の型。
     * @throws HttpStatus404Exception HttpStatusとして404を返却することを表す例外。
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

}
