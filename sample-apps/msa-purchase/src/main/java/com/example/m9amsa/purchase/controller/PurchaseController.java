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

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.m9amsa.purchase.entity.Purchase;
import com.example.m9amsa.purchase.exception.HttpStatus401Exception;
import com.example.m9amsa.purchase.model.ReserveInfo;
import com.example.m9amsa.purchase.model.topic.PurchaseTopic;
import com.example.m9amsa.purchase.model.topic.PurchaseTopicSource;
import com.example.m9amsa.purchase.service.DeletePurchaseService;
import com.example.m9amsa.purchase.service.FindPurchaseService;
import com.example.m9amsa.purchase.service.RegisterPurchaseService;

/**
 * 購入コントローラ。
 * 
 * <pre>
 * 購入情報の登録、購入状況照会を行うマイクロサービスです。
 * </pre>
 * 
 */
@RestController
@RequestMapping("/${info.url.root-path}/purchase")
@Validated
@EnableBinding(PurchaseTopicSource.class)
public class PurchaseController {

    /**
     * 購入情報登録サービス。
     */
    @Autowired
    private RegisterPurchaseService registerPurchaseService;

    /**
     * 購入状況照会サービス。
     */
    @Autowired
    private FindPurchaseService findPurchaseService;

    /**
     * 購入状況削除サービス。
     */
    @Autowired
    private DeletePurchaseService deletePurchaseService;

    /**
     * 購入トピック。
     */
    @Autowired
    private PurchaseTopicSource purchaseTopicSource;

    /**
     * モデル変換ヘルパー
     */
    @Autowired
    private PurchaseHelper purchaseHelper;

    /**
     * 
     * 購入情報登録を行います。
     * 
     * @param reserveInfo 予約情報。
     */
    @PostMapping("/register")
    public void registerPurchase(@RequestBody @Valid ReserveInfo reserveInfo) {
        // 購入情報を登録し、購入通知サービスへ連携する情報を生成
        PurchaseTopic purchaseTopic = purchaseHelper.createPurchaseTopic(reserveInfo,
                registerPurchaseService.registerPurchase(reserveInfo));

        // 購入トピックに通知
        purchaseTopicSource.output().send(MessageBuilder.withPayload(purchaseTopic).build());
    }

    /**
     * 
     * 購入情報削除を行います。
     * 
     * @param reserveId 予約Id。
     */
    @GetMapping("/delete/{reserveId}")
    public void deleteByReserveId(@PathVariable Long reserveId) {
        deletePurchaseService.deleteByReserveId(reserveId);
    }

    /**
     * 購入状況照会を行います。
     * 
     * @param authentication 認証情報
     * @param reserveId      予約Id。
     * @return 購入情報。
     */

    @GetMapping("/find/{reserveId}")
    public Purchase findPurchase(OAuth2Authentication authentication, @PathVariable Long reserveId) {
        Optional<Purchase> purchase = findPurchaseService.findPurchase(reserveId);

        // 認証済み会員IDと購入時会員IDが違う場合、認証エラーとします。
        return purchase.filter(p -> p.getPurchaseMemberId().equals(Long.valueOf(authentication.getName())))
                .orElseThrow(HttpStatus401Exception::new);
    }

}