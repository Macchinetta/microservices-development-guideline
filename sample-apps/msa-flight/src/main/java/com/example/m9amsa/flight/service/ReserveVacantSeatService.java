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

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.m9amsa.flight.entity.Flight;
import com.example.m9amsa.flight.entity.FlightRepository;
import com.example.m9amsa.flight.entity.ReserveVacantSeat;
import com.example.m9amsa.flight.entity.ReserveVacantSeatRepository;
import com.example.m9amsa.flight.entity.VacantSeat;
import com.example.m9amsa.flight.entity.VacantSeatPk;
import com.example.m9amsa.flight.entity.VacantSeatRepository;
import com.example.m9amsa.flight.exception.HttpStatus404Exception;
import com.example.m9amsa.flight.exception.BusinessException;
import com.example.m9amsa.flight.model.ReserveVacantSeatInfo;

/**
 * 空席確保サービス。
 * 
 */

@Service
public class ReserveVacantSeatService {

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

    /**
     * フライト情報レポジトリ。
     */
    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * 空席確保を行います。
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
    public VacantSeat reserveVacantSeat(ReserveVacantSeatInfo reserveVacantSeat) {

        boolean isFirstTime = !reserveVacantSeatRepository.existsById(reserveVacantSeat.getReserveId());

        if (isFirstTime) {
            return reserveVacantSeatAtFirstTime(reserveVacantSeat.asEntity());
        } else {
            return reserveVacantSeatAfterFirstTime(reserveVacantSeat.asEntity());
        }

    }

    /**
     * 一回目で、空席確保を行います。
     * 
     * @param reserveVacantSeat 空席確保情報。
     * 
     * @return 空席情報。
     * 
     */
    private VacantSeat reserveVacantSeatAtFirstTime(ReserveVacantSeat reserveVacantSeat) {

        // 空席確保情報を登録します。
        entityManager.persist(reserveVacantSeat);
        entityManager.flush();

        // 空席情報を取得します。
        VacantSeatPk vacantSeatPk = VacantSeatPk.builder() //
                .departureDate(reserveVacantSeat.getDepartureDate()) //
                .flightName(reserveVacantSeat.getFlightName()) //
                .build();
        VacantSeat vacantSeat = vacantSeatRepository.findById(vacantSeatPk).orElseGet(() -> {
            // DBに空席情報が存在しなければ、あらたに空席情報を生成します。
            // フライト情報がない場合は404を返却します。
            Flight flight = flightRepository.findById(reserveVacantSeat.getFlightName()).orElseThrow(HttpStatus404Exception::new);
            return VacantSeat.builder() //
                    .departureDate(reserveVacantSeat.getDepartureDate()) //
                    .flightName(reserveVacantSeat.getFlightName()) //
                    .vacantStandardSeatCount(flight.getAirplane().getStandardSeats()) //
                    .vacantSpecialSeatCount(flight.getAirplane().getSpecialSeats()) //
                    .build();
        });

        // 空席の更新
        switch (reserveVacantSeat.getSeatClass()) {
        case N: // 一般席
            vacantSeat = Optional.of(vacantSeat)
                    // 空席が確保できる場合のみフィルタ
                    .filter(v -> v.getVacantStandardSeatCount().compareTo(reserveVacantSeat.getVacantSeatCount()) >= 0)
                    // 予約席数を減らします。
                    .map(v -> {
                        v.reserveStandardSeat(reserveVacantSeat.getVacantSeatCount());
                        return v;
                    }) //
                       // 更新し、更新後空席情報を返します。空席が確保できなかった場合はエラー
                    .map(vacantSeatRepository::saveAndFlush).orElseThrow(BusinessException::new);
            break;
        case S: // 特別席
            vacantSeat = Optional.of(vacantSeat)
                    // 空席が確保できる場合のみフィルタ
                    .filter(v -> v.getVacantSpecialSeatCount().compareTo(reserveVacantSeat.getVacantSeatCount()) >= 0)
                    // 予約席数を減らします。
                    .map(v -> {
                        v.reserveSpecialSeat(reserveVacantSeat.getVacantSeatCount());
                        return v;
                    }) //
                       // 更新し、更新後空席情報を返す。空席が確保できなかった場合はエラー
                    .map(vacantSeatRepository::saveAndFlush).orElseThrow(BusinessException::new);
            break;
        }

        return vacantSeat;
    }

    /**
     * 二回目以降、更新せず空席情報を返します。
     * 
     * @param reserveVacantSeat 空席確保情報。
     * 
     * @return 空席情報。
     * 
     */
    private VacantSeat reserveVacantSeatAfterFirstTime(ReserveVacantSeat reserveVacantSeat) {

        // 空席情報を取得します。
        VacantSeatPk vacantSeatPk = VacantSeatPk.builder() //
                .departureDate(reserveVacantSeat.getDepartureDate()) //
                .flightName(reserveVacantSeat.getFlightName()) //
                .build();
        VacantSeat vacantSeat = vacantSeatRepository.findById(vacantSeatPk).orElseGet(() -> {
            // DBに空席情報が存在しなければ、あらたに空席情報を生成します。
            Flight flight = flightRepository.findById(reserveVacantSeat.getFlightName()).orElseThrow();
            return VacantSeat.builder() //
                    .departureDate(reserveVacantSeat.getDepartureDate()) //
                    .flightName(reserveVacantSeat.getFlightName()) //
                    .vacantStandardSeatCount(flight.getAirplane().getStandardSeats()) //
                    .vacantSpecialSeatCount(flight.getAirplane().getSpecialSeats()) //
                    .build();
        });

        // 更新せず返します。
        return vacantSeat;

    }

}
