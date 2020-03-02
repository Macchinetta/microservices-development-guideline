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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import com.example.m9amsa.purchase.exception.HttpStatus401Exception;
import com.example.m9amsa.purchase.exception.HttpStatus404Exception;

/**
 * ApiExceptionHandlerのテストクラス。
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_PURCHASE=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class ApiExceptionHandlerTest {

    @Autowired
    private ApiExceptionHandler apiExceptionHandler;

    @MockBean
    private WebRequest request;

    @Test
    public void testHandleHttpStatus401Exception() {
        ResponseEntity<Object> actual = apiExceptionHandler.handleHttpStatus401Exception(new HttpStatus401Exception(),
                request);

        assertThat("Bodyのインスタンスが正しいこと", actual.getBody(), instanceOf(ApiValidationErrorResponse.class));
        ApiValidationErrorResponse responce = (ApiValidationErrorResponse) actual.getBody();
        assertThat("Bodyのメッセージが正しいこと", responce.getMessage(), equalTo("Unauthorized"));
        assertTrue("Headersは空であること", actual.getHeaders().isEmpty());
        assertThat("HttpStatusが正しいこと", actual.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testHandleHttpStatus404Exception() {
        ResponseEntity<Object> actual = apiExceptionHandler.handleHttpStatus404Exception(new HttpStatus404Exception(),
                request);

        assertThat("Bodyのインスタンスが正しいこと", actual.getBody(), instanceOf(ApiValidationErrorResponse.class));
        ApiValidationErrorResponse responce = (ApiValidationErrorResponse) actual.getBody();
        assertThat("Bodyのメッセージが正しいこと", responce.getMessage(), equalTo("Bad request"));
        assertTrue("Headersは空であること", actual.getHeaders().isEmpty());
        assertThat("HttpStatusが正しいこと", actual.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void testHandleRuntimeException() {
        ResponseEntity<Object> actual = apiExceptionHandler.handleRuntimeException(new RuntimeException("test"),
                request);

        assertThat("Bodyのインスタンスが正しいこと", actual.getBody(), instanceOf(ApiValidationErrorResponse.class));
        ApiValidationErrorResponse responce = (ApiValidationErrorResponse) actual.getBody();
        assertThat("Bodyのメッセージが正しいこと", responce.getMessage(), equalTo("Unexpected exception"));
        assertTrue("Headersは空であること", actual.getHeaders().isEmpty());
        assertThat("HttpStatusが正しいこと", actual.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void testHandleMethodArgumentNotValid() {
        List<FieldError> errors = Arrays.asList(new FieldError("dummy", "dummy", "dummy"));
        BindingResult binding = Mockito.mock(BindingResult.class);
        doReturn(errors).when(binding).getFieldErrors();

        MethodArgumentNotValidException expNotValid = Mockito.mock(MethodArgumentNotValidException.class);
        doReturn(binding).when(expNotValid).getBindingResult();

        ResponseEntity<Object> actual = apiExceptionHandler.handleMethodArgumentNotValid( //
                expNotValid, //
                HttpHeaders.EMPTY, //
                HttpStatus.BAD_REQUEST, //
                request);

        assertThat("Bodyのインスタンスが正しいこと", actual.getBody(), equalTo(errors));
        assertTrue("Headersは空であること", actual.getHeaders().isEmpty());
        assertThat("HttpStatusが正しいこと", actual.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }
}
