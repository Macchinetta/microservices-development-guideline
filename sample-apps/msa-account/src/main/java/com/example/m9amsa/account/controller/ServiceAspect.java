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
package com.example.m9amsa.account.controller;

import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.m9amsa.account.entity.Member;
import com.example.m9amsa.account.exception.HttpStatus404Exception;

/**
 * 業務処理結果に対する共通処理を行うAspectクラス。
 * 
 * <pre>
 * 正常終了時に共通処理を行います。
 * 
 * 戻り値の型がMember型の場合、Memberが取得できていない場合はHttpStatus404Exceptionを発生させます。
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
     * 戻り値が会員情報(<code>Optional&#60;Member&#62;</code>)の場合に実行される後処理です。
     * 会員情報が空の時はMemberNotFoundExceptionを発生させます。
     * </pre>
     * 
     * @param jp     JoinPoint。
     * @param sa     <code>@Service</code>アノテーション。
     * @param member 会員情報。
     * @throws HttpStatus404Exception 会員情報が空の場合、HttpStatus404Exceptionを発生させます。
     */
    @AfterReturning(pointcut = "@within(sa)", returning = "member")
    public void correctReturn(JoinPoint jp, Service sa, Optional<Member> member) throws HttpStatus404Exception {
        if (member.isEmpty()) {
            // Memberを取得できない場合、404業務例外を返却
            throw new HttpStatus404Exception();
        }
    }

}
