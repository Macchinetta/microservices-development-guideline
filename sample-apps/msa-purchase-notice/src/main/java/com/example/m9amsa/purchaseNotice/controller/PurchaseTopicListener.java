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
package com.example.m9amsa.purchaseNotice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import com.example.m9amsa.purchaseNotice.model.topic.PurchaseTopic;
import com.example.m9amsa.purchaseNotice.model.topic.PurchaseTopicSink;
import com.example.m9amsa.purchaseNotice.service.PurchaseTopicService;

/**
 * ｛@code purchase-topic}のリスナークラス。
 * 
 */
@EnableBinding(PurchaseTopicSink.class)
public class PurchaseTopicListener {

    /**
     * PurchaseNoticeInfoテーブルを操作するサービスクラス。
     */
    @Autowired
    private PurchaseTopicService purchaseTopicService;

    /**
     * ｛@code purchase-topic}を参照します。
     * 
     * @param purchaseTopic 購入情報。｛@code purchase-topic}に対応するモデル。
     */
    @StreamListener(value = PurchaseTopicSink.INPUT)
    public void handlePurchaseTopic(PurchaseTopic purchaseTopic) {
        purchaseTopicService.registerPurchaseNoticeInfo(purchaseTopic);
    }
}
