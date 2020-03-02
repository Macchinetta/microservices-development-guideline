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
package com.example.m9amsa.purchaseNotice.service;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.m9amsa.purchaseNotice.entity.BaseClock;
import com.example.m9amsa.purchaseNotice.entity.Purchase;
import com.example.m9amsa.purchaseNotice.entity.PurchaseRepository;
import com.example.m9amsa.purchaseNotice.externalmicroservice.service.FlightExternalMicroServiceWithFallBack;
import com.example.m9amsa.purchaseNotice.model.RssChannel;
import com.example.m9amsa.purchaseNotice.model.RssItem;
import com.example.m9amsa.purchaseNotice.model.RssRoot;
import com.example.m9amsa.purchaseNotice.util.BeanUtil;

/**
 * 購入通知ビジネスロジック。
 * 
 */
@Service
public class PurchaseNoticeService {

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
     * 購入情報リポジトリ。
     */
    @Autowired
    private PurchaseRepository purchaseRepository;

    /**
     * 空港情報取得のFeignクライアントの実現クラス。
     * 
     */
    @Autowired
    private FlightExternalMicroServiceWithFallBack flightExternalMicroServiceWithFallBack;

    /**
     * 購入情報を通知します。
     * 
     * <pre>
     * PurchaseNoticeテーブルを参照し、emailIdに紐づく購入情報をRSSに変換して返します。
     * </pre>
     * 
     * @param emailId ユーザーが入力したメールアドレス
     * @return Rss フォマードした購入情報
     * @throws Exception 例外
     */
    @Transactional(readOnly = true)
    public RssRoot purchaseNotice(String emailId) throws Exception {

        // emailIdを持っているexampleクラス
        Purchase purchaseNoticeInfo = Purchase.builder().emailId(emailId).build();
        Example<Purchase> example = Example.of(purchaseNoticeInfo);

        List<RssItem> items = new ArrayList<RssItem>();
        purchaseRepository.findAll(example).forEach(p -> {
            String description = createDescription(p);
            RssItem item = RssItem.builder()//
                    .title("予約Id：" + p.getReserveId() + "の購入情報")//
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
     * 購入情報を文字列に変換します。
     * 
     * @param purchase 購入情報
     * @return description フォマードした文字列
     */
    private String createDescription(Purchase purchase) {
        StringJoiner description = new StringJoiner(LINE_SEPARATOR);

        boolean isMember = Optional.ofNullable(purchase.getPaymentId()).isPresent();
        String greetings;
        if (isMember) {
            greetings = "ご購入ありがとうございました。予約の詳細メッセージです。";
        } else {
            greetings = "ご予約ありがとうございました。期限切れ前にお支払いください。予約の詳細メッセージです。";
        }
        description.add(greetings);
        description.add(String.format("予約Id：%s", purchase.getReserveId()));

        description.add(String.format("フライト名：%s", purchase.getFlightId()));
        description.add(String.format("出発空港：%s", getAirportInfo(purchase.getDepartureAirportId())));
        description.add(String.format("出発日：%s", purchase.getDepartureDate()));
        description.add(String.format("出発時刻：%s", purchase.getDepartureTime()));
        description.add(String.format("到着空港：%s", getAirportInfo(purchase.getArrivalAirportId())));
        description.add(String.format("到着時刻：%s", purchase.getArrivalTime()));

        description.add(String.format("搭乗クラス：%s", purchase.getSeatClass()));
        description.add(String.format("料金タイプ：%s", purchase.getFareType()));
        description.add(String.format("料金：\\%s", purchase.getFare()));

        purchase.getPassengers().forEach(p -> {
            description.add(p.isMainPassenger() ? "代表搭乗者：" : "同時搭乗者：");
            description.add(String.format("  お名前：%s", p.getName()));
            description.add(String.format("  年齢：%s", p.getAge()));
        });

        if (isMember) {
            description.add(String.format("決済日時：%s", purchase.getPayDateTime()));
            description.add(String.format("カード番号：%s", purchase.getCardNo()));
        } else {
            description.add(String.format("支払期限：%s", purchase.getDepartureDate()));
        }
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
