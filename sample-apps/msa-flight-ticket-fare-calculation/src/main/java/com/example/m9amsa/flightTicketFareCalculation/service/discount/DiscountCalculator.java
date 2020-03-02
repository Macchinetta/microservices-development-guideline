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
package com.example.m9amsa.flightTicketFareCalculation.service.discount;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.example.m9amsa.flightTicketFareCalculation.constant.FlightType;
import com.example.m9amsa.flightTicketFareCalculation.constant.SeatClass;
import com.example.m9amsa.flightTicketFareCalculation.entity.PeakRatio;
import com.example.m9amsa.flightTicketFareCalculation.entity.SeatClassCharge;
import com.example.m9amsa.flightTicketFareCalculation.model.FareCalcInfo;

import lombok.Getter;

/**
 * 割引計算処理。
 * 
 * <pre>
 * 割引計算を行う計算ロジック。
 * {@link com.example.m9amsa.flightTicketFareCalculation.entity.Discount Discount}#discountIdで設定されているIdと同じ名前のクラスに計算ロジックを実現します。
 * 
 * {@link com.example.m9amsa.flightTicketFareCalculation.entity.Discount Discount}#discountId:reserve1 の場合。
 * 
 * <code>
 * public class Reserve1 implements DiscountCalculator {
 * // 省略
 * }
 * </code>
 * </pre>
 * 
 */
public interface DiscountCalculator {

    /**
     * 割引計算を行います。
     * 
     * @param fareCalcInfo    運賃計算入力情報。
     * @param seatClassCharge 搭乗クラス加算額。
     * @param peakRatio       ピーク時期積算比率。
     * @return 割引後の運賃。
     */
    int calcFare(FareCalcInfo fareCalcInfo, SeatClassCharge seatClassCharge, PeakRatio peakRatio);

    /**
     * この計算ロジックの対象割引を取得します。
     * 
     * @return 対象のDiscountId。
     */
    DiscountType targetDiscountId();

    /**
     * 基本運賃を計算します。
     * 
     * <pre>
     * 基本運賃 = (区間の基本料金 + 搭乗クラスの加算料金) * ピーク時期料金積算比率（％）
     * </pre>
     * 
     * @param baseFare 区間の基本料金。
     * @param charge   搭乗クラスの加算料金。
     * @param ratio    ピーク時期料金積算比率（％）。
     * @return 基本運賃。
     */
    default int basicFare(int baseFare, int charge, int ratio) {
        return (baseFare + charge) * ratio / 100;
    }

    /**
     * 入力情報が該当の割引に適用可能か判定します。
     * 
     * @param input 運賃計算入力情報。
     * @return 適用可能であればtrueと返します。
     */
    default boolean validateAvailable(FareCalcInfo input) {
        return Optional.of(input).filter(i -> //
        isAvailableSection(i.getDepartureAirportId(), i.getArrivalAirportId()) && //
                isAvailableTravelDate(i.getTravelDate()) && //
                isAvailableTotalPassengers(i.getTotalPassengers()) //
        ).isPresent();
    }

    /**
     * 利用可能区間判定。
     * 
     * @param departureAirportId 出発空港ID。
     * @param arrivalAirportId   到着空港ID。
     * @return 利用可能区間があればtrueと返します。
     */
    default boolean isAvailableSection(String departureAirportId, String arrivalAirportId) {
        return true;
    }

    /**
     * 利用可能時期判定。
     * 
     * @param travelDate 入力した時期。
     * @return 搭乗日90日前～搭乗日当日に当たればtrueを返します。
     */
    default boolean isAvailableTravelDate(LocalDate travelDate) {
        Duration duration = Duration.between(LocalDate.now(getClock()).atStartOfDay(), travelDate.atStartOfDay());
        // 搭乗日90日前～搭乗日当日
        return (0 <= duration.toDays()) && (duration.toDays() <= 90);
    }

    /**
     * 現在日付取得用の基準Clock。
     * 
     * @return 現在日付取得用の基準Clock。
     */
    default Clock getClock() {
        return Clock.systemDefaultZone();
    }

    /**
     * 利用可能搭乗人数判定。
     * 
     * @param totalPassengers 搭乗人数。
     * @return 一人以上であればtrueと返します。
     */
    default boolean isAvailableTotalPassengers(Integer totalPassengers) {
        return (totalPassengers >= 1);
    }

    /**
     * 割引種別列挙クラス。
     * 
     */
    @Getter
    public enum DiscountType {
        ONE_WAY("oneWayDiscount", FlightType.OW, SeatClass.N), // 片道運賃
        ROUND_TRIP("roundTripDiscount", FlightType.RT, SeatClass.N), // 往復運賃
        RESERVE1("reserve1Discount", FlightType.OW, SeatClass.N), // 予約割１
        RESERVE7("reserve7Discount", FlightType.OW, SeatClass.N), // 予約割７
        RESERVE30("reserve30Discount", FlightType.OW, SeatClass.N), // 早期割
        LADIES("ladiesDiscount", FlightType.OW, SeatClass.N), // レディース割
        GROUP("groupDiscount", FlightType.OW, SeatClass.N), // グループ割
        SPECIAL_ONE_WAY("specialOneWayDiscount", FlightType.OW, SeatClass.S), // 特別片道運賃
        SPECIAL_ROUND_TRIP("specialRoundTripDiscount", FlightType.RT, SeatClass.S), // 特別往復運賃
        SPECIAL_RESERVE1("specialReserve1Discount", FlightType.OW, SeatClass.S), // 特別予約割
        ;

        private String discountId;

        private FlightType flightType;

        private SeatClass seatClass;

        private DiscountType(String discountId, FlightType flightType, SeatClass seatClass) {
            this.discountId = discountId;
            this.flightType = flightType;
            this.seatClass = seatClass;
        }

        @Override
        public String toString() {
            return discountId;
        }

        /**
         * discountIdと等価であることを判定します。
         * 
         * @param id 比較するdiscountId。
         * @return discountIdと等価であればtrueと返します。
         */
        public boolean equivalent(String id) {
            return discountId.equals(id);
        }

        /**
         * discountIdをもとに対応するインスタンスを生成します。
         * 
         * @param id 割り引きID。
         * @return 割引種別。discountIdをもとに対応するインスタンス。
         */
        public static DiscountType valueOfId(String id) {
            return Arrays.asList(DiscountType.values()).stream().filter(d -> d.equivalent(id)).findFirst().orElse(null);
        }

        /**
         * 割引リストを生成します。
         * 
         * @return 全ての割引種別。
         */
        public static List<DiscountType> valuesList() {
            return Arrays.asList(DiscountType.values());
        }

        /**
         * フライト種別の存在を判定します。
         * 
         * @param flightType フライト種別。
         * @return 入力のフライト種別が存在する場合、trueを戻します。
         */
        public boolean flightTypeIs(FlightType flightType) {
            return this.flightType.equals(flightType);
        }

        /**
         * 搭乗クラスの存在を判定します。
         * 
         * @param seatClass 搭乗クラス
         * @return 入力の搭乗クラスが存在する場合、trueを戻します。
         */
        public boolean seatClassIs(SeatClass seatClass) {
            return this.seatClass.equals(seatClass);
        }
    }
}
