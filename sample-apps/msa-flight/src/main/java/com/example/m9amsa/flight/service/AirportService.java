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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.flight.entity.Airport;
import com.example.m9amsa.flight.entity.AirportRepository;
import com.example.m9amsa.flight.model.AirportInfo;

/**
 * 空港情報ビジネスロジック。
 * 
 */
@Service
public class AirportService {

    /**
     * 空港情報リポジトリ。
     */
    @Autowired
    AirportRepository airportRepository;

    /**
     * 空港情報を登録します。
     * 
     * <pre>
     * 既に同じ空港情報が登録されている場合は更新を行います。
     * </pre>
     * 
     * @param airport 空港情報。
     * @return Airport 空港情報。
     */
    @Transactional
    public Airport addAirport(AirportInfo airport) {
        return airportRepository.save(airport.asEntity());
    }

    /**
     * 空港情報リストを取得します。
     * 
     * <pre>
     * 登録されている空港情報をすべて取得します。
     * </pre>
     * 
     * @return 空港情報のリスト。空港情報が存在しない場合は0件のリストを返却します。
     */
    @Transactional(readOnly = true)
    public List<Airport> findAirportList() {
        return airportRepository.findAll();
    }

    /**
     * 空港情報を取得します。
     * 
     * <pre>
     * 空港Idに対応する空港情報を取得します。
     * </pre>
     * 
     * @param airportId 空港Id
     * @return 空港情報。
     */
    @Transactional(readOnly = true)
    public Optional<Airport> findAirport(String airportId) {
        return airportRepository.findById(airportId);
    }
}
