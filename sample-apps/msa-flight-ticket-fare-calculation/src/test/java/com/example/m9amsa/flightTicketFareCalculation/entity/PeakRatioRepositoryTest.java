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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_CALCULATE_FARE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=localhost" })
public class PeakRatioRepositoryTest {

    @Autowired
    private PeakRatioRepository peakRatioRepository;

    /**
     * PeakRatioは読み取り専用マスタデータなので、データの登録、更新、削除はテストしません。
     */
    @Test
    public void testPeakRatioRepositoryCorrect() {
        List<PeakRatio> expPeakRatios = Arrays.asList( //
                PeakRatio.builder().fromDate(LocalDate.parse("1901-01-01")).toDate(LocalDate.parse("1901-01-05"))
                        .ratio(140).build(),
                PeakRatio.builder().fromDate(LocalDate.parse("1901-03-20")).toDate(LocalDate.parse("1901-03-31"))
                        .ratio(140).build(),
                PeakRatio.builder().fromDate(LocalDate.parse("1901-08-08")).toDate(LocalDate.parse("1901-08-18"))
                        .ratio(140).build(),
                PeakRatio.builder().fromDate(LocalDate.parse("1901-12-26")).toDate(LocalDate.parse("1901-12-31"))
                        .ratio(140).build(),
                PeakRatio.builder().fromDate(LocalDate.parse("1901-07-18")).toDate(LocalDate.parse("1901-08-07"))
                        .ratio(125).build(),
                PeakRatio.builder().fromDate(LocalDate.parse("1901-08-19")).toDate(LocalDate.parse("1901-08-31"))
                        .ratio(125).build(),
                PeakRatio.builder().fromDate(LocalDate.parse("1901-12-19")).toDate(LocalDate.parse("1901-12-25"))
                        .ratio(125).build(),
                PeakRatio.builder().fromDate(LocalDate.parse("1901-03-13")).toDate(LocalDate.parse("1901-03-19"))
                        .ratio(125).build()//
        );

        List<PeakRatio> actPeakRatios = peakRatioRepository.findAll();

        assertThat("戻り値が正しいこと：サイズ", actPeakRatios.size(), equalTo(expPeakRatios.size()));
        assertThat("戻り値が正しいこと：内容", actPeakRatios.toArray(), arrayContainingInAnyOrder(expPeakRatios.toArray()));

    }
}
