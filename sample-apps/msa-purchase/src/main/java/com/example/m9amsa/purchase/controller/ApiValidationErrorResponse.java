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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * バリデーションチェック例外レスポンスクラス。
 * 
 */
@Getter
@RequiredArgsConstructor
public class ApiValidationErrorResponse implements Serializable {

    private static final long serialVersionUID = 159236649595934450L;

    /**
     * エラーメッセージ。
     */
    private final String message;

    /**
     * バリデーションチェック例外メッセージ内部クラス。
     */
    @Getter
    @RequiredArgsConstructor
    private static class ValidationErrorMessage implements Serializable {

        private static final long serialVersionUID = 3238148541348876412L;

        /**
         * 指定された実行ポイントを表すスタックトレース要素。
         */
        private final StackTraceElement[] stackTraceElements;
    }

    /**
     * バリデーションチェック例外メッセージリスト。
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ValidationErrorMessage> validationErrMessages = new ArrayList<>();

    /**
     * バリデーションチェック例外メッセージ追加。
     * 
     * @param stackTraceElements 発生したエラーのスタックトレース。
     */
    public void addValidationErrorMessage(StackTraceElement[] stackTraceElements) {
        validationErrMessages.add(new ValidationErrorMessage(stackTraceElements));
    }

}
