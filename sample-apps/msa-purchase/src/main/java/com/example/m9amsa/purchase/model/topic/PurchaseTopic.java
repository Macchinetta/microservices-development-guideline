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
package com.example.m9amsa.purchase.model.topic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.m9amsa.purchase.constant.SeatClass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 購入情報。
 * 
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PurchaseTopic {

    /**
     * 購入Id。
     */
    private Long purchaseId;

    /**
     * 予約Id。
     */
    private Long reserveId;

    /**
     * 出発日。
     */
    private LocalDate departureDate;

    /**
     * 便Id。
     */
    private String flightId;

    /**
     * 出発時刻。
     */
    private LocalTime departureTime;

    /**
     * 到着時刻。
     */
    private LocalTime arrivalTime;

    /**
     * 出発空港ID。
     */
    private String departureAirportId;

    /**
     * 到着空港ID。
     */
    private String arrivalAirportId;

    /**
     * 搭乗クラス種別。
     */
    private SeatClass seatClass;

    /**
     * 運賃種別。
     */
    private String fareType;

    /**
     * 運賃。
     */
    private Integer fare;

    /**
     * 搭乗者。
     */
    private List<PassengerTopic> passengers;

    /**
     * 購入者会員。
     */
    private Optional<MemberTopic> purchaseMember;

    /**
     * 決済Id。
     */
    private Long paymentId;

    /**
     * カード番号。
     */
    private String cardNo;

    /**
     * 決済日時。
     */
    private LocalDateTime payDateTime;

}
