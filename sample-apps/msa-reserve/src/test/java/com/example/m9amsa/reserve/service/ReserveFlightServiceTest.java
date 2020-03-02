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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.reserve.constant.SeatClass;
import com.example.m9amsa.reserve.entity.Passenger;
import com.example.m9amsa.reserve.entity.Reservation;
import com.example.m9amsa.reserve.entity.ReservationRepository;
import com.example.m9amsa.reserve.externalmicroservice.model.PurchaseInfoForEx;
import com.example.m9amsa.reserve.model.PassengerInfoModel;
import com.example.m9amsa.reserve.model.ReservationRequest;
import com.example.m9amsa.reserve.model.topic.PassengerTopic;
import com.example.m9amsa.reserve.model.topic.ReservationTopic;
import com.example.m9amsa.reserve.model.topic.ReservationTopicSource;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "HOSTNAME_CALCULATE_FARE=localhost:28080",
        "HOSTNAME_FLIGHT=localhost:28081", "HOSTNAME_PURCHASE=localhost:28082", "DB_HOSTNAME_RESERVE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_ACCOUNT=localhost", "OAUTH2_CLIENT_ID=my-client", "OAUTH2_CLIENT_SECRET=1" })
public class ReserveFlightServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ReservationTopicSource reserveTopic;

    @InjectMocks
    ReserveFlightService service;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * reserveFlight()のテスト
     * 
     * @throws Exception
     */
    @Test
    public void testReserveFlight01() throws Exception {
        // when
        List<PassengerInfoModel> passengerModels = new ArrayList<PassengerInfoModel>();
        ReservationRequest reservationRequest = ReservationRequest.builder().flightId("TEST001")
                .departureDate(LocalDate.now()).departureTime(LocalTime.of(8, 0)).arrivalTime(LocalTime.of(12, 0))
                .departureAirportId("HND").arrivalAirportId("KIX").seatClass(SeatClass.N).fareType("早期割").fare(10520)
                .passengers(passengerModels).build();

        passengerModels.add(PassengerInfoModel.builder().name("代表").age(20).telephoneNo("080-1234-5678")
                .email("abc@email.com").isMainPassenger(true).build());
        passengerModels.add(PassengerInfoModel.builder().name("同時１").age(21).isMainPassenger(false).build());
        passengerModels.add(PassengerInfoModel.builder().name("同時２").age(22).isMainPassenger(false).build());

        ArgumentCaptor<Reservation> captureReservationInfo = ArgumentCaptor.forClass(Reservation.class);
        when(reservationRepository.save(captureReservationInfo.capture())).thenReturn(null);

        Reservation reservationInfo = Reservation.builder().reserveTime(LocalDateTime.of(2019, 10, 16, 15, 55))
                .departureDate(reservationRequest.getDepartureDate()).flightId(reservationRequest.getFlightId())
                .departureTime(reservationRequest.getDepartureTime()).arrivalTime(reservationRequest.getArrivalTime())
                .departureAirportId(reservationRequest.getDepartureAirportId())
                .arrivalAirportId(reservationRequest.getArrivalAirportId()).seatClass(reservationRequest.getSeatClass())
                .fareType(reservationRequest.getFareType()).fare(reservationRequest.getFare())
                .subPassengers(new ArrayList<>()).build();

        Passenger mainPassengerInfo = Passenger.builder().build();
        BeanUtils.copyProperties(passengerModels.get(0), mainPassengerInfo);
        reservationInfo.setMainPassenger(mainPassengerInfo);

        Passenger subPassengerInfo1 = Passenger.builder().build();
        BeanUtils.copyProperties(passengerModels.get(1), subPassengerInfo1);
        reservationInfo.getSubPassengers().add(subPassengerInfo1);

        Passenger subPassengerInfo2 = Passenger.builder().build();
        BeanUtils.copyProperties(passengerModels.get(2), subPassengerInfo2);
        reservationInfo.getSubPassengers().add(subPassengerInfo2);

        PurchaseInfoForEx purchaseInfo = PurchaseInfoForEx.builder().departureDate(reservationRequest.getDepartureDate())
                .flightId(reservationRequest.getFlightId()).departureTime(reservationRequest.getDepartureTime())
                .arrivalTime(reservationRequest.getArrivalTime())
                .departureAirportId(reservationRequest.getDepartureAirportId())
                .arrivalAirportId(reservationRequest.getArrivalAirportId()).seatClass(reservationRequest.getSeatClass())
                .fareType(reservationRequest.getFareType()).fare(reservationRequest.getFare())
                .passengers(new ArrayList<>()).build();
        purchaseInfo.getPassengers()
                .add(Passenger.builder().name(passengerModels.get(0).getName()).age(passengerModels.get(0).getAge())
                        .telephoneNo(passengerModels.get(0).getTelephoneNo()).email(passengerModels.get(0).getEmail())
                        .build());
        purchaseInfo.getPassengers().add(Passenger.builder().name(passengerModels.get(1).getName())
                .age(passengerModels.get(1).getAge()).build());
        purchaseInfo.getPassengers().add(Passenger.builder().name(passengerModels.get(2).getName())
                .age(passengerModels.get(2).getAge()).build());

        MessageChannel messageChannel = mock(MessageChannel.class);
        when(reserveTopic.output()).thenReturn(messageChannel);
        ArgumentCaptor<Message<?>> captureMessageReservationInfo = ArgumentCaptor.forClass(Message.class);
        when(messageChannel.send(captureMessageReservationInfo.capture())).thenReturn(true);

        ReservationTopic expMessageReservationTopic = ReservationTopic.builder().reserveTime(LocalDateTime.now())
                .departureDate(reservationRequest.getDepartureDate()).flightId(reservationRequest.getFlightId())
                .departureTime(reservationRequest.getDepartureTime()).arrivalTime(reservationRequest.getArrivalTime())
                .departureAirportId(reservationRequest.getDepartureAirportId())
                .arrivalAirportId(reservationRequest.getArrivalAirportId()).seatClass(reservationRequest.getSeatClass())
                .fareType(reservationRequest.getFareType()).fare(reservationRequest.getFare())
                .passenger(new ArrayList<>()).build();
        expMessageReservationTopic.getPassenger()
                .add(PassengerTopic.builder().name(passengerModels.get(0).getName())
                        .age(passengerModels.get(0).getAge()).telephoneNo(passengerModels.get(0).getTelephoneNo())
                        .email(passengerModels.get(0).getEmail()).build());
        expMessageReservationTopic.getPassenger().add(PassengerTopic.builder().name(passengerModels.get(1).getName())
                .age(passengerModels.get(1).getAge()).build());
        expMessageReservationTopic.getPassenger().add(PassengerTopic.builder().name(passengerModels.get(2).getName())
                .age(passengerModels.get(2).getAge()).build());

        // do
        service.registerReservationInfo(reservationInfo, purchaseInfo);

        // verify

        // 予約情報登録が呼ばれること
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        Reservation actualReservationInfo = captureReservationInfo.getValue();
        assertThat("予約情報登録の引数", actualReservationInfo, equalTo(reservationInfo));

        // 予約情報トピックが通知されること
        verify(reserveTopic, times(1)).output();
        verify(messageChannel, times(1)).send(any(Message.class));
        expMessageReservationTopic.setReserveTime(actualReservationInfo.getReserveTime()); // LocalDateTime.now()なので、予想できない
        @SuppressWarnings("unchecked")
        Message<ReservationTopic> actualMessage = (Message<ReservationTopic>) captureMessageReservationInfo.getValue();

        ReservationTopic actualMessageReservationTopic = actualMessage.getPayload();

        assertThat("Message<ReservationTopic>.reserveId", actualMessageReservationTopic.getReserveId(),
                equalTo(expMessageReservationTopic.getReserveId()));
        assertThat("Message<ReservationTopic>.reserveTime", actualMessageReservationTopic.getReserveTime(),
                equalTo(expMessageReservationTopic.getReserveTime()));
        assertThat("Message<ReservationTopic>.departureDate", actualMessageReservationTopic.getDepartureDate(),
                equalTo(expMessageReservationTopic.getDepartureDate()));
        assertThat("Message<ReservationTopic>.flightId", actualMessageReservationTopic.getFlightId(),
                equalTo(expMessageReservationTopic.getFlightId()));
        assertThat("Message<ReservationTopic>.departureTime", actualMessageReservationTopic.getDepartureTime(),
                equalTo(expMessageReservationTopic.getDepartureTime()));
        assertThat("Message<ReservationTopic>.arrivalTime", actualMessageReservationTopic.getArrivalTime(),
                equalTo(expMessageReservationTopic.getArrivalTime()));
        assertThat("Message<ReservationTopic>.departureAirportId",
                actualMessageReservationTopic.getDepartureAirportId(),
                equalTo(expMessageReservationTopic.getDepartureAirportId()));
        assertThat("Message<ReservationTopic>.arrivalAirportId", actualMessageReservationTopic.getArrivalAirportId(),
                equalTo(expMessageReservationTopic.getArrivalAirportId()));
        assertThat("Message<ReservationTopic>.seatClass", actualMessageReservationTopic.getSeatClass(),
                equalTo(expMessageReservationTopic.getSeatClass()));
        assertThat("Message<ReservationTopic>.fareType", actualMessageReservationTopic.getFareType(),
                equalTo(expMessageReservationTopic.getFareType()));
        assertThat("Message<ReservationTopic>.fare", actualMessageReservationTopic.getFare(),
                equalTo(expMessageReservationTopic.getFare()));

        assertThat("Message<ReservationTopic>.passengers.0.name",
                actualMessageReservationTopic.getPassenger().get(0).getName(),
                equalTo(expMessageReservationTopic.getPassenger().get(0).getName()));
        assertThat("Message<ReservationTopic>.passengers.0.age",
                actualMessageReservationTopic.getPassenger().get(0).getAge(),
                equalTo(expMessageReservationTopic.getPassenger().get(0).getAge()));
        assertThat("Message<ReservationTopic>.passengers.0.telephoneNo",
                actualMessageReservationTopic.getPassenger().get(0).getTelephoneNo(),
                equalTo(expMessageReservationTopic.getPassenger().get(0).getTelephoneNo()));
        assertThat("Message<ReservationTopic>.passengers.0.email",
                actualMessageReservationTopic.getPassenger().get(0).getEmail(),
                equalTo(expMessageReservationTopic.getPassenger().get(0).getEmail()));
        assertThat("Message<ReservationTopic>.passengers.1.name",
                actualMessageReservationTopic.getPassenger().get(1).getName(),
                equalTo(expMessageReservationTopic.getPassenger().get(1).getName()));
        assertThat("Message<ReservationTopic>.passengers.1.age",
                actualMessageReservationTopic.getPassenger().get(1).getAge(),
                equalTo(expMessageReservationTopic.getPassenger().get(1).getAge()));
        assertThat("Message<ReservationTopic>.passengers.1.telephoneNo",
                actualMessageReservationTopic.getPassenger().get(1).getTelephoneNo(),
                equalTo(expMessageReservationTopic.getPassenger().get(1).getTelephoneNo()));
        assertThat("Message<ReservationTopic>.passengers.1.email",
                actualMessageReservationTopic.getPassenger().get(1).getEmail(),
                equalTo(expMessageReservationTopic.getPassenger().get(1).getEmail()));
        assertThat("Message<ReservationTopic>.passengers.2.name",
                actualMessageReservationTopic.getPassenger().get(2).getName(),
                equalTo(expMessageReservationTopic.getPassenger().get(2).getName()));
        assertThat("Message<ReservationTopic>.passengers.2.age",
                actualMessageReservationTopic.getPassenger().get(2).getAge(),
                equalTo(expMessageReservationTopic.getPassenger().get(2).getAge()));
        assertThat("Message<ReservationTopic>.passengers.2.telephoneNo",
                actualMessageReservationTopic.getPassenger().get(2).getTelephoneNo(),
                equalTo(expMessageReservationTopic.getPassenger().get(2).getTelephoneNo()));
        assertThat("Message<ReservationTopic>.passengers.2.email",
                actualMessageReservationTopic.getPassenger().get(2).getEmail(),
                equalTo(expMessageReservationTopic.getPassenger().get(2).getEmail()));
    }
}
