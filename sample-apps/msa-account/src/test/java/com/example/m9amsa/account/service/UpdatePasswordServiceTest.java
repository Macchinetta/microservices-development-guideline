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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.account.entity.Account;
import com.example.m9amsa.account.entity.AccountRepository;
import com.example.m9amsa.account.entity.Authorities;
import com.example.m9amsa.account.entity.Password;
import com.example.m9amsa.account.entity.PasswordRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_ACCOUNT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_APIGATEWAY=localhost" })
public class UpdatePasswordServiceTest {

    @Autowired
    private UpdatePasswordService updatePasswordService;

    /**
     * アカウントレポジトリ
     */
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Before
    public void setUp() throws Exception {
        accountRepository.deleteAll();
        passwordRepository.deleteAll();
    }

    /**
     * Test for UpdatePasswordService#updatePassword()
     */
    @Test
    public void testUpdatePassword() {

        // 変更するアカウントの設定
        Account account = Account.builder().build();
        account = accountRepository.save(account);
        account.getAuthorities().add(Authorities.builder().memberId(account.getMemberId()).authority("USER").build());
        account.getPasswords().add(Password.builder().password("current-password").build());
        account = accountRepository.save(account);

        // run
        updatePasswordService.updatePassword(account.getMemberId(), "new-password");

        // verify
        Account result = accountRepository.findById(account.getMemberId()).get();
        assertThat("passwordが2件に増えていること", result.getPasswords().size(), equalTo(2));
        assertThat("passwordの先頭が変更後パスワードであること",
                passwordEncoder.matches("new-password", result.getPasswords().stream().findFirst().get().getPassword()),
                equalTo(true));

        // 異常系
        try {
            updatePasswordService.updatePassword(-1L, "new-password");
            fail("存在しない会員IDを指定されて正常終了したらテスト失敗");
        } catch (Exception e) {
            assertThat("会員検索0件の場合にデータ無し例外を返せること", e.getClass(), equalTo(NoSuchElementException.class));
        }
    }

}
