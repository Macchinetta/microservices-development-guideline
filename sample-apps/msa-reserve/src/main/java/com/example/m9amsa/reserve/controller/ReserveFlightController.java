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

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import com.example.m9amsa.reserve.entity.Reservation;
import com.example.m9amsa.reserve.externalmicroservice.model.PurchaseInfoForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.ReserveVacantSeatForEx;
import com.example.m9amsa.reserve.externalmicroservice.service.FlightExternalMicroService;
import com.example.m9amsa.reserve.externalmicroservice.service.PurchaseExternalMicroService;
import com.example.m9amsa.reserve.model.ReservationRequest;
import com.example.m9amsa.reserve.model.topic.ReservationTopicSource;
import com.example.m9amsa.reserve.service.ReserveFlightService;

import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Tracer;

/**
 * フライトチケット予約コントローラー。
 */
@RestController
@RequestMapping("/${info.url.root-path}/reserve")
@Validated
@EnableBinding(ReservationTopicSource.class)
public class ReserveFlightController {

    /**
     * 予約処理ヘルパー
     */
    @Autowired
    private ReservationHelper reservationHelper;

    /**
     * フライトチケット予約サービス。
     */
    @Autowired
    private ReserveFlightService reserveFlightService;

    /**
     * フライト空席確保サービスのFeignクライアント。
     */
    @Autowired
    private FlightExternalMicroService flightExternalMicroService;

    /**
     * 購入情報登録サービスのFeignクライアント。
     */
    @Autowired
    private PurchaseExternalMicroService purchaseExternalMicroService;

    @Autowired
    private Tracer tracer;

    /**
     * フライトチケット予約。
     * 
     * <pre>
     * フライトチケット予約では以下の順に処理を実行します。
     * </pre>
     * 
     * <ul>
     * <li>空席情報の更新: フライトサービスとの同期処理</li>
     * <li>購入情報の登録: 購入サービスとの同期処理</li>
     * <li>予約情報の登録: ローカルの更新処理</li>
     * <li>予約情報トピックの通知: 予約通知サービスへ非同期処理</li>
     * </ul>
     * 
     * <pre>
     * 空席情報、購入情報、予約情報はそれぞれが組み合わさって意味を成すため、いずれかの更新・登録処理が失敗した場合は全体を処理前の状態にロールバックさせなければなりません。
     * そこで、同期処理を受け付けるサービスはロールバック用のAPIを実装し、同期処理を依頼するサービスは処理の進行に合わせて必要なロールバック処理を依頼できるように用意する必要があります。
     * 
     * フライトチケット予約処理では、同期処理の正常終了時にロールバック処理をリストに追加し、後続処理が失敗した場合に例外ハンドラクラスでローバック処理を実行する方法を採っています。
     * </pre>
     * 
     * @param reservationRequest 予約要求情報。
     * @param request            HTTPリクエスト
     * @throws Exception 例外
     */
    @PostMapping("reserve-flight")
    public void reserveFlight(@RequestBody @Valid ReservationRequest reservationRequest, WebRequest request)
            throws Exception {

        // ユニークなIDとしてOpenTracingが生成するtraceIdを使用します。

        Long reserveId = Long.parseUnsignedLong(((JaegerSpanContext) tracer.activeSpan().context()).getTraceId(), 16);

        /*
         * 【SAGA:補償トランザクション】補償トランザクション処理のリスト
         * WebRequestにセットすることで例外ハンドラに保障トランザクション処理を委譲します。
         */
        List<Runnable> compensations = new ArrayList<>();
        request.setAttribute("compensations", compensations, RequestAttributes.SCOPE_REQUEST);

        Reservation reservation = reservationHelper.convertReservationInfo(reservationRequest, reserveId);

        // フライトサービス同期連携: フライト空席を確保します
        ReserveVacantSeatForEx secureVacantSeatInfo = reservationHelper.createReserveVacantSeat(reservation,
                reservationRequest.getPassengers().size());
        flightExternalMicroService.secureVacantSeat(secureVacantSeatInfo);
        // 【SAGA:補償トランザクション】リストにフライト空席確保のロールバック処理を追加
        compensations.add(0, () -> flightExternalMicroService.cancelReservedSeat(secureVacantSeatInfo));

        // 購入サービス同期連携: 購入情報を登録します
        PurchaseInfoForEx purchaseInfo = reservationHelper.createPurchaseInfo(reservation,
                reservationRequest.getMemberInfo());
        purchaseExternalMicroService.registerPurchaseInfo(purchaseInfo);
        // 【SAGA:補償トランザクション】リストに購入情報登録のロールバック処理を追加
        compensations.add(0, () -> purchaseExternalMicroService.deleteByReserveId(reservation.getReserveId()));

        /*
         * 予約情報を登録します ローカル処理のロールバックは通常通りに@Transactionalに任せ、ここで発生する例外を
         * 例外ハンドラで捕まえて補償トランザクションを実行します。
         */
        reserveFlightService.registerReservationInfo(reservation, purchaseInfo);
    }
}
