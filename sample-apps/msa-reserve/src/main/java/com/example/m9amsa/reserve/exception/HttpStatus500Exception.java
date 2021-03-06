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
package com.example.m9amsa.reserve.exception;

/**
 * HttpStatusとして500を返却することを表す例外。
 * 
 */
public class HttpStatus500Exception extends RuntimeException {

    private static final long serialVersionUID = -6425384449677893546L;

    public HttpStatus500Exception() {
        super();
    }

    public HttpStatus500Exception(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public HttpStatus500Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpStatus500Exception(String message) {
        super(message);
    }

    public HttpStatus500Exception(Throwable cause) {
        super(cause);
    }
}