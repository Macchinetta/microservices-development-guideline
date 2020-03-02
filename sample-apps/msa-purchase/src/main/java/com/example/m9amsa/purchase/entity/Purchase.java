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
package com.example.m9amsa.purchase.entity;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import com.example.m9amsa.purchase.constant.SeatClass;
import com.example.m9amsa.purchase.util.BeanUtil;

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
@Entity
public class Purchase implements Serializable {

    private static final long serialVersionUID = 6663796219286830190L;

    /**
     * 購入Id。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long purchaseId;

    /**
     * 予約Id。
     */
    private Long reserveId;

    /**
     * 購入者会員Id。
     */
    private Long purchaseMemberId;

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
     * 搭乗者人数。
     */
    private Integer passengerCount;

    /**
     * 購入情報登録日時。
     */
    private LocalDateTime payDateTime;

    /**
     * 決済情報。
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn
    private Payment payment;

    /**
     * 生成前の日時更新処理。
     */
    @PrePersist
    public void onPrePersist() {
        setPayDateTime(LocalDateTime.now(getClock()));
    }

    /**
     * 現在日付取得用の基準Clock。
     * 
     * @return 現在日付取得用の基準Clock。
     */
    public Clock getClock() {
        BaseClock baseClock = BeanUtil.getBean(BaseClock.class);
        return baseClock.systemDefaultZone();
    }

}
