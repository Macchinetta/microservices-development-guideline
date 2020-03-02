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
package com.example.m9amsa.flightTicketFareCalculation.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flightTicketFareCalculation.service.discount.DiscountCalculator.DiscountType;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_CALCULATE_FARE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=localhost" })
public class DiscountRepositoryTest {

    @Autowired
    private DiscountRepository discountRepository;

    /**
     * Discountは読み取り専用マスタデータなので、データの登録、更新、削除はテストしません。
     */
    @Test
    public void testDiscountRepositoryCorrect() {
        List<Discount> expDiscounts = Arrays.asList( //
                Discount.builder().discountId(DiscountType.ONE_WAY.toString()).name("片道運賃")
                        .description("一般席を利用する航空チケット予約システム利用者の通常運賃。").build(),
                Discount.builder().discountId(DiscountType.ROUND_TRIP.toString()).name("往復運賃")
                        .description("同一路線を往復する場合の運賃。(一般席、特別席の混合不可)").build(),
                Discount.builder().discountId(DiscountType.RESERVE1.toString()).name("予約割１")
                        .description("一般席を利用する航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。").build(),
                Discount.builder().discountId(DiscountType.RESERVE7.toString()).name("予約割７")
                        .description("一般席を利用する航空チケット予約システム利用者が、搭乗日の 7 日前までに予約する場合に利用できる運賃。").build(),
                Discount.builder().discountId(DiscountType.RESERVE30.toString()).name("早期割")
                        .description("一般席を利用する航空チケット予約システム利用者が、搭乗日の 30 日前までに予約する場合に利用できる運賃。").build(),
                Discount.builder().discountId(DiscountType.LADIES.toString()).name("レディース割")
                        .description("一般席を利用する女性の航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。").build(),
                Discount.builder().discountId(DiscountType.GROUP.toString()).name("グループ割")
                        .description("一般席を利用する航空チケット予約システム利用者が、3 名以上で搭乗日の前日までに予約する場合に利用できる運賃。").build(),
                Discount.builder().discountId(DiscountType.SPECIAL_ONE_WAY.toString()).name("特別片道運賃")
                        .description("特別席を利用する航空チケット予約システム利用者の通常運賃。").build(),
                Discount.builder().discountId(DiscountType.SPECIAL_ROUND_TRIP.toString()).name("特別往復運賃")
                        .description("同一路線を往復する場合の運賃。(一般席、特別席の混合不可)").build(),
                Discount.builder().discountId(DiscountType.SPECIAL_RESERVE1.toString()).name("特別予約割")
                        .description("特別席を利用する航空チケット予約システム利用者が、搭乗日の前日までに予約する場合に利用できる運賃。").build());

        List<Discount> actDiscounts = discountRepository.findAll();

        assertThat("戻り値が正しいこと：サイズ", actDiscounts.size(), equalTo(expDiscounts.size()));
        assertThat("戻り値が正しいこと：内容", actDiscounts.toArray(), arrayContainingInAnyOrder(expDiscounts.toArray()));
    }
}
