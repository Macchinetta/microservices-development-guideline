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
package com.example.m9amsa.purchase.controller;

import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.m9amsa.purchase.entity.Purchase;
import com.example.m9amsa.purchase.exception.HttpStatus404Exception;

/**
 * 業務処理結果に対する共通処理を行うAspectクラス。
 * 
 * <pre>
 * 正常終了時に共通処理を行います。
 * 
 * 戻り値の型がPurchase型の場合: Purchaseが取得できていない場合はHttpStatus404Exceptionを発生させます。
 * </pre>
 * 
 * 
 */
@Aspect
@Component
public class ServiceAspect {

    /**
     * 正常終了時の共通処理。
     * 
     * <pre>
     * 戻り値の型が<code>Optional&#60;Purchase&#62;</code>型の場合、Purchaseオブジェクトが空の時はHttpStatus404Exceptionを発生させます。
     * </pre>
     * 
     * @param jp       JoinPoint。
     * @param sa       <code>@Service</code>アノテーション。
     * @param purchase 戻り値の型。
     * @throws HttpStatus404Exception Purchaseが空の場合、HttpStatus404Exceptionを発生させます。
     */
    @AfterReturning(pointcut = "@within(sa)", returning = "purchase")
    public void correctReturn(JoinPoint jp, Service sa, Optional<Purchase> purchase) throws HttpStatus404Exception {
        if (purchase.isEmpty()) {
            // Purchaseを取得できない場合、404を返却
            throw new HttpStatus404Exception();
        }
    }

}
