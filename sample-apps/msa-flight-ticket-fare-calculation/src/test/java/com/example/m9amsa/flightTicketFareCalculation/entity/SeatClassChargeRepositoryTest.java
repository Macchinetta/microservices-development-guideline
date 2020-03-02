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

import com.example.m9amsa.flightTicketFareCalculation.constant.SeatClass;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_CALCULATE_FARE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=localhost" })
public class SeatClassChargeRepositoryTest {

    @Autowired
    private SeatClassChargeRepository seatClassChargeRepository;

    /**
     * SeatClassChargeは読み取り専用マスタデータなので、データの登録、更新、削除はテストしません。
     */
    @Test
    public void testSeatClassChargeRepositoryCorrect() {
        List<SeatClassCharge> expSeatClassCharges = Arrays.asList( //
                SeatClassCharge.builder().seatClass(SeatClass.N).charge(0).build(), //
                SeatClassCharge.builder().seatClass(SeatClass.S).charge(5000).build());

        List<SeatClassCharge> actSeatClassCharges = seatClassChargeRepository.findAll();

        assertThat("戻り値が正しいこと：サイズ", actSeatClassCharges.size(), equalTo(expSeatClassCharges.size()));
        assertThat("戻り値が正しいこと：内容", actSeatClassCharges.toArray(),
                arrayContainingInAnyOrder(expSeatClassCharges.toArray()));

    }
}
