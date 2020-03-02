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

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.example.m9amsa.account.model.MemberUpdateInfo;

/**
 * {@link MemberUpdateInfo}のバリデータ。
 * 
 */
@Component
public class MemberUpdateInfoValidator implements Validator {

    /**
     * @param clazz クラス
     * @return {@code MemberUpdateInfo.class.isAssignableFrom(clazz)}
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return MemberUpdateInfo.class.isAssignableFrom(clazz);
    }

    /**
     * バリデーションチェック。
     * 
     * @param target 目標オブジェクト
     * @param errors 例外
     */
    @Override
    public void validate(Object target, Errors errors) {

        if (errors.hasFieldErrors("password")) {
            return;
        }

        MemberUpdateInfo memberUpdateInfo = (MemberUpdateInfo) target;
        if (memberUpdateInfo.getPassword() != null
                && !memberUpdateInfo.getPassword().equals(memberUpdateInfo.getReEnterPassword())) {
            // エラーメッセージコードは設定していないのでここで指定したデフォルトメッセージが入る
            errors.rejectValue("password", "validator.memberUpdateInfo.reEnterPassword", "パスワードと再入力パスワードの不一致");
        }
    }

}
