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
package com.example.m9amsa.account.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.example.m9amsa.account.entity.MemberRepository;
import com.example.m9amsa.account.spy.MemberRepositorySpy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class TestConfig {

    /**
     * APIテストの際にリクエストボディjsonを使用するためのObjectMapper.
     * 
     * <pre>
     * 以下のシリアライザ用フォーマッタを設定
     * - LocalDate : yyyy/MM/dd
     * </pre>
     * 
     * @return シリアライザ設定済みObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        System.out.println("------------------------ Json Mapper -------------------------");
        ObjectMapper jsonMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        jsonMapper.registerModule(module);
        return jsonMapper;
    }

    @Primary
    @Bean(name = "MemberRepositorySpy")
    public MemberRepository memberRepositorySpy(final MemberRepository real) {
        log.info("MemberRepository   : {}", real);
        log.info("MemberRepository   : {}", real.getClass().getName());

        MemberRepositorySpy spy = new MemberRepositorySpy(real);

        log.info("MemberRepositorySpy: {}", spy);
        log.info("MemberRepositorySpy: {}", spy.getClass().getName());

        return spy;
    }
}
