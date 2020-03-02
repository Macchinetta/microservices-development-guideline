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

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flightTicketFareCalculation.exception.HttpStatus404Exception;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_CALCULATE_FARE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=localhost" })
public class ServiceAspectTest {

    @Autowired
    private ServiceAspect serviceAspect;

    @MockBean
    private JoinPoint joinPoint;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(joinPoint);
    }

    /**
     * correctRetrurn()メソッドのテスト。
     * 
     */
    @Test
    public void testCorrectReturn() {
        MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        // ターゲットメソッドの戻り値がList以外の場合
        Throwable e = catchThrowable(() -> {
            doReturn(String.class).when(methodSignature).getReturnType();
            doReturn(methodSignature).when(joinPoint).getSignature();

            serviceAspect.correctReturn(joinPoint, (Service) null, (List<?>) null);
        });

        if (e != null)
            e.printStackTrace();
        assertNull("予期しない例外が発生：", e);

        // ターゲットメソッドの戻り値がListで、emptyではない場合
        reset(methodSignature);
        e = catchThrowable(() -> {
            List<Integer> list = Arrays.asList(1, 2, 3);

            doReturn(list.getClass()).when(methodSignature).getReturnType();
            doReturn(methodSignature).when(joinPoint).getSignature();

            serviceAspect.correctReturn(joinPoint, (Service) null, list);
        });

        if (e != null)
            e.printStackTrace();
        assertNull("予期しない例外が発生：", e);

        // ターゲットメソッドの戻り値がListで、emptyの場合
        HttpStatus404Exception e404 = catchThrowableOfType(() -> {
            serviceAspect.correctReturn(joinPoint, (Service) null, Collections.EMPTY_LIST);
        }, HttpStatus404Exception.class);

        assertNotNull("HttpStatus404Exception例外が発生するはず：", e404);

    }
}
