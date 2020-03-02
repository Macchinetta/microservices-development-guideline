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

import java.util.NoSuchElementException;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.m9amsa.flight.exception.BusinessException;
import com.example.m9amsa.flight.exception.HttpStatus404Exception;

/**
 * 例外ハンドラー。
 * 
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * HttpStatus404Exception用の例外ハンドリング処理です。
     * 
     * <pre>
     * サービスがHTTPステータス404を返却するよう処理します。
     * </pre>
     * 
     * @param ex      HttpStatus404Exception。
     * @param request WebRequest。
     * @return HTTPレスポンス。
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleHttpStatus404Exception(HttpStatus404Exception ex, WebRequest request) {
        ApiValidationErrorResponse error = new ApiValidationErrorResponse("Not Found");
        error.addValidationErrorMessage(ex.getStackTrace());
        return super.handleExceptionInternal(ex, error, null, HttpStatus.NOT_FOUND, request);
    }

    /**
     * BusinessException用の例外ハンドリング処理です。
     * 
     * <pre>
     * サービスがHTTPステータス412を返却するよう処理します。
     * </pre>
     * 
     * @param ex      HttpStatus412Exception。
     * @param request WebRequest。
     * @return HTTPレスポンス。
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleBusinessException(BusinessException ex, WebRequest request) {
        ApiValidationErrorResponse error = new ApiValidationErrorResponse("Precondition Failed");
        error.addValidationErrorMessage(ex.getStackTrace());
        return super.handleExceptionInternal(ex, error, null, HttpStatus.PRECONDITION_FAILED, request);
    }

    /**
     * NoSuchElementException用の例外ハンドリング処理です。
     * 
     * <pre>
     *サービスがHTTPステータス500を返却するよう処理します。
     * </pre>
     *
     * @param ex      NoSuchElementException。
     * @param request WebRequest。
     * @return HTTPレスポンス。
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException ex, WebRequest request) {
        ApiValidationErrorResponse error = new ApiValidationErrorResponse("Data access error");
        error.addValidationErrorMessage(ex.getStackTrace());
        return super.handleExceptionInternal(ex, error, null, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * ConstraintViolationException用の例外ハンドリング処理です。
     * 
     * <pre>
     * サービスがHTTPステータス400を返却するよう処理します。
     * </pre>
     * 
     * @param ex      ConstraintViolationException。
     * @param request WebRequest。
     * @return HTTPレスポンス。
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex,
            WebRequest request) {
        ApiValidationErrorResponse error = new ApiValidationErrorResponse("Bad Request");
        error.addValidationErrorMessage(ex.getStackTrace());
        return super.handleExceptionInternal(ex, error, null, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * MethodArgumentNotValidException用の例外ハンドリング処理です。
     * 
     * <pre>
     * サービスがHTTPステータス400を返却するよう処理します。
     * </pre>
     * 
     * @param ex      MethodArgumentNotValidException。
     * @param headers HttpHeaders。
     * @param status  HttpStatus。
     * @param request WebRequest。
     * @return HTTPレスポンス。
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        return super.handleExceptionInternal(ex, ex.getBindingResult().getFieldErrors(), null, HttpStatus.BAD_REQUEST,
                request);
    }

    /**
     * 何らかの実行時例外用の例外ハンドリング処理です。
     * 
     * <pre>
     *サービスがHTTPステータス500を返却するよう処理します。
     * </pre>
     *
     * @param ex      NoSuchElementException。
     * @param request WebRequest。
     * @return HTTPレスポンス。
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        ApiValidationErrorResponse error = new ApiValidationErrorResponse("Unexpected exception");
        error.addValidationErrorMessage(ex.getStackTrace());
        return super.handleExceptionInternal(ex, error, null, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
