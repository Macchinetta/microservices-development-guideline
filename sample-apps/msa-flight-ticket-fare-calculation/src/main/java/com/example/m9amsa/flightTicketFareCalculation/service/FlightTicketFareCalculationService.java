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
package com.example.m9amsa.flightTicketFareCalculation.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.flightTicketFareCalculation.entity.Discount;
import com.example.m9amsa.flightTicketFareCalculation.entity.DiscountRepository;
import com.example.m9amsa.flightTicketFareCalculation.entity.PeakRatio;
import com.example.m9amsa.flightTicketFareCalculation.entity.PeakRatioRepository;
import com.example.m9amsa.flightTicketFareCalculation.entity.SeatClassCharge;
import com.example.m9amsa.flightTicketFareCalculation.entity.SeatClassChargeRepository;
import com.example.m9amsa.flightTicketFareCalculation.model.FareCalcInfo;
import com.example.m9amsa.flightTicketFareCalculation.model.FlightFareInfo;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.DiscountCalculator.DiscountType;
import com.example.m9amsa.flightTicketFareCalculation.service.discount.DiscountCalculatorFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * フライトチケット運賃計算サービス。
 * 
 */
@Service
@Slf4j
public class FlightTicketFareCalculationService {

    /**
     * 割引計算のファクトリクラス。
     */
    @Autowired
    private DiscountCalculatorFactory discountCalculatorFactory;

    /**
     * 搭乗クラス加算額リポジトリ。
     */
    @Autowired
    private SeatClassChargeRepository seatClassChargeRepository;

    /**
     * ピーク時期積算比率リポジトリ。
     */
    @Autowired
    private PeakRatioRepository peakRatioRepository;

    /**
     * 割引リポジトリ。
     */
    @Autowired
    private DiscountRepository discountRepository;

    /**
     * 運賃を計算します。
     * 
     * @param fareCalcInfo 運賃計算入力情報。
     * @return フライト運賃一覧。
     */
    @Transactional(readOnly = true)
    public List<FlightFareInfo> calcFare(FareCalcInfo fareCalcInfo) {
        log.info("input:{}", fareCalcInfo);

        List<DiscountType> targetDiscounts;
        if (fareCalcInfo.getDiscountId() == null) {
            targetDiscounts = DiscountType.valuesList().stream() //
                    .filter(d -> d.flightTypeIs(fareCalcInfo.getFlightType())) // flightType でフィルタリング
                    .filter(d -> d.seatClassIs(fareCalcInfo.getSeatClass())) // seatClass でフィルタリング
                    .collect(Collectors.toList());
        } else {
            targetDiscounts = Optional.ofNullable(DiscountType.valueOfId(fareCalcInfo.getDiscountId()))
                    .map(Arrays::asList).orElse(Collections.emptyList());
        }
        log.info("targetDiscounts:{}", targetDiscounts);

        SeatClassCharge seatClassCharge = seatClassChargeRepository.findById(fareCalcInfo.getSeatClass()).orElseThrow();
        log.info("seatClassCharge:{}", seatClassCharge);

        // ピーク時期以外は積算比率1.00で計算します
        PeakRatio peakRatio = peakRatioRepository.findAll().stream()
                .filter(pr -> isBetween(fareCalcInfo.getTravelDate(), pr.getFromDate(), pr.getToDate())).findFirst() //
                .orElse(PeakRatio.builder().ratio(100).build()); // 見つからない場合は積算比率1.00でインスタンスを作成
        log.info("peakRatio:{}", peakRatio);

        return targetDiscounts.stream().map(discountCalculatorFactory::getCalculator) // DiscountId to
                                                                                      // DiscountCalculator
                .filter(c -> c.validateAvailable(fareCalcInfo)) // 入力情報の対象となる割引をフィルタリング
                .map(c -> { // 運賃計算
                    Discount discount = discountRepository.findById(c.targetDiscountId().toString()).orElseThrow();
                    log.info("discount:{}", discount);

                    FlightFareInfo flightFare = new FlightFareInfo();
                    BeanUtils.copyProperties(discount, flightFare);

                    int fare = c.calcFare(fareCalcInfo, seatClassCharge, peakRatio);
                    // 運賃の100円未満を切り上げ
                    fare = (int) Math.ceil((double) fare / 100) * 100;
                    flightFare.setFare(fare);
                    log.info("flightFare:{}", flightFare);

                    return flightFare; // 計算結果をFlightFareとして返却
                }).collect(Collectors.toList());
    }

    /**
     * ある日付がパラメータの区間内であるかどうかを判定します。
     * 
     * @param targetOrg 判断しようとする日付。
     * @param start     区間開始。
     * @param end       区間終了。
     * @return 区間内であればtrueを返します。
     */
    protected boolean isBetween(LocalDate targetOrg, LocalDate start, LocalDate end) {
        // 1901年に変換
        LocalDate target = targetOrg.withYear(1901);
        return target.isEqual(start) || target.isEqual(end) || (target.isAfter(start) && target.isBefore(end));
    }
}
