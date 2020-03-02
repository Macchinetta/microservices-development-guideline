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
package com.example.m9amsa.reserve.controller;

import java.util.List;

import javax.persistence.PersistenceException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

/**
 * 例外ハンドラー。
 * 
 */
@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

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
        ApiValidationErrorResponse error = new ApiValidationErrorResponse("Method argument not valid");
        error.addValidationErrorMessage(ex.getStackTrace());
        return super.handleExceptionInternal(ex, error, null, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * FeignException用の例外ハンドリング処理です。
     * 
     * <pre>
     * サービスがHTTPステータス500を返却するよう処理します。
     * </pre>
     * 
     * @param ex      FeignException。
     * @param request WebRequest。
     * @return HTTPレスポンス。
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleFeignException(FeignException ex, WebRequest request) {
        /*
         *  SAGA処理の実行
         *  あえてFeignExceptionの例外ハンドラを用意する必要はサンプルアプリケーションではありませんが
         *  同期連携ガイドの説明をする上で、便宜的に設定しています。
         */
        this.executeSaga(ex, request);

        /*
         * FeignExceptionは連携先サービスが何らかの例外を返した時に発生します。
         * ここでは、連携先から400系エラーが返ってきた場合も、予約サービスとしては500エラー
         * としてクライアントへ結果を返却しています。
         * 連携先から返ってくるステータスごとに予約サービスとしてクライアントに返す結果を
         * 分ける必要がある場合、ここで条件分岐させることになります。
         */
        ApiValidationErrorResponse error = new ApiValidationErrorResponse("Internal server error");
        error.addValidationErrorMessage(ex.getStackTrace());
        return super.handleExceptionInternal(ex, error, null, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * PersistenceException用の例外ハンドリング処理です。
     * 
     * <pre>
     * サービスがHTTPステータス500を返却するよう処理します。
     * </pre>
     * 
     * @param ex PersistenceException。
     * @param request WebRequest。
     * @return HTTPレスポンス。
     */
    @ExceptionHandler
    public ResponseEntity<Object> handlePersistenceException(PersistenceException ex, WebRequest request) {
        // SAGA処理の実行
        this.executeSaga(ex, request);

        ApiValidationErrorResponse error = new ApiValidationErrorResponse("Internal server error");
        error.addValidationErrorMessage(ex.getStackTrace());
        return super.handleExceptionInternal(ex, error, null, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * 全ての例外に対してSAGA処理のチェックを実行するため、内部エラー処理をオーバーライドします。
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        // SAGA処理の実行
        this.executeSaga(ex, request);

        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    /**
     * SAGA処理を実行します。
     * 
     * <pre>
     * WebRequestにSAGA処理が登録されている場合、登録順に実行します。
     * SAGA処理は補償トランザクションが必要な同期連携処理で例外が発生した場合に登録されます。
     * </pre>
     * 
     * @param ex 主処理中に発生した例外
     * @param request RequestAttributeにSAGA処理リストを保持します
     */
    private void executeSaga(Exception ex, WebRequest request) {
        @SuppressWarnings("unchecked")
        List<Runnable> compensations = (List<Runnable>) request.getAttribute("compensations",
                RequestAttributes.SCOPE_REQUEST);
        if (compensations == null) {
            return;
        }

        compensations.stream() //
                .forEach(c -> {
                    log.warn("補償トランザクションを開始します。", ex);
                    try {
                        c.run();
                    } catch (Exception e1) {
                        log.error("補償トランザクションを実行時に未知エラーが発生しました。", e1);
                        ex.addSuppressed(e1);
                    }
                    log.warn("補償トランザクションを実行しました。");
                });
    }
}
