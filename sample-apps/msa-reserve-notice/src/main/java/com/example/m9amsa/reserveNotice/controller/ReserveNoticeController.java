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
package com.example.m9amsa.reserveNotice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.m9amsa.reserveNotice.model.RssRoot;
import com.example.m9amsa.reserveNotice.service.ReserveNoticeService;

/**
 * 予約完了通知コントローラ。
 * 
 */
@RestController
@RequestMapping("/${info.url.root-path}/reserve-notice/rss-feed")
public class ReserveNoticeController {

    /**
     * 予約完了通知サービス。
     */
    @Autowired
    private ReserveNoticeService reserveNoticeService;

    /**
     * 予約情報を参照してユーザーにお知らせします。
     * 
     * @param emailId メールアドレス
     * @return RSS形式の通知フォーマット
     * @throws Exception 例外
     */
    @GetMapping
    public RssRoot getReserveCompleteNotice(@RequestParam String emailId) throws Exception {

        return reserveNoticeService.getReserveCompleteNotice(emailId);
    }
}
