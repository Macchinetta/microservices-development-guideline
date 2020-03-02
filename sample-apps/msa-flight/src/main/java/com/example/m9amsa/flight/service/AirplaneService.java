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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.flight.entity.Airplane;
import com.example.m9amsa.flight.entity.AirplaneRepository;
import com.example.m9amsa.flight.model.AirplaneInfo;

/**
 * 機体情報ビジネスロジック。
 * 
 */
@Service
public class AirplaneService {

    /**
     * 機体情報リポジトリ。
     */
    @Autowired
    AirplaneRepository airplaneRepository;

    /**
     * 機体情報を登録します。
     * 
     * <pre>
     * 既に同じ機体情報が登録されている場合は更新を行います。
     * </pre>
     * 
     * @param airplaneInfo 機体情報。
     * @return DBに保存した機体情報。
     */
    @Transactional
    public Airplane addAirplane(AirplaneInfo airplaneInfo) {
        Example<Airplane> airplaneExample = Example.of(Airplane.builder().name(airplaneInfo.getName()).build());
        Airplane airplane = airplaneRepository.findOne(airplaneExample).orElse(airplaneInfo.asEntity());
        BeanUtils.copyProperties(airplaneInfo, airplane, "id");
        return airplaneRepository.save(airplane);
    }

    /**
     * 機体情報を参照します。
     * 
     * <pre>
     * 登録されている機体情報をすべて取得します。
     * </pre>
     * 
     * @return 機体情報のリスト。機体情報が存在しない場合は0件のリストを返却します。
     */
    @Transactional(readOnly = true)
    public List<Airplane> findAirplaneList() {
        return airplaneRepository.findAll();
    }
}
