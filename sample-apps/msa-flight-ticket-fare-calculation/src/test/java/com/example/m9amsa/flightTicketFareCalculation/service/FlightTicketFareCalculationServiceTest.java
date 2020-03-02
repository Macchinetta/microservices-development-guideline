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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flightTicketFareCalculation.TestConfiguration;
import com.example.m9amsa.flightTicketFareCalculation.constant.FlightType;
import com.example.m9amsa.flightTicketFareCalculation.constant.SeatClass;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_CALCULATE_FARE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=localhost" })
@Import(TestConfiguration.class)
@Slf4j
public class FlightTicketFareCalculationServiceTest {

    @SpyBean
    private SeatClassChargeRepository seatClassChargeRepository;

    @SpyBean
    private PeakRatioRepository peakRatioRepository;

    @SpyBean
    private DiscountRepository discountRepository;

    @SpyBean
    private DiscountCalculatorFactory discountCalculatorFactory;

    @InjectMocks
    private FlightTicketFareCalculationService flightTicketFareCalculationService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(seatClassChargeRepository);
        reset(peakRatioRepository);
        reset(discountRepository);
    }

    /**
     * Test for calcFare(). <br>
     * 正常ケース１。 <br>
     * 割引コード指定なし。
     */
    @Test
    public void testCalcFareCorrect1() {

        FareCalcInfo input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .basicFare(30000).flightType(FlightType.OW).seatClass(SeatClass.N).totalPassengers(1)
                .travelDate(LocalDate.now().plusDays(1)).build();

        doReturn(Optional.of(SeatClassCharge.builder().charge(0).build())).when(seatClassChargeRepository)
                .findById(any(SeatClass.class));

        doReturn(Arrays.asList(PeakRatio.builder().fromDate(LocalDate.now().withYear(1901).minusDays(1))
                .toDate(LocalDate.now().withYear(1901).plusDays(1)).ratio(140).build())).when(peakRatioRepository)
                        .findAll();

        List<FlightFareInfo> actualList = flightTicketFareCalculationService.calcFare(input);
        log.info("** {}", actualList);

        assertThat("運賃の件数が正しいこと", actualList.size(), equalTo(3));

        // 片道運賃
        Optional<FlightFareInfo> actualFlightFare = actualList.stream() //
                .filter(f -> DiscountType.ONE_WAY.getDiscountId().equals(f.getDiscountId())) //
                .findFirst();

        Discount discount = discountRepository.findById(DiscountType.ONE_WAY.toString()).orElseThrow();

        assertTrue("片道運賃が計算されていること", actualFlightFare.isPresent());
        assertThat("片道運賃が計算されていること", actualFlightFare.get().getName(), equalTo(discount.getName()));
        assertThat("片道運賃が計算されていること", actualFlightFare.get().getDescription(), equalTo(discount.getDescription()));
        assertThat("片道運賃が計算されていること", actualFlightFare.get().getFare(), equalTo(42000));

        // 予約割１
        actualFlightFare = actualList.stream() //
                .filter(f -> DiscountType.RESERVE1.getDiscountId().equals(f.getDiscountId())) //
                .findFirst();

        discount = discountRepository.findById(DiscountType.RESERVE1.toString()).orElseThrow();

        assertTrue("予約割１が計算されていること", actualFlightFare.isPresent());
        assertThat("予約割１が計算されていること", actualFlightFare.get().getName(), equalTo(discount.getName()));
        assertThat("予約割１が計算されていること", actualFlightFare.get().getDescription(), equalTo(discount.getDescription()));
        assertThat("予約割１が計算されていること", actualFlightFare.get().getFare(), equalTo(37800));

        // レディース割
        actualFlightFare = actualList.stream() //
                .filter(f -> DiscountType.LADIES.getDiscountId().equals(f.getDiscountId())) //
                .findFirst();

        discount = discountRepository.findById(DiscountType.LADIES.toString()).orElseThrow();

        assertTrue("レディース割が計算されていること", actualFlightFare.isPresent());
        assertThat("レディース割が計算されていること", actualFlightFare.get().getName(), equalTo(discount.getName()));
        assertThat("レディース割が計算されていること", actualFlightFare.get().getDescription(), equalTo(discount.getDescription()));
        assertThat("レディース割が計算されていること", actualFlightFare.get().getFare(), equalTo(29400));
    }

    /**
     * Test for calcFare(). <br>
     * 正常ケース２。 <br>
     * 割引コード：グループ割
     */
    @Test
    public void testCalcFareCorrect2() {

        FareCalcInfo input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .basicFare(30000).flightType(FlightType.OW).seatClass(SeatClass.N).totalPassengers(3)
                .travelDate(LocalDate.now().plusDays(1))//
                .discountId(DiscountType.GROUP.getDiscountId())//
                .build();

        doReturn(Optional.of(SeatClassCharge.builder().charge(0).build())).when(seatClassChargeRepository)
                .findById(any(SeatClass.class));

        doReturn(Arrays.asList(PeakRatio.builder().fromDate(LocalDate.now().withYear(1901).minusDays(1))
                .toDate(LocalDate.now().withYear(1901).plusDays(1)).ratio(140).build())).when(peakRatioRepository)
                        .findAll();

        List<FlightFareInfo> actualList = flightTicketFareCalculationService.calcFare(input);
        log.info("** {}", actualList);

        assertThat("運賃の件数が正しいこと", actualList.size(), equalTo(1));

        // グループ割
        Optional<FlightFareInfo> actualFlightFare = actualList.stream() //
                .filter(f -> DiscountType.GROUP.getDiscountId().equals(f.getDiscountId())) //
                .findFirst();

        Discount discount = discountRepository.findById(DiscountType.GROUP.toString()).orElseThrow();

        assertTrue("グループ割が計算されていること", actualFlightFare.isPresent());
        assertThat("グループ割が計算されていること", actualFlightFare.get().getName(), equalTo(discount.getName()));
        assertThat("グループ割が計算されていること", actualFlightFare.get().getDescription(), equalTo(discount.getDescription()));
        assertThat("グループ割が計算されていること", actualFlightFare.get().getFare(), equalTo(29400));

    }

    /**
     * Test for calcFare(). <br>
     * 正常ケース３。 <br>
     * 適用可能割引なし。
     */
    @Test
    public void testCalcFareCorrect3() {

        FareCalcInfo input = FareCalcInfo.builder().departureAirportId("HND").arrivalAirportId("OSA")
                .basicFare(30000).flightType(FlightType.OW).seatClass(SeatClass.N).totalPassengers(1)
                .travelDate(LocalDate.now().plusDays(1))//
                .discountId(DiscountType.GROUP.getDiscountId())//
                .build();

        doReturn(Optional.of(SeatClassCharge.builder().charge(0).build())).when(seatClassChargeRepository)
                .findById(any(SeatClass.class));

        doReturn(Arrays.asList(PeakRatio.builder().fromDate(LocalDate.now().withYear(1901).minusDays(1))
                .toDate(LocalDate.now().withYear(1901).plusDays(1)).ratio(140).build())).when(peakRatioRepository)
                        .findAll();

        List<FlightFareInfo> actualList = flightTicketFareCalculationService.calcFare(input);
        log.info("** {}", actualList);

        assertThat("運賃の件数が正しいこと", actualList.size(), equalTo(0));

    }
}
