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
package com.example.m9amsa.reserve.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.m9amsa.reserve.model.VacantSeatInfo;
import com.example.m9amsa.reserve.model.VacantSeatQueryCondition;
import com.example.m9amsa.reserve.service.TicketSearchService;

/**
 * 空席情報照会コントローラー。
 */
@RestController
@RequestMapping("/${info.url.root-path}/reserve")
@Validated
public class TicketSearchController {

    /**
     * 空席照会サービス。
     */
    @Autowired
    TicketSearchService ticketSearchService;

    /**
     * 空席情報照会。
     * 
     * @param condition 空席情報照会条件。
     * @return 空席情報。
     * @throws InvocationTargetException 呼び出されたメソッドによってスローされた例外。
     * @throws IllegalAccessException    現在実行中のメソッドにはアクセス権がない場合、スローされた例外。
     */
    @PostMapping("vacant-seat-info")
    public List<VacantSeatInfo> getVacantSeatInfo(@RequestBody @Valid VacantSeatQueryCondition condition)
            throws IllegalAccessException, InvocationTargetException {
        return ticketSearchService.getVacantSeatInfo(condition);
    }
}
