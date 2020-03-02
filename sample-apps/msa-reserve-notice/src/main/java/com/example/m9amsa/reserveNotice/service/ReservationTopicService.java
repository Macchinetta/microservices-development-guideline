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
package com.example.m9amsa.reserveNotice.service;

import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.reserveNotice.entity.Passenger;
import com.example.m9amsa.reserveNotice.entity.Reservation;
import com.example.m9amsa.reserveNotice.entity.ReservationRepository;
import com.example.m9amsa.reserveNotice.model.topic.PassengerTopic;
import com.example.m9amsa.reserveNotice.model.topic.ReservationTopic;

/**
 * ReservationInfoテーブルを操作するサービスクラス。
 * 
 */
@Service
public class ReservationTopicService {

    /**
     * 予約情報リポジトリ。
     */
    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * 予約通知情報を登録します。
     * 
     * <pre>
     * ｛@code reserve-topic}から受け取った予約情報ReservationInfoModelを予約通知情報ReservationInfoに変換して、
     *   ReservationInfoテーブルに登録します。
     * </pre>
     *
     * @param reservationTopic トピックからの予約情報
     */
    @Transactional
    public void registerReservationInfo(ReservationTopic reservationTopic) {

        Reservation reservation = new Reservation();
        BeanUtils.copyProperties(reservationTopic, reservation, "passengers"); // copy without passengers.

        reservation.setPassengers(
                reservationTopic.getPassenger().stream().map(this::toPassenger).collect(Collectors.toList()));
        // emailIdをセットします。
        reservation.getPassengers().stream().filter(Passenger::isMainPassenger).findFirst().map(Passenger::getEmail)
                .ifPresent(reservation::setEmailId);
        reservationRepository.saveAndFlush(reservation);
    }

    private Passenger toPassenger(PassengerTopic passengerTopic) {
        Passenger passenger = new Passenger();
        BeanUtils.copyProperties(passengerTopic, passenger);
        return passenger;
    }
}
