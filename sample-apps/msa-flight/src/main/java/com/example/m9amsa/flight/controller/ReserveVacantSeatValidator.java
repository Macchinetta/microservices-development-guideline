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
package com.example.m9amsa.flight.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.example.m9amsa.flight.entity.FlightRepository;
import com.example.m9amsa.flight.entity.ReserveVacantSeat;
import com.example.m9amsa.flight.exception.HttpStatus404Exception;

/**
 * 空席確保情報用バリデータ。
 * 
 */
@Component
public class ReserveVacantSeatValidator implements Validator {
    /**
     * フライトレポジトリ。
     */
    @Autowired
    private FlightRepository flightRepository;

    @Override
    public boolean supports(Class<?> targetClass) {
        return ReserveVacantSeat.class.isAssignableFrom(targetClass);
    }

    /**
     * チェックメソッド。
     * 
     * @param target パラメータオブジェクト。
     * @param errors エラー情報。
     */
    @Override
    public void validate(Object target, Errors errors) {
        ReserveVacantSeat reserveVacantSeat = (ReserveVacantSeat) target;

        if ((reserveVacantSeat.getReserveId() == null) || //
                (reserveVacantSeat.getDepartureDate() == null) || //
                (reserveVacantSeat.getFlightName() == null) || //
                (reserveVacantSeat.getSeatClass() == null) || //
                (reserveVacantSeat.getVacantSeatCount() == null)) {
            // 必須チェックは別で行うのでここでは判定しません
            return;
        }

        if (!flightRepository.existsById(reserveVacantSeat.getFlightName())) {
            throw new HttpStatus404Exception();
        }
    }

}
