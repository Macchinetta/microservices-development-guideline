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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.example.m9amsa.reserve.constant.Gender;
import com.example.m9amsa.reserve.constant.SeatClass;
import com.example.m9amsa.reserve.entity.Passenger;
import com.example.m9amsa.reserve.entity.Reservation;
import com.example.m9amsa.reserve.externalmicroservice.model.CardForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.MemberForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.PurchaseInfoForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.ReserveVacantSeatForEx;
import com.example.m9amsa.reserve.externalmicroservice.model.VacantSeatForEx;
import com.example.m9amsa.reserve.externalmicroservice.service.FlightExternalMicroService;
import com.example.m9amsa.reserve.externalmicroservice.service.PurchaseExternalMicroService;
import com.example.m9amsa.reserve.model.CardInfo;
import com.example.m9amsa.reserve.model.MemberInfo;
import com.example.m9amsa.reserve.model.PassengerInfoModel;
import com.example.m9amsa.reserve.model.ReservationRequest;
import com.example.m9amsa.reserve.service.ReserveFlightService;

import feign.FeignException;
import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Span;
import io.opentracing.Tracer;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "HOSTNAME_CALCULATE_FARE=localhost:28080",
        "HOSTNAME_FLIGHT=localhost:28081", "HOSTNAME_PURCHASE=localhost:28082", "DB_HOSTNAME_RESERVE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_ACCOUNT=localhost", "OAUTH2_CLIENT_ID=my-client", "OAUTH2_CLIENT_SECRET=1" })
public class ReserveFlightControllerTest {

    @Mock
    ReserveFlightService reserveFlightService;

    @Mock
    private FlightExternalMicroService flightExternalMicroService;

    @Mock
    private PurchaseExternalMicroService purchaseExternalMicroService;

    @SpyBean
    private ReservationHelper reservationHelper;

    @SpyBean
    private Tracer tracer;

    @InjectMocks
    ReserveFlightController controller;

    @Captor
    ArgumentCaptor<Reservation> reservationInfoCaptor;

    @Captor
    ArgumentCaptor<ReserveVacantSeatForEx> reserveVacantSeatCaptor;

    @Captor
    ArgumentCaptor<PurchaseInfoForEx> purchaseInfoCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test for reserveFlight()
     * 
     * <pre>
     * 正常系のみ
     * </pre>
     */
    @Test
    public void testReserveFlight() throws Exception {
        WebRequest requestMock = new ServletWebRequest(new MockHttpServletRequest());

        ReservationRequest reservationRequest = createReservationRequest();

        Long reserveId = 12345678L;

        Span activeSpan = mock(Span.class);
        when(tracer.activeSpan()).thenReturn(activeSpan);
        JaegerSpanContext context = mock(JaegerSpanContext.class);
        when(activeSpan.context()).thenReturn(context);
        when(context.getTraceId()).thenReturn(Long.toHexString(reserveId));

        // do
        controller.reserveFlight(reservationRequest, requestMock);

        // verify
        verify(flightExternalMicroService).secureVacantSeat(reserveVacantSeatCaptor.capture());

        verify(purchaseExternalMicroService).registerPurchaseInfo(purchaseInfoCaptor.capture());

        verify(reserveFlightService).registerReservationInfo(reservationInfoCaptor.capture(),
                purchaseInfoCaptor.capture());

        // flightExternalMicroService
        ReserveVacantSeatForEx resultSeatForEx = reserveVacantSeatCaptor.getValue();
        assertThat("flightExternalMicroServiceのパラメータが正しいこと:departureDate", resultSeatForEx.getDepartureDate(),
                equalTo(reservationRequest.getDepartureDate()));
        assertThat("flightExternalMicroServiceのパラメータが正しいこと:flightName", resultSeatForEx.getFlightName(),
                equalTo(reservationRequest.getFlightId()));
        assertThat("flightExternalMicroServiceのパラメータが正しいこと:seatClass", resultSeatForEx.getSeatClass(),
                equalTo(reservationRequest.getSeatClass()));
        assertThat("flightExternalMicroServiceのパラメータが正しいこと:reserveId", resultSeatForEx.getReserveId(),
                equalTo(reserveId));

        // purchaseExternalMicroService
        PurchaseInfoForEx resultPurchaseForEx = purchaseInfoCaptor.getAllValues().get(0);
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:arraivalAirport", resultPurchaseForEx.getArrivalAirportId(),
                equalTo(reservationRequest.getArrivalAirportId()));
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:arrivalTime", resultPurchaseForEx.getArrivalTime(),
                equalTo(reservationRequest.getArrivalTime()));
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:departureAirport",
                resultPurchaseForEx.getDepartureAirportId(), equalTo(reservationRequest.getDepartureAirportId()));
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:departureDate", resultPurchaseForEx.getDepartureDate(),
                equalTo(reservationRequest.getDepartureDate()));
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:departureTime", resultPurchaseForEx.getDepartureTime(),
                equalTo(reservationRequest.getDepartureTime()));
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:fareType", resultPurchaseForEx.getFareType(),
                equalTo(reservationRequest.getFareType()));
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:fare", resultPurchaseForEx.getFare(),
                equalTo(reservationRequest.getFare()));
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:flightId", resultPurchaseForEx.getFlightId(),
                equalTo(reservationRequest.getFlightId()));
        List<Passenger> passengerInfoList = createExpectedPassengerInfo(reservationRequest.getPassengers());
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:passengers", resultPurchaseForEx.getPassengers(),
                equalTo(passengerInfoList));
        Optional<MemberForEx> expMemberForEx = reservationRequest.getMemberInfo().map(m -> {
            MemberForEx memberForEx = new MemberForEx();
            BeanUtils.copyProperties(m, memberForEx, "card");
            memberForEx.setCard(m.getCard().map(c -> {
                CardForEx cardForEx = new CardForEx();
                BeanUtils.copyProperties(c, cardForEx);
                return cardForEx;
            }));
            return memberForEx;
        });
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:purchaseMember", resultPurchaseForEx.getPurchaseMember(),
                equalTo(expMemberForEx));
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:seatClass", resultPurchaseForEx.getSeatClass(),
                equalTo(reservationRequest.getSeatClass()));
        assertThat("purchaseExternalMicroServiceのパラメータが正しいこと:reserveId", resultPurchaseForEx.getReserveId(),
                equalTo(reserveId));

        // reserveFlightService
        Reservation reseltReserve = reservationInfoCaptor.getValue();
        assertThat("reserveFlightServiceへのパラメータが正しいこと:flightId", reseltReserve.getFlightId(),
                equalTo(reservationRequest.getFlightId()));
        assertThat("reserveFlightServiceへのパラメータが正しいこと:arrivalAirport", reseltReserve.getArrivalAirportId(),
                equalTo(reservationRequest.getArrivalAirportId()));
        assertThat("reserveFlightServiceへのパラメータが正しいこと:arrivalTime", reseltReserve.getArrivalTime(),
                equalTo(reservationRequest.getArrivalTime()));
        assertThat("reserveFlightServiceへのパラメータが正しいこと:departureAirport", reseltReserve.getDepartureAirportId(),
                equalTo(reservationRequest.getDepartureAirportId()));
        assertThat("reserveFlightServiceへのパラメータが正しいこと:departureDate", reseltReserve.getDepartureDate(),
                equalTo(reservationRequest.getDepartureDate()));
        assertThat("reserveFlightServiceへのパラメータが正しいこと:departureTime", reseltReserve.getDepartureTime(),
                equalTo(reservationRequest.getDepartureTime()));
        assertThat("reserveFlightServiceへのパラメータが正しいこと:fare", reseltReserve.getFare(),
                equalTo(reservationRequest.getFare()));
        assertThat("reserveFlightServiceへのパラメータが正しいこと:fareType", reseltReserve.getFareType(),
                equalTo(reservationRequest.getFareType()));
        assertThat("reserveFlightServiceへのパラメータが正しいこと:mainPassenger", reseltReserve.getMainPassenger(),
                equalTo(passengerInfoList.get(0)));
        assertThat("reserveFlightServiceへのパラメータが正しいこと:seartClass", reseltReserve.getSeatClass(),
                equalTo(reservationRequest.getSeatClass()));
        assertThat("reserveFlightServiceへのパラメータが正しいこと:subPassengers.size", reseltReserve.getSubPassengers().size(),
                equalTo(1));
        Passenger expectedPassengerInfo = new Passenger();
        BeanUtils.copyProperties(reservationRequest.getPassengers().get(1), expectedPassengerInfo);
        assertThat("reserveFlightServiceへのパラメータが正しいこと:subPassengers", reseltReserve.getSubPassengers().get(0),
                equalTo(expectedPassengerInfo));
        assertThat("reserveFlightServiceへのパラメータが正しいこと:reserveId", reseltReserve.getReserveId(), equalTo(reserveId));

        assertThat("reserveFlightServiceへのパラメータが正しいこと:purchaseInfo", resultPurchaseForEx,
                equalTo(purchaseInfoCaptor.getAllValues().get(1)));

        @SuppressWarnings("unchecked")
        List<Runnable> compensations = (List<Runnable>) requestMock.getAttribute("compensations",
                RequestAttributes.SCOPE_REQUEST);
        assertThat("正常終了時の補償トランザクションリストは2件", compensations.size(), equalTo(2));
        // saga処理の実行確認等はReserveApplicationTestsで実施
    }

    /**
     * Test for reserveFlight()
     * 
     * <pre>
     * エラーケース
     * </pre>
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testReserveFlightAbnormal() throws Exception {
        WebRequest requestMock = new ServletWebRequest(new MockHttpServletRequest());

        ReservationRequest reservationRequest = createReservationRequest();
        Long reserveId = 12345678L;

        Span activeSpan = mock(Span.class);
        when(tracer.activeSpan()).thenReturn(activeSpan);
        JaegerSpanContext context = mock(JaegerSpanContext.class);
        when(activeSpan.context()).thenReturn(context);
        when(context.getTraceId()).thenReturn(Long.toHexString(reserveId));

        // 空席確保で例外
        when(flightExternalMicroService.secureVacantSeat(any(ReserveVacantSeatForEx.class)))
                .thenThrow(FeignException.class);
        try {
            controller.reserveFlight(reservationRequest, requestMock);
            fail("正常終了したらテスト失敗");
        } catch (Exception e) {
            assertThat("Feignで例外が発生する設定", e.getClass(), equalTo(FeignException.class));
            List<Runnable> compensations = (List<Runnable>) requestMock.getAttribute("compensations",
                    RequestAttributes.SCOPE_REQUEST);
            assertThat("空席確保で例外が発生した場合、SAGA処理は無し", compensations.size(), equalTo(0));
        }

        // 購入で例外
        when(flightExternalMicroService.secureVacantSeat(any(ReserveVacantSeatForEx.class)))
                .thenReturn(new VacantSeatForEx());
        doThrow(FeignException.class).when(purchaseExternalMicroService)
                .registerPurchaseInfo(any(PurchaseInfoForEx.class));
        try {
            controller.reserveFlight(reservationRequest, requestMock);
            fail("正常終了したらテスト失敗");
        } catch (Exception e) {
            assertThat("Feignで例外が発生する設定", e.getClass(), equalTo(FeignException.class));
            List<Runnable> compensations = (List<Runnable>) requestMock.getAttribute("compensations",
                    RequestAttributes.SCOPE_REQUEST);
            assertThat("購入で例外が発生した場合、SAGA処理は1件", compensations.size(), equalTo(1));
        }

        // ローカル処理で例外
        when(flightExternalMicroService.secureVacantSeat(any(ReserveVacantSeatForEx.class)))
                .thenReturn(new VacantSeatForEx());
        doNothing().when(purchaseExternalMicroService).registerPurchaseInfo(any(PurchaseInfoForEx.class));
        doThrow(RuntimeException.class).when(reserveFlightService).registerReservationInfo(any(Reservation.class),
                any(PurchaseInfoForEx.class));
        try {
            controller.reserveFlight(reservationRequest, requestMock);
            fail("正常終了したらテスト失敗");
        } catch (Exception e) {
            assertThat("ローカル処理で何らかの例外が発生", e.getClass(), equalTo(RuntimeException.class));
            List<Runnable> compensations = (List<Runnable>) requestMock.getAttribute("compensations",
                    RequestAttributes.SCOPE_REQUEST);
            assertThat("ローカル処理で例外が発生した場合、SAGA処理は2件", compensations.size(), equalTo(2));
        }
    }

    private ReservationRequest createReservationRequest() {
        LocalDate departureDate = LocalDate.of(2019, 10, 16);
        LocalTime departureTime = LocalTime.of(12, 0);
        LocalTime arrivalTime = LocalTime.of(14, 0);

        PassengerInfoModel mainPassenger = PassengerInfoModel.builder().age(30).email("main@email.com")
                .isMainPassenger(true).name("main").telephoneNo("09012345678").build();
        PassengerInfoModel subPassenger = PassengerInfoModel.builder().age(31).email("sub@email.com")
                .isMainPassenger(false).name("sub").telephoneNo("09087654321").build();
        List<PassengerInfoModel> passengerList = Arrays.asList(mainPassenger, subPassenger);

        MemberInfo memberInfo = MemberInfo.builder().address("東京都").birthday(LocalDate.of(1999, 10, 16))
                .card(Optional.of(CardInfo.builder().cardCompanyCode("JCB").cardNo("card-number").validTillMonth("01")
                        .validTillYear("2022").build()))
                .emailId("mail@mail.com").firstName("花子").firstNameKana("ハナコ").gender(Gender.Female).memberId(1234L)
                .postalCode("東京都").surname("山田").surnameKana("ヤマダ").telephoneNo("09012345678").build();

        return ReservationRequest.builder().flightId("NTT01").departureDate(departureDate).departureTime(departureTime)
                .arrivalTime(arrivalTime).departureAirportId("HND").arrivalAirportId("KIX").seatClass(SeatClass.N)
                .fareType("test").fare(20000).passengers(passengerList).memberInfo(Optional.of(memberInfo)).build();
    }

    private List<Passenger> createExpectedPassengerInfo(List<PassengerInfoModel> modelList) {
        return modelList.stream().map(this::toPassengerInfo).collect(Collectors.toList());
    }

    private Passenger toPassengerInfo(PassengerInfoModel passengerInfoModel) {
        Passenger passengerInfo = new Passenger();
        BeanUtils.copyProperties(passengerInfoModel, passengerInfo);
        return passengerInfo;

    }
}
