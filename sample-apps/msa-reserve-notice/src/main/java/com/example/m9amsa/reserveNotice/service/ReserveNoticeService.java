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

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.m9amsa.reserveNotice.entity.BaseClock;
import com.example.m9amsa.reserveNotice.entity.Reservation;
import com.example.m9amsa.reserveNotice.entity.ReservationRepository;
import com.example.m9amsa.reserveNotice.externalmicroservice.service.FlightExternalMicroServiceWithFallBack;
import com.example.m9amsa.reserveNotice.model.RssChannel;
import com.example.m9amsa.reserveNotice.model.RssItem;
import com.example.m9amsa.reserveNotice.model.RssRoot;
import com.example.m9amsa.reserveNotice.util.BeanUtil;

/**
 * 予約完了通知サービス。
 * 
 */
@Service
public class ReserveNoticeService {

    /**
     * RSSのバージョン。
     */
    @Value("${info.rss.version}")
    private String version;

    /**
     * RSSのタイトル。
     */
    @Value("${info.rss.title}")
    private String title;

    /**
     * チャネルのディスクリプション。
     */
    @Value("${info.rss.channel.description}")
    private String description;

    /**
     * 文字列の改行。
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * 予約完了通知レポジトリ。
     */
    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * 空港情報取得のFeignクライアントの実現クラス。
     * 
     */
    @Autowired
    private FlightExternalMicroServiceWithFallBack flightExternalMicroServiceWithFallBack;

    /**
     * 予約情報を通知します。
     * 
     * <pre>
     * ReservationInfoテーブルを参照し、emailIdに紐づく予約情報をRSSに変換して返します。
     * </pre>
     * 
     * @param emailId ユーザーが入力したメールアドレス
     * @return Rss フォマードした予約情報
     * @throws Exception 例外
     */
    @Transactional(readOnly = true)
    public RssRoot getReserveCompleteNotice(String emailId) throws Exception {

        // emailIdを持っているexampleクラス
        Reservation reservation = Reservation.builder().emailId(emailId).build();
        Example<Reservation> example = Example.of(reservation);

        List<RssItem> items = new ArrayList<RssItem>();
        reservationRepository.findAll(example).forEach(p -> {
            String description = createDescription(p);
            RssItem item = RssItem.builder()//
                    .title("予約Id：" + p.getReserveId() + "の予約情報")//
                    .pubDate(LocalDateTime.now(getClock()).format(DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm")))//
                    .description(description).build();
            items.add(item);
        });

        return formatToRss(items);
    }

    /**
     * Rssの変換処理。
     * 
     * @param items RSSのitem
     * @return Rss フォマードしたRss
     * @throws IOException
     */
    private RssRoot formatToRss(List<RssItem> items) throws IOException {

        RssRoot rssRoot = RssRoot.builder()//
                .version(version)//
                .channel(RssChannel.builder()//
                        .title(title)//
                        .description(description).items(items).build())
                .build();

        return rssRoot;
    }

    /**
     * 予約情報を文字列に変換します。
     * 
     * @param reservation 予約情報
     * @return description フォマードした文字列
     */
    private String createDescription(Reservation reservation) {
        StringJoiner description = new StringJoiner(LINE_SEPARATOR);

        description.add("ご予約ありがとうございました。予約の詳細メッセージです。");

        description.add(String.format("予約Id：%s", reservation.getReserveId()));

        description.add(String.format("フライト名：%s", reservation.getFlightId()));
        description.add(String.format("出発空港：%s", getAirportInfo(reservation.getDepartureAirportId())));
        description.add(String.format("出発日：%s", reservation.getDepartureDate()));
        description.add(String.format("出発時刻：%s", reservation.getDepartureTime()));
        description.add(String.format("到着空港：%s", getAirportInfo(reservation.getArrivalAirportId())));
        description.add(String.format("到着時刻：%s", reservation.getArrivalTime()));

        description.add(String.format("搭乗クラス：%s", reservation.getSeatClass()));
        description.add(String.format("料金タイプ：%s", reservation.getFareType()));
        description.add(String.format("料金：\\%s", reservation.getFare()));

        reservation.getPassengers().forEach(p -> {
            description.add(p.isMainPassenger() ? "代表搭乗者：" : "同時搭乗者：");
            description.add(String.format("  お名前：%s", p.getName()));
            description.add(String.format("  年齢：%s", p.getAge()));
        });
        description.add("");

        return description.toString();
    }

    /**
     * 空港Idにより空港情報文字列を取得します。
     * 
     * @param airportId 空港Id。
     * @return 空港情報文字列。
     */
    private String getAirportInfo(String airportId) {
        String airportName = flightExternalMicroServiceWithFallBack.getAirport(airportId).getName();
        if (StringUtils.isEmpty(airportName)) {
            return airportId;
        } else {
            return String.format("%s - %s", airportId, airportName);
        }
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
