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
package com.example.m9amsa.reserve.service;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.reserve.entity.Passenger;
import com.example.m9amsa.reserve.entity.Reservation;
import com.example.m9amsa.reserve.entity.ReservationRepository;
import com.example.m9amsa.reserve.externalmicroservice.model.PurchaseInfoForEx;
import com.example.m9amsa.reserve.model.topic.PassengerTopic;
import com.example.m9amsa.reserve.model.topic.ReservationTopic;
import com.example.m9amsa.reserve.model.topic.ReservationTopicSource;

/**
 * フライトチケットを予約するサービスクラス。
 *
 */
@Service
public class ReserveFlightService {

    /**
     * 予約情報リポジトリ。
     */
    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * 予約情報Topic。
     */
    @Autowired
    private ReservationTopicSource reservationTopicSource;

    /**
     * 予約情報を登録します。
     * 
     * @param reservationInfo 予約情報。
     * @param purchaseInfo    購入情報。
     * @throws InvocationTargetException BeanUtilsで例外が発生した場合にスローされます
     * @throws IllegalAccessException    BeanUtilsで例外が発生した場合にスローされます
     */
    @Transactional
    public void registerReservationInfo(Reservation reservationInfo, PurchaseInfoForEx purchaseInfo)
            throws IllegalAccessException, InvocationTargetException {
        reservationRepository.save(reservationInfo);

        // 予約情報トピックを通知します
        ReservationTopic reservationTopic = ReservationTopic.builder().build();
        BeanUtils.copyProperties(reservationInfo, reservationTopic);

        reservationTopic.setPassenger(
                purchaseInfo.getPassengers().stream().map(this::toPassengerTopic).collect(Collectors.toList()));
        reservationTopicSource.output().send(MessageBuilder.withPayload(reservationTopic).build());
    }

    /**
     * <code>Passenger</code>から<code>PassengerTopic</code>への変換メソッド。
     * 
     * @param passengerInfo
     * @return PassengerTopic
     */
    private PassengerTopic toPassengerTopic(Passenger passenger) {
        PassengerTopic passengerTopic = new PassengerTopic();
        BeanUtils.copyProperties(passenger, passengerTopic);
        return passengerTopic;
    }
}
