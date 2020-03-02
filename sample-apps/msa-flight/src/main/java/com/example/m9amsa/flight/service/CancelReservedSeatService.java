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

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.m9amsa.flight.entity.ReserveVacantSeat;
import com.example.m9amsa.flight.entity.ReserveVacantSeatRepository;
import com.example.m9amsa.flight.entity.VacantSeat;
import com.example.m9amsa.flight.entity.VacantSeatPk;
import com.example.m9amsa.flight.entity.VacantSeatRepository;
import com.example.m9amsa.flight.exception.HttpStatus404Exception;
import com.example.m9amsa.flight.model.ReserveVacantSeatInfo;

/**
 * 空席確保取り消しサービス。
 * 
 */

@Service
public class CancelReservedSeatService {

    /**
     * 空席確保情報レポジトリ。
     */
    @Autowired
    private ReserveVacantSeatRepository reserveVacantSeatRepository;

    /**
     * 空席情報レポジトリ。
     */
    @Autowired
    private VacantSeatRepository vacantSeatRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * 空席確保取り消しを行います。
     * 
     * <pre>
     * 冪等性を持っています。
     * </pre>
     * 
     * @param reserveVacantSeat 空席確保情報。
     * 
     * @return 空席情報。
     * 
     */
    @Transactional
    public VacantSeat cancelReservedSeat(ReserveVacantSeatInfo reserveVacantSeat) {

        boolean isFirstTime = reserveVacantSeatRepository.existsById(reserveVacantSeat.getReserveId());

        if (isFirstTime) {
            return cancelReservedSeatAtFirstTime(reserveVacantSeat.asEntity());
        } else {
            return cancelReservedSeatAfterFirstTime(reserveVacantSeat.asEntity());
        }

    }

    /**
     * 一回目で、空席確保取り消しを行います。
     * 
     * @param reserveId 予約Id。
     * 
     * @return 空席情報。
     * 
     */
    private VacantSeat cancelReservedSeatAtFirstTime(ReserveVacantSeat reserveVacantSeat) {

        // DBから空席確保情報を削除します
        reserveVacantSeatRepository.findById(reserveVacantSeat.getReserveId()).orElseThrow(HttpStatus404Exception::new);
        reserveVacantSeatRepository.deleteById(reserveVacantSeat.getReserveId());

        // 空席情報を取得します
        VacantSeatPk vacantSeatPk = VacantSeatPk.builder() //
                .departureDate(reserveVacantSeat.getDepartureDate()) //
                .flightName(reserveVacantSeat.getFlightName()) //
                .build();
        VacantSeat vacantSeat = vacantSeatRepository.findById(vacantSeatPk).orElseThrow();

        // 空席の取り消し
        switch (reserveVacantSeat.getSeatClass()) {
        case N: // 一般席
            vacantSeat.cancelStandardSeat(reserveVacantSeat.getVacantSeatCount());
            break;
        case S: // 特別席
            vacantSeat.cancelSpecialSeat(reserveVacantSeat.getVacantSeatCount());
            break;
        }

        entityManager.persist(vacantSeat);
        entityManager.flush();
        return vacantSeat;
    }

    /**
     * 二回目以降、更新せず空席情報を返します。
     * 
     * @param reserveId 予約Id。
     * 
     * @return 空席情報。
     * 
     */
    private VacantSeat cancelReservedSeatAfterFirstTime(ReserveVacantSeat reserveVacantSeat) {

        // 空席情報を取得します
        VacantSeatPk vacantSeatPk = VacantSeatPk.builder() //
                .departureDate(reserveVacantSeat.getDepartureDate()) //
                .flightName(reserveVacantSeat.getFlightName()) //
                .build();
        return vacantSeatRepository.findById(vacantSeatPk).orElseThrow();
    }

}
