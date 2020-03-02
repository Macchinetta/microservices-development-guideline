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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.m9amsa.flight.entity.Flight;
import com.example.m9amsa.flight.entity.VacantSeat;
import com.example.m9amsa.flight.model.FlightUpdateInfo;
import com.example.m9amsa.flight.model.ReserveVacantSeatInfo;
import com.example.m9amsa.flight.model.topic.FlightTopic;
import com.example.m9amsa.flight.model.topic.FlightTopicSource;
import com.example.m9amsa.flight.model.topic.FlightVacantSeatTopic;
import com.example.m9amsa.flight.service.CancelReservedSeatService;
import com.example.m9amsa.flight.service.FlightService;
import com.example.m9amsa.flight.service.ReserveVacantSeatService;

/**
 * フライト情報コントローラークラス。
 * 
 */
@RestController
@RequestMapping("/${info.url.root-path}/flight")
@Validated
@EnableBinding(FlightTopicSource.class)
public class FlightController {

    /**
     * フライトサービス。
     */
    @Autowired
    private FlightService flightService;

    /**
     * 空席確保サービス。
     * 
     */
    @Autowired
    private ReserveVacantSeatService reserveVacantSeatService;

    /**
     * 空席確保取り消しサービス。
     */
    @Autowired
    private CancelReservedSeatService cancelReservedSeatService;

    /**
     * フライト更新モデル用バリデータ。
     */
    @Autowired
    private FlightUpdateInfoValidator flightUpdateInfoValidator;

    /**
     * 空席確保情報用バリデータ。
     */
    @Autowired
    private ReserveVacantSeatValidator reserveVacantSeatValidator;

    /**
     * フライトトピック。
     */
    @Autowired
    private FlightTopicSource flightTopicSource;

    @InitBinder("flightUpdateInfo")
    public void initBinderForFlightUpdateInfo(WebDataBinder binder) {
        binder.setValidator(flightUpdateInfoValidator);
    }

    @InitBinder("reserveVacantSeat")
    public void initBinderForReserveVacantSeat(WebDataBinder binder) {
        binder.setValidator(reserveVacantSeatValidator);
    }

    /**
     * フライト情報を登録します。
     * 
     * <pre>
     * 正常に登録された場合、フライトトピックにフライト情報を通知します。
     * フライト情報のメッセージには、ヘッダ情報として x-payload-class: Flight が設定されます。
     * </pre>
     * 
     * @param flightUpdateInfo 登録するフライト情報。
     */
    @PostMapping
    public void addFlight(@RequestBody @Valid FlightUpdateInfo flightUpdateInfo) {

        Flight flight = flightService.addFlight(flightUpdateInfo);

        FlightTopic flightTopic = new FlightTopic();
        BeanUtils.copyProperties(flightUpdateInfo, flightTopic);
        BeanUtils.copyProperties(flight, flightTopic);

        LocalDateTime departureDateTime = flightUpdateInfo.getDepartureTime();
        flightTopic.setDepartureTime(LocalTime.of(departureDateTime.getHour(), departureDateTime.getMinute(),
                departureDateTime.getSecond()));
        LocalDateTime arrivalDateTime = flightUpdateInfo.getArrivalTime();
        flightTopic.setArrivalTime(
                LocalTime.of(arrivalDateTime.getHour(), arrivalDateTime.getMinute(), arrivalDateTime.getSecond()));

        flightTopicSource.output().send(MessageBuilder.withPayload(flightTopic)
                .setHeader("x-payload-class", FlightTopic.class.getSimpleName()).build());
    }

    /**
     * フライト情報を参照します。
     * 
     * <pre>
     * 全てのフライト情報を取得します。
     * </pre>
     * 
     * @param departureId 出発空港Id。
     * @param arrivalId   到着空港Id。
     * 
     * @return 登録されている全てのフライト情報。
     */
    @GetMapping("/list")
    public List<Flight> findFlightList( //
            @RequestParam(name = "d", required = false) @Size(min = 3, max = 3) @Valid String departureId,
            @RequestParam(name = "a", required = false) @Size(min = 3, max = 3) @Valid String arrivalId) {
        return flightService.findFlightList(Optional.ofNullable(departureId), Optional.ofNullable(arrivalId));

    }

    /**
     * 空席確保を行います。
     * 
     * <pre>
     * 正常に空席確保された場合、フライトトピックにフライト情報を通知します。
     * フライト情報のメッセージには、ヘッダ情報として x-payload-class: VacantSeat が設定されます。
     * </pre>
     * 
     * @param reserveVacantSeat 空席確保情報。
     */
    @PostMapping("/seat/reserve")
    public void reserveVacantSeat(@RequestBody @Valid ReserveVacantSeatInfo reserveVacantSeat) {
        VacantSeat vacantSeat = reserveVacantSeatService.reserveVacantSeat(reserveVacantSeat);

        // フライトトピックに通知
        FlightVacantSeatTopic flightVacantSeatTopic = new FlightVacantSeatTopic();
        BeanUtils.copyProperties(vacantSeat, flightVacantSeatTopic);
        flightTopicSource.output().send(MessageBuilder.withPayload(flightVacantSeatTopic)
                .setHeader("x-payload-class", FlightVacantSeatTopic.class.getSimpleName()).build());

    }

    /**
     * 空席確保取り消しを行います。
     *
     * <pre>
     * 正常に空席確保取り消された場合、フライトトピックにフライト情報を通知します。
     * フライト情報のメッセージには、ヘッダ情報として x-payload-class: VacantSeat が設定されます。
     * </pre>
     * 
     * @param reserveVacantSeat 空席確保情報。
     */
    @PostMapping("/seat/cancel")
    public void cancelReservedSeat(@RequestBody ReserveVacantSeatInfo reserveVacantSeat) {
        VacantSeat vacantSeat = cancelReservedSeatService.cancelReservedSeat(reserveVacantSeat);

        // フライトトピックに通知
        FlightVacantSeatTopic flightVacantSeatTopic = new FlightVacantSeatTopic();
        BeanUtils.copyProperties(vacantSeat, flightVacantSeatTopic);
        flightTopicSource.output().send(MessageBuilder.withPayload(flightVacantSeatTopic)
                .setHeader("x-payload-class", FlightVacantSeatTopic.class.getSimpleName()).build());

    }

}
