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

package com.example.m9amsa.account.constant;

/**
 * 性別の列挙型。
 * 
 */
public enum Gender {

    /**
     * 女性のコード値。
     */
    Female,

    /**
     * 男性のコード値。
     */
    Male;

    /**
     * 性別コードを取得します。
     * 
     * @return 性別コード
     */
    public String getCode() {
        return name();
    }
}
