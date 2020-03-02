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
package com.example.m9amsa.account.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.m9amsa.account.entity.Member;
import com.example.m9amsa.account.model.MemberUpdateInfo;
import com.example.m9amsa.account.service.MemberService;

/**
 * 会員情報コントローラ。
 * 
 */
@RestController
@RequestMapping("/${info.url.root-path}/account/member")
@Validated
public class MemberController {

    /**
     * 会員情報バリデータ。
     */
    @Autowired
    private MemberUpdateInfoValidator memberUpdateInfoValidator;

    /**
     * 会員サービス。
     */
    @Autowired
    private MemberService memberService;

    /**
     * 会員情報バリデータの設定
     * 
     * @param binder リクエストパラメータのバインド処理クラス
     */
    @InitBinder("memberUpdateInfo")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(memberUpdateInfoValidator);
    }

    /**
     * 認証情報に含まれる会員IDに紐づく会員情報を取得します。
     * 
     * @param authentication OAuth2認証情報。
     * @return 会員情報。
     */
    @GetMapping
    public Member getMember(OAuth2Authentication authentication) {
        return memberService.getMember(Long.valueOf(authentication.getName())).get();
    }

    /**
     * カード会員情報更新。
     * 
     * @param authentication   OAuth2認証情報。
     * @param memberUpdateInfo 会員更新情報。
     */
    @PutMapping
    public void updateMember(OAuth2Authentication authentication,
            @RequestBody @Valid MemberUpdateInfo memberUpdateInfo) {
        memberService.updateMember(Long.valueOf(authentication.getName()), memberUpdateInfo);
    }
}
