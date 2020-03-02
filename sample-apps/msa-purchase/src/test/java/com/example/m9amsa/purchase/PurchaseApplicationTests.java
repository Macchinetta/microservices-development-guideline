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
package com.example.m9amsa.purchase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.m9amsa.purchase.config.OAuthHelper;
import com.example.m9amsa.purchase.constant.Gender;
import com.example.m9amsa.purchase.constant.SeatClass;
import com.example.m9amsa.purchase.entity.Purchase;
import com.example.m9amsa.purchase.entity.PurchaseRepository;
import com.example.m9amsa.purchase.model.CardInfo;
import com.example.m9amsa.purchase.model.MemberInfo;
import com.example.m9amsa.purchase.model.PassengerInfo;
import com.example.m9amsa.purchase.model.ReserveInfo;
import com.example.m9amsa.purchase.service.FindPurchaseService;
import com.example.m9amsa.purchase.service.RegisterPurchaseService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_PURCHASE=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
@EnableResourceServer
@Slf4j
public class PurchaseApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @SpyBean
    private RegisterPurchaseService registerPurchaseService;

    @SpyBean
    private FindPurchaseService findPurchaseService;

    @Value("${info.url.root-path}")
    private String urlRoot;

    @Autowired
    private OAuthHelper oauthHelper;

    @Before
    public void before() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();
    }

    /**
     * Test for registerPurchase.
     * 
     * @throws Exception
     */
    @Test
    public void testRegisterPurchase() throws Exception {

        ReserveInfo reserveInfo = createReserveInfo(1L);

        String json = jsonMapper.writeValueAsString(reserveInfo);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post(String.format("/%s/purchase/register", urlRoot)).with(postProcessor) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8))//
                .andExpect(status().isOk());

        List<Purchase> purchases = purchaseRepository.findAll();
        assertThat("購入情報の登録件数は1件であること", purchases.size(), equalTo(1));

        Purchase purchase = purchases.get(0);
        assertNotNull("決済情報が登録されたこと", purchase.getPayment());
    }

    /**
     * Test for registerPurchase.
     * 
     * 
     * <pre>
     * Error pattern.
     * - Http Status 400
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testRegisterPurchase400() throws Exception {

        ReserveInfo reserveInfo = ReserveInfo.builder().build();

        String json = jsonMapper.writeValueAsString(reserveInfo);

        doThrow(new RuntimeException()).when(registerPurchaseService).registerPurchase(any(ReserveInfo.class));
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post(String.format("/%s/purchase/register", urlRoot)).with(postProcessor) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8))//
                .andExpect(status().is(400));
    }

    /**
     * Test for registerPurchase.
     * 
     * 
     * <pre>
     * Error pattern.
     * - Http Status 401
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testRegisterPurchase401() throws Exception {

        ReserveInfo reserveInfo = createReserveInfo(1L);

        String json = jsonMapper.writeValueAsString(reserveInfo);
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post(String.format("/%s/purchase/register", urlRoot)) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8))//
                .andExpect(status().is(401));

    }

    /**
     * Test for registerPurchase.
     * 
     * 
     * <pre>
     * Error pattern.
     * - Http Status 500
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testRegisterPurchase500() throws Exception {

        ReserveInfo reserveInfo = createReserveInfo(1L);

        String json = jsonMapper.writeValueAsString(reserveInfo);

        doThrow(new RuntimeException("testRegisterPurchase500")).when(registerPurchaseService)
                .registerPurchase(any(ReserveInfo.class));
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post(String.format("/%s/purchase/register", urlRoot)).with(postProcessor) //
                        .content(json).accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON_UTF8))//
                .andExpect(status().is(500));
    }

    /**
     * Test for findPurchase.
     * 
     * @throws Exception
     */
    @Test
    public void testFindPurchase() throws Exception {

        Long reserveId = 1L;
        Long memberId = 1L;
        ReserveInfo reserveInfo = createReserveInfo(reserveId, memberId);
        registerPurchaseService.registerPurchase(reserveInfo);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken(memberId.toString(), "USER");

        MvcResult mvcResult = mvc.perform( //
                MockMvcRequestBuilders //
                        .get(String.format("/%s/purchase/find/%s", urlRoot, reserveId)) //
                        .accept(MediaType.APPLICATION_JSON_UTF8)//
                        .contentType(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)) //
                .andExpect(status().isOk())//
                .andReturn();

        Purchase purchase = jsonMapper.readValue(mvcResult.getResponse().getContentAsString(), Purchase.class);
        log.info("決済情報:{}", purchase);
        assertThat("購入情報が返却されること", reserveId, equalTo(purchase.getReserveId()));
    }

    /**
     * Test for findPurchase.
     * 
     * <pre>
     * Error pattern.
     * - Http Status 400
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testFindPurchase400() throws Exception {

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("1", "USER");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .get(String.format("/%s/purchase/find/%s", urlRoot, "aaa")) //
                        .accept(MediaType.APPLICATION_JSON_UTF8)//
                        .contentType(MediaType.APPLICATION_JSON_UTF8).with(postProcessor)//
        ) //
                .andExpect(status().is(400));
    }

    /**
     * Test for findPurchase.
     * 
     * <pre>
     * Error pattern.
     * - Http Status 401
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testFindPurchase401() throws Exception {

        Long reserveId = 1L;
        Long memberId = 1L;
        ReserveInfo reserveInfo = createReserveInfo(reserveId, memberId);
        registerPurchaseService.registerPurchase(reserveInfo);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("2", "USER");

        mvc.perform( //
                MockMvcRequestBuilders //
                        .get(String.format("/%s/purchase/find/%s", urlRoot, reserveId)) //
                        .accept(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)) //
                .andExpect(status().is(401));

    }

    /**
     * Test for findPurchase.
     * 
     * <pre>
     * Error pattern.
     * - Http Status 404
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testFindPurchase404() throws Exception {

        Long reserveId = 1L;
        purchaseRepository.delete(Purchase.builder().reserveId(reserveId).build());

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("1", "USER");

        mvc.perform( //
                MockMvcRequestBuilders //
                        .get(String.format("/%s/purchase/find/%s", urlRoot, reserveId)) //
                        .accept(MediaType.APPLICATION_JSON_UTF8)//
                        .contentType(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)) //
                .andExpect(status().is(404));
    }

    /**
     * Test for findPurchase.
     * 
     * <pre>
     * Error pattern.
     * - Http Status 500
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void testFindPurchase500() throws Exception {

        Long reserveId = 1L;
        ReserveInfo reserveInfo = createReserveInfo(reserveId);
        registerPurchaseService.registerPurchase(reserveInfo);

        when(findPurchaseService.findPurchase(reserveId)).thenThrow(new RuntimeException("Something error"));

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("1", "USER");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .get(String.format("/%s/purchase/find/%s", urlRoot, reserveId)) //
                        .accept(MediaType.APPLICATION_JSON_UTF8)//
                        .with(postProcessor)) //
                .andExpect(status().is(500));
    }

    /**
     * 予約情報を作成します（会員Id：1L）。
     * 
     * @param reserveId 予約Id
     * 
     * @return 予約情報。
     */
    private ReserveInfo createReserveInfo(Long reserveId) {
        return createReserveInfo(reserveId, 1L);
    }

    /**
     * 予約情報を作成します。
     * 
     * @param reserveId 予約Id
     * @param memberId  会員Id
     * 
     * @return 予約情報。
     */
    private ReserveInfo createReserveInfo(Long reserveId, Long memberId) {

        // 予約情報
        ReserveInfo reserveInfo = ReserveInfo.builder() //
                .reserveId(reserveId) //
                .departureDate(LocalDate.now()) //
                .flightId("NTT001") //
                .departureTime(LocalTime.now()) //
                .arrivalTime(LocalTime.now()) //
                .departureAirportId("HND") //
                .arrivalAirportId("CTS") //
                .seatClass(SeatClass.N) //
                .fareType("basicFare") //
                .fare(30000) //
                .passengers(Arrays.asList(new PassengerInfo[] //
                { //
                        PassengerInfo.builder().name("渡辺 太郎").age(20).telephoneNo("01012345678")
                                .email("sample2@example.com").isMainPassenger(true).build()//
                        ,
                        PassengerInfo.builder().name("搭乗者２").age(22).telephoneNo("01012345678")
                                .email("sample2@example.com").isMainPassenger(false).build() })) //
                .build();

        PassengerInfo passengerInfo = reserveInfo.getPassengers().get(0);
        MemberInfo memberInfo = MemberInfo.builder() //
                .memberId(memberId)//
                .surname("渡辺") //
                .firstName("太郎") //
                .surnameKana("ワタナベ")//
                .firstNameKana("タロウ") //
                .birthday(LocalDate.of(1999, 9, 12)) //
                .gender(Gender.Male)//
                .telephoneNo(passengerInfo.getTelephoneNo())//
                .postalCode("0000000")//
                .address("東京都中央区") //
                .emailId(passengerInfo.getEmail())//
                .build();

        memberInfo.setCard(Optional.of(//
                CardInfo.builder()//
                        .cardNo("1234567890123456")//
                        .cardCompanyCode("VIS")//
                        .cardCompanyName("VISA")//
                        .validTillMonth(12)//
                        .validTillYear(23)//
                        .build()));
        reserveInfo.setPurchaseMember(Optional.of(memberInfo));

        return reserveInfo;
    }

}
