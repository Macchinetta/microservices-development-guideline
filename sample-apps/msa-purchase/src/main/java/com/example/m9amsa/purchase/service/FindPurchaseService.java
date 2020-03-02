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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.purchase.entity.Purchase;
import com.example.m9amsa.purchase.entity.PurchaseRepository;

/**
 * 購入状況照会サービス。
 * 
 */
@Service
public class FindPurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    /**
     * 購入状況照会を行います。
     * 
     * @param reserveId 予約Id。
     * @return 購入情報。
     */
    @Transactional(readOnly = true)
    public Optional<Purchase> findPurchase(Long reserveId) {
        Example<Purchase> condition = Example.of(Purchase.builder().reserveId(reserveId).build());
        return purchaseRepository.findOne(condition);
    }

}
