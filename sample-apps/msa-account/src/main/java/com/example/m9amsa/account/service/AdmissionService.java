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

import javax.persistence.EntityManager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.account.entity.Account;
import com.example.m9amsa.account.entity.AccountRepository;
import com.example.m9amsa.account.entity.Authorities;
import com.example.m9amsa.account.entity.Card;
import com.example.m9amsa.account.entity.Member;
import com.example.m9amsa.account.entity.Password;
import com.example.m9amsa.account.model.AdmissionMemberInfo;
import com.example.m9amsa.account.model.CardInfo;
import com.example.m9amsa.account.model.MemberIdInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * 入会サービス。
 * 
 */
@Service
@Slf4j
public class AdmissionService {

    /**
     * アカウントリポジトリ。
     */
    @Autowired
    private AccountRepository accountRepository;

    /**
     * 会員情報サービス。
     */
    @Autowired
    private MemberService memberService;

    /**
     * パスワードエンコード。
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * エンティティマネジャー。
     */
    @Autowired
    private EntityManager entityManager;

    /**
     * 新規会員を登録します。
     * 
     * <pre>
     * - アカウント情報の登録
     *   入力されたパスワードと新規に生成した会員IDを登録します。
     * - 会員情報の登録
     *   入力された会員情報を登録します。
     * </pre>
     * 
     * @param admissionMemberInfo 新規会員の登録情報。
     * @return 会員ID。
     */
    @Transactional
    public MemberIdInfo createMemberAccount(AdmissionMemberInfo admissionMemberInfo) {

        // 認証サービス: アカウント登録
        MemberIdInfo memberIdInfo = this.registerAccount(admissionMemberInfo.getPassword());
        log.info("AdmissionService.admission. member Id:{}", memberIdInfo);

        // 会員サービス: 会員登録
        Member member = new Member();
        BeanUtils.copyProperties(admissionMemberInfo, member);

        // カード情報をコピーします
        CardInfo cardInfo = admissionMemberInfo.getCard();
        Card card = new Card();
        member.setCard(card);
        BeanUtils.copyProperties(cardInfo, card);
        card.setValidTillYear(String.format("%02d", cardInfo.getValidTillYear()));
        card.setValidTillMonth(String.format("%02d", cardInfo.getValidTillMonth()));

        BeanUtils.copyProperties(memberIdInfo, member); // set memberId
        memberService.createMember(member);

        return memberIdInfo;
    }

    /**
     * アカウントの登録を行います。
     * 
     * <pre>
     * アカウントの登録はパスワードを引数として行い、会員Idを自動生成しアカウントを生成します。
     * 登録した会員にはデフォルトの認可権限として"USER"を付与します。
     * 処理の戻り値として生成した会員Idを返却します。
     * </pre>
     * 
     * @param inputPassword 新規登録するアカウントのパスワード。
     * @return 新規登録した会員Id。
     */
    @Transactional
    private MemberIdInfo registerAccount(String inputPassword) {

        Password password = Password.builder().password(passwordEncoder.encode(inputPassword)).build();
        Account account = Account.builder().build();
        entityManager.persist(account);
        account.getPasswords().add(password);
        account.getAuthorities().add(Authorities.builder().memberId(account.getMemberId()).authority("USER").build());
        account = accountRepository.saveAndFlush(account);

        return MemberIdInfo.builder().memberId(account.getMemberId()).build();
    }
}
