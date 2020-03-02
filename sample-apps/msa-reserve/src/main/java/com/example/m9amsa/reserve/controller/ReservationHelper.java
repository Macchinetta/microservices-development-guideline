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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.example.m9amsa.reserve.entity.Passenger;
import com.example.m9amsa.reserve.entity.Reservation;
import com.example.m9amsa.reserve.externalmicroservice.model.CardForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.MemberForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.PurchaseInfoForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.ReserveVacantSeatForEx;
import com.example.m9amsa.reserve.model.MemberInfo;
import com.example.m9amsa.reserve.model.PassengerInfoModel;
import com.example.m9amsa.reserve.model.ReservationRequest;

/**
 * フライトチケット予約処理ヘルパー。
 * 
 */
@Component
public class ReservationHelper {

    /**
     * 予約要求情報を内部処理で使用するbeanに変換します。
     * 
     * @param reservationRequest 予約要求情報
     * @param reserveId          予約情報に設定するID
     * @return 予約情報
     * @throws InvocationTargetException {@link BeanUtils#copyProperties(Object, Object)}の処理失敗時にスローされる例外
     * @throws IllegalAccessException    {@link BeanUtils#copyProperties(Object, Object)}の処理失敗時にスローされる例外
     */
    public Reservation convertReservationInfo(ReservationRequest reservationRequest, Long reserveId)
            throws IllegalAccessException, InvocationTargetException {

        Reservation reservation = Reservation.builder().reserveTime(LocalDateTime.now())
                .departureDate(reservationRequest.getDepartureDate()).flightId(reservationRequest.getFlightId())
                .departureTime(reservationRequest.getDepartureTime()).arrivalTime(reservationRequest.getArrivalTime())
                .departureAirportId(reservationRequest.getDepartureAirportId())
                .arrivalAirportId(reservationRequest.getArrivalAirportId()).seatClass(reservationRequest.getSeatClass())
                .fareType(reservationRequest.getFareType()).fare(reservationRequest.getFare()).reserveId(reserveId)
                .build();

        // 予約情報に代表搭乗者情報を設定します
        PassengerInfoModel main = reservationRequest.getPassengers().stream().filter(p -> p.isMainPassenger())
                .findFirst().get();
        Passenger mainPassengerInfo = Passenger.builder().build();
        BeanUtils.copyProperties(main, mainPassengerInfo);
        reservation.setMainPassenger(mainPassengerInfo);

        // 予約情報に同時搭乗者情報を設定します
        List<PassengerInfoModel> subList = reservationRequest.getPassengers().stream().filter(p -> !p.isMainPassenger())
                .collect(Collectors.toList());
        List<Passenger> subPassengerList = new ArrayList<Passenger>();
        for (PassengerInfoModel sub : subList) {
            Passenger subPassengerInfo = Passenger.builder().build();
            BeanUtils.copyProperties(sub, subPassengerInfo);
            subPassengerList.add(subPassengerInfo);
        }
        reservation.setSubPassengers(subPassengerList);
        return reservation;
    }

    /**
     * フライト空席確保サービスのリクエストパラメータを生成します。
     * 
     * @param reservation 予約要求情報
     * @param vacantSeatCount 空席数
     * @return フライト空席確保サービスのリクエストパラメータ
     */
    public ReserveVacantSeatForEx createReserveVacantSeat(Reservation reservation, int vacantSeatCount) {
        ReserveVacantSeatForEx secureVacantSeatInfo = ReserveVacantSeatForEx.builder()
                .reserveId(reservation.getReserveId()).departureDate(reservation.getDepartureDate())
                .flightName(reservation.getFlightId()).build();
        secureVacantSeatInfo.setSeatClass(reservation.getSeatClass());
        secureVacantSeatInfo.setVacantSeatCount(vacantSeatCount);
        return secureVacantSeatInfo;
    }

    /**
     * 購入情報登録サービスのリクエストパラメータを生成します。
     * 
     * @param reservation 予約要求情報
     * @param memberInfo      会員情報
     * @return 購入情報登録サービスのリクエストパラメータ
     */
    public PurchaseInfoForEx createPurchaseInfo(Reservation reservation, Optional<MemberInfo> memberInfo) {
        PurchaseInfoForEx purchaseInfoForEx = PurchaseInfoForEx.builder().reserveId(reservation.getReserveId())
                .departureDate(reservation.getDepartureDate()).flightId(reservation.getFlightId())
                .departureTime(reservation.getDepartureTime()).arrivalTime(reservation.getArrivalTime())
                .departureAirportId(reservation.getDepartureAirportId())
                .arrivalAirportId(reservation.getArrivalAirportId()).seatClass(reservation.getSeatClass())
                .fareType(reservation.getFareType()).fare(reservation.getFare()).passengers(new ArrayList<>())
                .build();
        purchaseInfoForEx.getPassengers().add(reservation.getMainPassenger());
        purchaseInfoForEx.getPassengers().addAll(reservation.getSubPassengers());
        purchaseInfoForEx.setPurchaseMember(memberInfo.map(m -> {
            MemberForEx memberForEx = new MemberForEx();
            BeanUtils.copyProperties(m, memberForEx, "card");
            memberForEx.setCard(m.getCard().map(card -> {
                CardForEx cardForEx = new CardForEx();
                BeanUtils.copyProperties(card, cardForEx);
                return cardForEx;
            }));
            return memberForEx;
        }));
        return purchaseInfoForEx;
    }
}
