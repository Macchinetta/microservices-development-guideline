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
package com.example.m9amsa.purchaseNotice.service;

import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.purchaseNotice.entity.Passenger;
import com.example.m9amsa.purchaseNotice.entity.Purchase;
import com.example.m9amsa.purchaseNotice.entity.PurchaseRepository;
import com.example.m9amsa.purchaseNotice.model.topic.PassengerTopic;
import com.example.m9amsa.purchaseNotice.model.topic.PurchaseTopic;

/**
 * PurchaseNoticeInfoテーブルを操作するサービスクラス。
 * 
 */
@Service
public class PurchaseTopicService {

    /**
     * 購入情報リポジトリ。
     */
    @Autowired
    private PurchaseRepository purchaseRepository;

    /**
     * 購入通知情報を登録します。
     * 
     * <pre>
     * ｛@code purchase-topic}から受け取った購入情報PurchaseInfoを購入通知情報PurchaseNoticeInfoに変換して、
     *   purchaseNoticeInfoテーブルに登録します。
     * </pre>
     *
     * @param purchaseTopic トピックからの購入情報。
     */
    @Transactional
    public void registerPurchaseNoticeInfo(PurchaseTopic purchaseTopic) {

        Purchase purchase = new Purchase();
        BeanUtils.copyProperties(purchaseTopic, purchase, "passengers");
        purchase.setPassengers(
                purchaseTopic.getPassengers().stream().map(this::toPassenger).collect(Collectors.toList()));
        // PurchaseはemailIdの属性があり、ここでセットします。
        purchase.setEmailId(purchaseTopic.getPurchaseMember().get().getEmailId());
        purchaseRepository.saveAndFlush(purchase);

    }

    private Passenger toPassenger(PassengerTopic passengerTopic) {
        Passenger passenger = new Passenger();
        BeanUtils.copyProperties(passengerTopic, passenger);
        return passenger;
    }
}
