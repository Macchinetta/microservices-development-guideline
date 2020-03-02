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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.m9amsa.purchaseNotice.model.RssRoot;
import com.example.m9amsa.purchaseNotice.service.PurchaseNoticeService;

/**
 * 購入通知コントローラークラス。
 * 
 * <pre>
 * 購入結果通知を行うマイクロサービスです。
 * URLのクエリパラメータ「emailId」に該当する代表搭乗者のメールアドレスに紐づく購入情報を通知します。
 *  -会員の場合：支払結果を通知します。
 *  -非会員の場合：支払期限を通知します。
 * 通知フォーマットはRSS形式です。
 * 該当する予約情報がないとき「item」タグが空のRSSを返します。
 * </pre>
 * 
 */
@RestController
@RequestMapping("/${info.url.root-path}/purchase-notice/rss-feed")
public class PurchaseNoticeController {

    /**
     * 購入通知サービス。
     */
    @Autowired
    private PurchaseNoticeService purchaseNoticeService;

    /**
     * 購入結果を通知します。
     * 
     * @param emailId メールアドレス
     * @return RSS形式の通知フォーマット
     * @throws Exception 例外
     */
    @GetMapping
    public RssRoot purchaseNotice(@RequestParam String emailId) throws Exception {

        return purchaseNoticeService.purchaseNotice(emailId);

    }
}
