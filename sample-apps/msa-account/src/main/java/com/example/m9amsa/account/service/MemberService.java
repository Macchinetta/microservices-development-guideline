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
package com.example.m9amsa.account.service;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.account.entity.CardRepository;
import com.example.m9amsa.account.entity.Member;
import com.example.m9amsa.account.entity.MemberRepository;
import com.example.m9amsa.account.model.MemberUpdateInfo;

/**
 * 会員情報サービス。
 * 
 */
@Service
public class MemberService {

    /**
     * 会員リポジトリ。
     */
    @Autowired
    private MemberRepository memberRepository;

    /**
     * カードリポジトリ。
     */
    @Autowired
    private CardRepository cardRepository;

    /**
     * パスワード更新サービス。
     */
    @Autowired
    private UpdatePasswordService updatePasswordService;

    /**
     * 会員情報取得サービス。
     * 
     * @param memberId 会員Id。
     * @return {@code Optional<Member>}
     *         会員Idに該当する会員情報。会員Idに該当する会員情報が存在しない場合、{@code Optional.empty()}を返却します。
     */
    @Transactional(readOnly = true)
    public Optional<Member> getMember(Long memberId) {
        return memberRepository.findById(memberId);
    }

    /**
     * カード会員情報変更サービス。
     * 
     * @param memberId         会員Id。
     * @param memberUpdateInfo 会員更新情報。
     */
    @Transactional
    public void updateMember(Long memberId, MemberUpdateInfo memberUpdateInfo) {
        // 認証できているユーザの会員情報が無い場合はデータ異常とみなします
        Member currentMember = memberRepository.findById(memberId).orElseThrow();

        // 更新パスワードが設定されている場合のみパスワード変更処理を行います
        if (null != memberUpdateInfo.getReEnterPassword() && !memberUpdateInfo.getReEnterPassword().isEmpty()) {
            updatePasswordService.updatePassword(memberId, memberUpdateInfo.getPassword());
        }

        Member member = new Member();
        BeanUtils.copyProperties(memberUpdateInfo, member);
        member.setMemberId(memberId);
        memberRepository.save(member);

        // @OneToOneのリレーションが切れた古いカードを削除します
        if (!currentMember.getCard().equals(memberUpdateInfo.getCard())) {
            cardRepository.delete(currentMember.getCard());
        }

    }

    /**
     * カード会員登録サービス。
     * 
     * @param member カード会員情報。
     * @return 登録された会員情報。
     */
    @Transactional
    public Member createMember(Member member) {
        return memberRepository.save(member);
    }
}
