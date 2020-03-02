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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.m9amsa.reserve.constant.SeatClass;
import com.example.m9amsa.reserve.entity.Flight;
import com.example.m9amsa.reserve.entity.FlightRepository;
import com.example.m9amsa.reserve.entity.FlightVacantSeat;
import com.example.m9amsa.reserve.entity.FlightVacantSeatId;
import com.example.m9amsa.reserve.entity.FlightVacantSeatRepository;
import com.example.m9amsa.reserve.externalmicroservice.model.FareCalcInfoForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.FlightFareForEx;
import com.example.m9amsa.reserve.externalmicroservice.service.CalculateFareExternalMicroService;
import com.example.m9amsa.reserve.model.FareInfo;
import com.example.m9amsa.reserve.model.VacantSeatInfo;
import com.example.m9amsa.reserve.model.VacantSeatQueryCondition;

/**
 * 空席照会サービス。
 */
@Service
public class TicketSearchService {

    /**
     * 運賃計算サービスのFeignクライアント。
     */
    @Autowired
    CalculateFareExternalMicroService calculateFareExternalMicroService;

    /**
     * フライト情報リポジトリ。
     */
    @Autowired
    FlightRepository flightRepository;

    /**
     * フライト空席情報リポジトリ。
     */
    @Autowired
    FlightVacantSeatRepository flightVacantSeatRepository;

    /**
     * 空席情報一覧を取得します。
     * 
     * @param condition 空席情報照会条件。
     * @return 空席情報一覧。
     * @throws InvocationTargetException 呼び出されたメソッドによってスローされた例外。
     * @throws IllegalAccessException    現在実行中のメソッドにはアクセス権がない場合、スローされた例外。
     */
    @Transactional(readOnly = true)
    public List<VacantSeatInfo> getVacantSeatInfo(VacantSeatQueryCondition condition)
            throws IllegalAccessException, InvocationTargetException {
        // フライト情報検索
        Flight searchCondition = Flight.builder().departureAirportId(condition.getDepartureAirportId())
                .arrivalAirportId(condition.getArrivalAirportId()).build();
        Example<Flight> example = Example.of(searchCondition);
        Sort sort = new Sort(Direction.ASC, "departureTime");
        List<Flight> flightInfoList = flightRepository.findAll(example, sort);
        // 運賃取得: flightInfoList中の区間基本料金はすべて一致します
        List<FareInfo> fareInfo = flightInfoList.isEmpty() ? new ArrayList<>()
                : this.getFareInfo(condition, flightInfoList.get(0).getFare());

        List<VacantSeatInfo> vacantSeatList = new ArrayList<>();
        for (Flight flightInfo : flightInfoList) {
            // 日時と便名を指定して残席数を取得します
            FlightVacantSeatId flightVacantSeatId = FlightVacantSeatId.builder().flightName(flightInfo.getName())
                    .departureDate(condition.getDepartureDate()).build();
            Optional<FlightVacantSeat> flightVacantSeatInfo = flightVacantSeatRepository.findById(flightVacantSeatId);

            Integer vacantSeats = flightVacantSeatInfo
                    .map(v -> SeatClass.N.name().equals(condition.getSeatClass().name()) ? v.getStandardSeats()
                            : v.getSpecialSeats())
                    .orElse(SeatClass.N.name().equals(condition.getSeatClass().name()) ? flightInfo.getStandardSeats()
                            : flightInfo.getSpecialSeats());

            VacantSeatInfo vacantSeatInfo = new VacantSeatInfo();
            BeanUtils.copyProperties(flightInfo, vacantSeatInfo);
            vacantSeatInfo.setVacantSeats(vacantSeats);
            vacantSeatInfo.setSeatClass(condition.getSeatClass());
            vacantSeatInfo.setFareList(fareInfo);
            vacantSeatList.add(vacantSeatInfo);
        }

        return vacantSeatList;
    }

    /**
     * 運賃計算サービスから照会中フライトの運賃を取得します。
     * 
     * @param condition 空席照会条件。
     * @param basicFare 基本料金。
     * @return 運賃情報一覧。
     * @throws InvocationTargetException 呼び出されたメソッドによってスローされた例外。
     * @throws IllegalAccessException    現在実行中のメソッドにはアクセス権がない場合、スローされた例外。
     */
    private List<FareInfo> getFareInfo(VacantSeatQueryCondition condition, Integer basicFare)
            throws IllegalAccessException, InvocationTargetException {
        FareCalcInfoForEx calcInput = new FareCalcInfoForEx();
        BeanUtils.copyProperties(condition, calcInput);
        calcInput.setTravelDate(condition.getDepartureDate());
        calcInput.setTotalPassengers(1); // ここでは人数が入力されないのでデフォルト値を入れます
        calcInput.setBasicFare(basicFare);
        calcInput.setFlightType(condition.getFlightType().getCode());

        List<FlightFareForEx> flightFare = calculateFareExternalMicroService.calcFare(calcInput);

        List<FareInfo> fareInfo = new ArrayList<>();
        for (FlightFareForEx f : flightFare) {
            FareInfo fi = new FareInfo();
            fi.setFareCode(f.getDiscountId());
            fi.setFareType(f.getName());
            fi.setFare(f.getFare());
            fareInfo.add(fi);
        }

        return fareInfo;
    }
}
