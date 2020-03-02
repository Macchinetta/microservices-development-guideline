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
package com.example.m9amsa.purchase.service;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.purchase.entity.Payment;
import com.example.m9amsa.purchase.entity.Purchase;
import com.example.m9amsa.purchase.entity.PurchaseRepository;
import com.example.m9amsa.purchase.model.MemberInfo;
import com.example.m9amsa.purchase.model.PassengerInfo;
import com.example.m9amsa.purchase.model.ReserveInfo;
import com.example.m9amsa.purchase.model.topic.CardTopic;
import com.example.m9amsa.purchase.model.topic.MemberTopic;
import com.example.m9amsa.purchase.model.topic.PassengerTopic;
import com.example.m9amsa.purchase.model.topic.PurchaseTopic;

/**
 * 購入情報登録サービス。
 * 
 */
@Service
public class RegisterPurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    /**
     * 購入情報登録を行います。
     * 
     * @param reserveInfo 予約情報。
     * @return 購入情報。
     */
    @Transactional
    public Purchase registerPurchase(ReserveInfo reserveInfo) {
        Purchase purchase = new Purchase();
        BeanUtils.copyProperties(reserveInfo, purchase);
        purchase.setPassengerCount(reserveInfo.getPassengers().size());

        // 会員情報が設定されている場合
        reserveInfo.getPurchaseMember().ifPresent(m -> {
            purchase.setPurchaseMemberId(m.getMemberId());
            m.getCard().ifPresent(c -> { // カード情報が存在する場合、自動決済を行います
                Payment payment = new Payment();
                BeanUtils.copyProperties(m, payment);
                BeanUtils.copyProperties(c, payment);
                payment.setValidTillYear(c.getValidTillYear().toString());
                payment.setValidTillMonth(c.getValidTillMonth().toString());
                payment.setPurchase(purchase);
                payment.setFare(reserveInfo.getFare());
                purchase.setPayment(payment);
            });
        });

        // 購入情報を登録、返却します
        return purchaseRepository.saveAndFlush(purchase);
    }

}
