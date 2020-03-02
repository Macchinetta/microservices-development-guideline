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
package com.example.m9amsa.account.entity;

import java.time.Clock;

import org.springframework.stereotype.Component;

/**
 * 現在日付取得用の基準Clockを定義するクラス。
 * 
 */
@Component
public class BaseClock {

    /**
     * {@link Clock#systemDefaultZone()}をデフォルトClockとして設定します。
     */
    private Clock defaultClock = Clock.systemDefaultZone();

    /**
     * defaultClockを返却します。
     * 
     * @return {@link BaseClock#defaultClock}
     */
    public Clock systemDefaultZone() {
        return defaultClock;
    }
}
