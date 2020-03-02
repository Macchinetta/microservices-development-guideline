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
package com.example.m9amsa.flight.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.flight.entity.BasicFare;
import com.example.m9amsa.flight.entity.BasicFareRepository;
import com.example.m9amsa.flight.model.BasicFareInfo;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * 区間運賃情報ビジネスロジック。
 * 
 */
@Service
public class BasicFareService {

    /**
     * 区間運賃情報リポジトリ。
     */
    @Autowired
    BasicFareRepository basicFareRepository;

    /**
     * 区間運賃情報を登録します。
     * 
     * <pre>
     * 既に同じ区間運賃情報が登録されている場合は更新を行います。
     * 更新は復路についても行います。
     * Input:
     *    Departure: HND
     *    Arrival: CTS
     *    Fare: 30000
     * Save1:
     *    Departure: HND
     *    Arrival: CTS
     *    Fare: 30000
     * Save2:
     *    Departure: CTS
     *    Arrival: HND
     *    Fare: 30000
     * </pre>
     * 
     * @param basicFare 区間運賃情報。
     * @return 更新した区間運賃情報。往路、復路両方のインスタンスを返却します。
     * 
     */
    @Transactional
    public Tuple2<BasicFare, BasicFare> addBasicFare(BasicFareInfo basicFare) {
        BasicFare basicFareOutbound = basicFareRepository.save(basicFare.asEntity());
        BasicFare basicFareReturnpath = basicFareRepository.save(BasicFare.builder().departure(basicFare.getArrival())
                .arrival(basicFare.getDeparture()).fare(basicFare.getFare()).build());
        return Tuples.of(basicFareOutbound, basicFareReturnpath);
    }

    /**
     * 区間運賃情報を参照します。
     * 
     * <pre>
     * 登録されている区間運賃情報をすべて取得します。
     * </pre>
     * 
     * @param departureId 出発空港。
     * @param arrivalId   到着空港。
     * @return 区間運賃情報のリスト。区間運賃情報が存在しない場合は0件のリストを返却します。
     */
    @Transactional(readOnly = true)
    public List<BasicFare> findBasicFareList(Optional<String> departureId, Optional<String> arrivalId) {
        Example<BasicFare> basicFareExample = Example
                .of(BasicFare.builder().departure(departureId.orElse(null)).arrival(arrivalId.orElse(null)).build());
        return basicFareRepository.findAll(basicFareExample);
    }
}
