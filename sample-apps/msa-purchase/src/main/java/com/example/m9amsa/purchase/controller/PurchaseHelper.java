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

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.example.m9amsa.purchase.entity.Payment;
import com.example.m9amsa.purchase.entity.Purchase;
import com.example.m9amsa.purchase.model.MemberInfo;
import com.example.m9amsa.purchase.model.PassengerInfo;
import com.example.m9amsa.purchase.model.ReserveInfo;
import com.example.m9amsa.purchase.model.topic.CardTopic;
import com.example.m9amsa.purchase.model.topic.MemberTopic;
import com.example.m9amsa.purchase.model.topic.PassengerTopic;
import com.example.m9amsa.purchase.model.topic.PurchaseTopic;

@Component
public class PurchaseHelper {

    /**
     * 予約情報（リクエストパラメータ）とServiceが登録した購入情報から購入通知サービスへ連携する情報を生成します。
     * 
     * @param reserveInfo 予約情報（リクエストパラメータ）
     * @param purchase 購入情報（登録した購入情報）
     * @return 購入通知サービスへ連携する購入情報
     */
    public PurchaseTopic createPurchaseTopic(ReserveInfo reserveInfo, Purchase purchase) {
        // 購入情報を作成します
        PurchaseTopic purchaseTopic = new PurchaseTopic();
        BeanUtils.copyProperties(purchase, purchaseTopic, "passengers");
        purchaseTopic.setPassengers(
                reserveInfo.getPassengers().stream().map(this::toPassengerTopic).collect(Collectors.toList()));
        reserveInfo.getPurchaseMember().map(MemberInfo::getCard)
                .ifPresent(card -> BeanUtils.copyProperties(card, purchaseTopic));
        Optional.ofNullable(purchase.getPayment()).ifPresent(p -> {
            BeanUtils.copyProperties(p, purchaseTopic);
        });


        purchaseTopic.setPurchaseMember(reserveInfo.getPurchaseMember().map(m -> {
            MemberTopic memberTopic = new MemberTopic();
            BeanUtils.copyProperties(m, memberTopic, "card");
            memberTopic.setCard(m.getCard().map(card -> {
                CardTopic cardTopic = new CardTopic();
                BeanUtils.copyProperties(card, cardTopic);
                return cardTopic;
            }));
            return memberTopic;
        }));

        return purchaseTopic;
    }

    private PassengerTopic toPassengerTopic(PassengerInfo passengerInfo) {
        PassengerTopic passengerTopic = new PassengerTopic();
        BeanUtils.copyProperties(passengerInfo, passengerTopic);
        return passengerTopic;
    }

}
