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
package com.example.m9amsa.flight;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.m9amsa.flight.config.OAuthHelper;
import com.example.m9amsa.flight.constant.SeatClass;
import com.example.m9amsa.flight.controller.AirportController;
import com.example.m9amsa.flight.entity.Airplane;
import com.example.m9amsa.flight.entity.AirplaneRepository;
import com.example.m9amsa.flight.entity.Airport;
import com.example.m9amsa.flight.entity.AirportRepository;
import com.example.m9amsa.flight.entity.BasicFare;
import com.example.m9amsa.flight.entity.BasicFarePk;
import com.example.m9amsa.flight.entity.BasicFareRepository;
import com.example.m9amsa.flight.entity.Flight;
import com.example.m9amsa.flight.entity.FlightRepository;
import com.example.m9amsa.flight.entity.ReserveVacantSeat;
import com.example.m9amsa.flight.entity.ReserveVacantSeatRepository;
import com.example.m9amsa.flight.entity.VacantSeat;
import com.example.m9amsa.flight.entity.VacantSeatPk;
import com.example.m9amsa.flight.entity.VacantSeatRepository;
import com.example.m9amsa.flight.model.AirplaneInfo;
import com.example.m9amsa.flight.model.AirportInfo;
import com.example.m9amsa.flight.model.BasicFareInfo;
import com.example.m9amsa.flight.model.FlightUpdateInfo;
import com.example.m9amsa.flight.model.ReserveVacantSeatInfo;
import com.example.m9amsa.flight.model.topic.AirplaneTopic;
import com.example.m9amsa.flight.model.topic.AirportTopic;
import com.example.m9amsa.flight.model.topic.BasicFareTopic;
import com.example.m9amsa.flight.model.topic.FlightTopic;
import com.example.m9amsa.flight.model.topic.FlightTopicSource;
import com.example.m9amsa.flight.model.topic.FlightVacantSeatTopic;
import com.example.m9amsa.flight.service.AirplaneService;
import com.example.m9amsa.flight.service.AirportService;
import com.example.m9amsa.flight.service.BasicFareService;
import com.example.m9amsa.flight.service.CancelReservedSeatService;
import com.example.m9amsa.flight.service.FlightService;
import com.example.m9amsa.flight.service.ReserveVacantSeatService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // 途中、レコードの更新情報を再利用するため名前順に実行
@EnableResourceServer
@Slf4j
public class FlightApplicationTests {

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private AirportService airportService;

    @SpyBean
    private AirplaneService airplaneService;

    @SpyBean
    private BasicFareService basicFareService;

    @SpyBean
    private FlightService flightService;

    @Autowired
    private OAuthHelper oauthHelper;

    @SpyBean
    private ReserveVacantSeatService reserveVacantSeatService;

    @SpyBean
    private CancelReservedSeatService cancelReservedSeatService;

    @InjectMocks
    private AirportController airportController;

    @Value("${info.url.root-path}")
    private String urlRoot;

    @Autowired
    private AirplaneRepository airplaneRepository;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private BasicFareRepository basicFareRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private ReserveVacantSeatRepository reserveVacantSeatRepository;

    @Autowired
    private VacantSeatRepository vacantSeatRepository;

    @Autowired
    private FlightTopicSource flightTopicSource;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private ObjectMapper jsonMapper;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(airportService);
        reset(airplaneService);
        reset(basicFareService);
        reset(flightService);

    }

    private void deleteAll() {
        flightRepository.deleteAll();
        flightRepository.flush();

        airplaneRepository.deleteAll();
        airplaneRepository.flush();

        airportRepository.deleteAll();
        airportRepository.flush();

        basicFareRepository.deleteAll();
        basicFareRepository.flush();

    }

    /**
     * 全てのテストの前処理。
     * 
     * <pre>
     * ほんとは<code>@AfterClass/@BeforeClass</code>とかで実施するべきだが、staticメソッドにしたくない為、テストメソッドを別途用意しました。
     * <code>@Autowired</code>でインスタンス化したオブジェクトを使用したいため。
     * </pre>
     */
    @Test
    public void test000BeforeAllTests() {
        deleteAll();

    }

    /**
     * 全てのテストの後処理。
     * 
     * <pre>
     * ほんとは<code>@AfterClass/@BeforeClass</code>とかで実施するべきだが、staticメソッドにしたくない為、テストメソッドを別途用意しました。
     * <code>@Autowired</code>でインスタンス化したオブジェクトを使用したいため。
     * </pre>
     */
    @Test
    public void test999AfterAllTests() {
        deleteAll();

    }

    /**
     * Test for GET /airport/list.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>レコードが存在しない場合、HttpStatus(404)が返却されることを確認する。</li>
     * <li>レコードが存在しない状態で実施する必要がある為、テストの先頭で実施する。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test001FindAirportList404() throws Exception {
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/airport/list").with(postProcessor) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isNotFound()) //
        ;

    }

    /**
     * Test for GET /airplane/list.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>レコードが存在しない場合、HttpStatus(404)が返却されることを確認する。</li>
     * <li>レコードが存在しない状態で実施する必要がある為、テストの先頭で実施する。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test002FindAirolaneList404() throws Exception {
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/airplane/list").with(postProcessor)//
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isNotFound()) //
        ;

    }

    /**
     * Test for GET /basic-fare/list.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>レコードが存在しない場合、HttpStatus(404)が返却されることを確認する。</li>
     * <li>レコードが存在しない状態で実施する必要がある為、テストの先頭で実施する。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test003FindBasicFareList404() throws Exception {
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/basic-fare/list").with(postProcessor) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isNotFound()) //
        ;

    }

    /**
     * Test for GET /flight/list.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>レコードが存在しない場合、HttpStatus(404)が返却されることを確認する。</li>
     * <li>レコードが存在しない状態で実施する必要がある為、テストの先頭で実施する。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test004FindFlightList404() throws Exception {
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/flight/list").with(postProcessor) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isNotFound()) //
        ;

    }

    /**
     * Test for POST /airport.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>レコードの追加、更新が正しく行われることを確認します。</li>
     * <li>追加したレコードが正しくトピックへ通知されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test101AddAirportCorrect() throws Exception {
        // レコードの追加をテスト１件目

        Airport expAirport = Airport.builder().id("HND").name("東京").build();
        String jsonAirport = jsonMapper.writeValueAsString(expAirport);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airport").with(postProcessor) //
                        .content(jsonAirport) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        List<Airport> actualAirports = airportRepository.findAll();

        assertThat("レコードが追加されていること", actualAirports.size(), equalTo(1));
        assertThat("追加したレコードが正しい事", actualAirports.get(0), equalTo(expAirport));

        AirportTopic airportTopic = new AirportTopic();
        BeanUtils.copyProperties(expAirport, airportTopic);
        checkTopic(airportTopic);

        // レコードの追加をテスト２件目

        expAirport = Airport.builder().id("CTS").name("札幌（千歳）").build();
        jsonAirport = jsonMapper.writeValueAsString(expAirport);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airport").with(postProcessor) //
                        .content(jsonAirport) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        actualAirports = airportRepository.findAll();

        assertThat("レコードが追加されていること", actualAirports.size(), equalTo(2));
        assertThat("追加したレコードが正しい事", actualAirports, hasItems(expAirport));

        BeanUtils.copyProperties(expAirport, airportTopic);
        checkTopic(airportTopic);

        // レコードの更新をテスト

        Airport expUpdatedAirport = Airport.builder().id("HND").name("東京（羽田）").build();
        jsonAirport = jsonMapper.writeValueAsString(expUpdatedAirport);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airport").with(postProcessor) //
                        .content(jsonAirport) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        actualAirports = airportRepository.findAll();

        assertThat("レコード数が変わらないこと", actualAirports.size(), equalTo(2));
        assertThat("更新したレコードが正しい事", actualAirports, hasItems(expAirport, expUpdatedAirport));

        BeanUtils.copyProperties(expUpdatedAirport, airportTopic);
        checkTopic(airportTopic);

    }

    // トピックを確認する共通ロジック
    private <T> void checkTopic(T expTopic) throws Exception {
        checkTopic(expTopic, expTopic.getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private <T> void checkTopic(T expTopic, String payloadClassName) throws Exception {
        Message<String> sendMessage = (Message<String>) messageCollector.forChannel(flightTopicSource.output()).poll();

        assertTrue("トピックヘッダに'x-payload-class'が設定されている事", sendMessage.getHeaders().containsKey("x-payload-class"));
        assertThat("トピックヘッダ'x-payload-class'の値が正しい事", sendMessage.getHeaders().get("x-payload-class"),
                equalTo(payloadClassName));
        JSONAssert.assertEquals("トピックのメッセージが正しい事", jsonMapper.writeValueAsString(expTopic), sendMessage.getPayload(),
                false);

    }

    /**
     * Test for POST /airport.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>入力パラメータチェックエラーが発生した場合、HttpStatus(400)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test103AddAirport400() throws Exception {
        Airport expAirport = Airport.builder().id("HANEDA").name("東京").build();
        String jsonAirport = jsonMapper.writeValueAsString(expAirport);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airport").with(postProcessor) //
                        .content(jsonAirport) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isBadRequest()) //
        ;

    }

    /**
     * Test for POST /airport.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>認証していない場合、HttpStatus(401)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test102AddAirport401() throws Exception {
        // レコードの追加をテスト１件目

        Airport expAirport = Airport.builder().id("HND").name("東京").build();
        String jsonAirport = jsonMapper.writeValueAsString(expAirport);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airport")//
                        .content(jsonAirport) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is(401)) //
        ;
    }

    /**
     * Test for POST /airport.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>処理ロジックでエラーが発生した場合、HttpStatus(500)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test104AddAirport500() throws Exception {

        AirportInfo expAirport = AirportInfo.builder().id("HND").name("東京").build();
        String jsonAirport = jsonMapper.writeValueAsString(expAirport);

        doThrow(new RuntimeException("Something error")).when(airportService).addAirport(expAirport);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airport").with(postProcessor) //
                        .content(jsonAirport) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;

    }

    /**
     * Test for GET /airport/list.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>登録されている空港情報が正しく返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test105FindAirportListCorrect() throws Exception {

        Airport[] expAirports = { //
                Airport.builder().id("CTS").name("札幌（千歳）").build(), //
                Airport.builder().id("HND").name("東京（羽田）").build() //
        };

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/airport/list").with(postProcessor) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expAirports))) //
        ;
    }

    /**
     * Test for GET /airport/list.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>認証していない場合、HttpStatus(401)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test106FindAirportList401() throws Exception {

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/airport/list") //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is(401)) //
        ;
    }

    /**
     * Test for GET /airport/list.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>処理ロジックでエラーが発生した場合、HttpStatus(500)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test107FindAirportList500() throws Exception {

        when(airportService.findAirportList()).thenThrow(new RuntimeException("Something error"));

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .get("/" + urlRoot + "/airport/list").with(postProcessor) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;

    }

    /**
     * Test for GET /airport/{airportId}.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>登録されている空港情報が正しく返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test115FindAirportCorrect() throws Exception {

        String airportId = "CTS";
        Airport expAirport = Airport.builder().id(airportId).name("札幌（千歳）").build();

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/airport/" + airportId)//
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expAirport))) //
        ;
    }

    /**
     * Test for GET /airport/{airportId}.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>空港Idの桁数が不正の場合、HttpStatus(400)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test116FindAirport400() throws Exception {

        String airportId = "CT";

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .get("/" + urlRoot + "/airport/" + airportId).with(postProcessor) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is(400)) //
        ;
    }

    /**
     * Test for GET /airport/{airportId}.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>処理ロジックでエラーが発生した場合、HttpStatus(500)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test117FindAirport500() throws Exception {

        String airportId = "CTS";
        when(airportService.findAirport(airportId)).thenThrow(new RuntimeException("Something error"));

        mvc.perform( //
                MockMvcRequestBuilders //
                        .get("/" + urlRoot + "/airport/" + airportId) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;

    }

    /**
     * Test for POST /airplane.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>レコードの追加、更新が正しく行われることを確認します。</li>
     * <li>追加したレコードが正しくトピックへ通知されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test201AddAirplaneCorrect() throws Exception {
        // レコードの追加をテスト１件目

        Airplane expAirplane = Airplane.builder().name("B787").standardSeats(200).specialSeats(50).build();
        String jsonAirplane = jsonMapper.writeValueAsString(expAirplane);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airplane").with(postProcessor) //
                        .content(jsonAirplane) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        List<Airplane> actualAirplanes = airplaneRepository.findAll();

        assertThat("レコードが追加されていること", actualAirplanes.size(), equalTo(1));
        // idは比較対象外のため、あらかじめ同じ値にしておく
        expAirplane.setId(actualAirplanes.get(0).getId());
        assertThat("追加したレコードが正しい事", actualAirplanes.get(0), equalTo(expAirplane));

        AirplaneTopic airplaneTopic = new AirplaneTopic();
        BeanUtils.copyProperties(expAirplane, airplaneTopic);
        checkTopic(airplaneTopic);

        // レコードの追加をテスト２件目

        expAirplane = Airplane.builder().name("B777").standardSeats(270).specialSeats(30).build();
        jsonAirplane = jsonMapper.writeValueAsString(expAirplane);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airplane").with(postProcessor) //
                        .content(jsonAirplane) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        actualAirplanes = airplaneRepository.findAll();

        assertThat("レコードが追加されていること", actualAirplanes.size(), equalTo(2));

        Airplane actualB777 = actualAirplanes.stream().filter(a -> a.getName().equals("B777")).findFirst()
                .orElseThrow();
        // idは比較対象外のため、あらかじめ同じ値にしておく
        expAirplane.setId(actualB777.getId());
        assertThat("追加したレコードが正しい事", actualAirplanes, hasItems(expAirplane));

        BeanUtils.copyProperties(expAirplane, airplaneTopic);
        checkTopic(airplaneTopic);

        // レコードの更新をテスト

        Example<Airplane> example = Example.of(Airplane.builder().name("B787").build());
        Airplane expUpdatedAirplane = airplaneRepository.findOne(example).orElseThrow();
        expUpdatedAirplane.setStandardSeats(250);
        expUpdatedAirplane.setSpecialSeats(100);
        jsonAirplane = jsonMapper.writeValueAsString(expUpdatedAirplane);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airplane").with(postProcessor) //
                        .content(jsonAirplane) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        actualAirplanes = airplaneRepository.findAll();

        assertThat("レコード数が変わらないこと", actualAirplanes.size(), equalTo(2));
        assertThat("更新したレコードが正しい事", actualAirplanes, hasItems(expAirplane, expUpdatedAirplane));

        BeanUtils.copyProperties(expUpdatedAirplane, airplaneTopic);
        checkTopic(airplaneTopic);

    }

    /**
     * Test for POST /airplane.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>入力パラメータチェックエラーが発生した場合、HttpStatus(400)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test203AddAirplane400() throws Exception {
        Airplane expAirplane = Airplane.builder().name("B787").build();
        String jsonAirplane = jsonMapper.writeValueAsString(expAirplane);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airplane").with(postProcessor) //
                        .content(jsonAirplane) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isBadRequest()) //
        ;

    }

    /**
     * Test for POST /airplane.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>認証していない場合、HttpStatus(401)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test202AddAirplane401() throws Exception {
        // レコードの追加をテスト１件目

        Airplane expAirplane = Airplane.builder().name("B787").standardSeats(200).specialSeats(50).build();
        String jsonAirplane = jsonMapper.writeValueAsString(expAirplane);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airplane")//
                        .content(jsonAirplane) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is(401)) //
        ;
    }

    /**
     * Test for POST /airplane.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>処理ロジックでエラーが発生した場合、HttpStatus(500)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test204AddAirplane500() throws Exception {

        AirplaneInfo expAirplane = AirplaneInfo.builder().name("B787").standardSeats(200).specialSeats(50).build();
        String jsonAirplane = jsonMapper.writeValueAsString(expAirplane);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        doThrow(new RuntimeException("Something error")).when(airplaneService).addAirplane(expAirplane);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/airplane").with(postProcessor) //
                        .content(jsonAirplane) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;

    }

    /**
     * Test for GET /airplane/list.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>登録されている機体情報が正しく返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test205FindAirplaneListCorrect() throws Exception {

        List<Airplane> expAirplanes = airplaneRepository.findAll();

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/airplane/list").with(postProcessor) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expAirplanes))) //
        ;
    }

    /**
     * Test for GET /airplane/list.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>認証していない場合、HttpStatus(401)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test206FindAirplaneList401() throws Exception {

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/airplane/list")//
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is(401));
    }

    /**
     * Test for GET /airplane/list.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>処理ロジックでエラーが発生した場合、HttpStatus(500)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test207FindAirplaneList500() throws Exception {

        when(airplaneService.findAirplaneList()).thenThrow(new RuntimeException("Something error"));

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .get("/" + urlRoot + "/airplane/list").with(postProcessor) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;

    }

    /**
     * Test for POST /basic-fare.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>レコードの追加、更新が正しく行われることを確認します。</li>
     * <li>追加したレコードが正しくトピックへ通知されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test301AddBasicFareCorrect() throws Exception {
        // レコードの追加をテスト１件目

        BasicFareInfo expBasicFare = BasicFareInfo.builder().departure("HND").arrival("CTS").fare(30000).build();
        String jsonBasicFare = jsonMapper.writeValueAsString(expBasicFare);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/basic-fare").with(postProcessor) //
                        .content(jsonBasicFare) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        List<BasicFare> actualBasicFares = basicFareRepository.findAll();
        actualBasicFares.sort(Comparator.comparing(BasicFare::getDeparture).reversed());
        // CST -> HND
        BasicFareInfo returnTrip = BasicFareInfo.builder().departure(expBasicFare.getArrival())
                .arrival(expBasicFare.getDeparture()).fare(expBasicFare.getFare()).build();

        // HND -> CTS を確認
        assertThat("レコードが追加されていること", actualBasicFares.size(), equalTo(2));
        assertThat("追加したレコードが正しい事/Departure", actualBasicFares.get(0).getDeparture(),
                equalTo(expBasicFare.getDeparture()));
        assertThat("追加したレコードが正しい事/Arrival", actualBasicFares.get(0).getArrival(), equalTo(expBasicFare.getArrival()));
        assertThat("追加したレコードが正しい事/Fare", actualBasicFares.get(0).getFare(), equalTo(expBasicFare.getFare()));
        // CTS -> HND を確認
        assertThat("追加したレコードが正しい事/Departure", actualBasicFares.get(1).getDeparture(),
                equalTo(returnTrip.getDeparture()));
        assertThat("追加したレコードが正しい事/Arrival", actualBasicFares.get(1).getArrival(), equalTo(returnTrip.getArrival()));
        assertThat("追加したレコードが正しい事/Fare", actualBasicFares.get(1).getFare(), equalTo(returnTrip.getFare()));

        // トピックのメッセージを確認
        checkTopicBasicFare(expBasicFare, returnTrip);

        // レコードの追加をテスト２件目

        expBasicFare = BasicFareInfo.builder().departure("HND").arrival("OSA").fare(25000).build();
        jsonBasicFare = jsonMapper.writeValueAsString(expBasicFare);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/basic-fare").with(postProcessor) //
                        .content(jsonBasicFare) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        actualBasicFares = basicFareRepository.findAll();
        assertThat("レコードが追加されていること", actualBasicFares.size(), equalTo(4));

        // 追加したレコードのみフィルタ
        actualBasicFares = basicFareRepository.findAll().stream()
                .filter(a -> a.getDeparture().equals("OSA") || a.getArrival().equals("OSA"))
                .collect(Collectors.toList());
        actualBasicFares.sort(Comparator.comparing(BasicFare::getDeparture));
        // OSA -> HND
        returnTrip = BasicFareInfo.builder().departure(expBasicFare.getArrival()).arrival(expBasicFare.getDeparture())
                .fare(expBasicFare.getFare()).build();

        // HND -> OSA
        assertThat("追加したレコードが正しい事/Departure", actualBasicFares.get(0).getDeparture(),
                equalTo(expBasicFare.getDeparture()));
        assertThat("追加したレコードが正しい事/Arrival", actualBasicFares.get(0).getArrival(), equalTo(expBasicFare.getArrival()));
        assertThat("追加したレコードが正しい事/Fare", actualBasicFares.get(0).getFare(), equalTo(expBasicFare.getFare()));
        // OSA -> HND
        assertThat("追加したレコードが正しい事/Departure", actualBasicFares.get(1).getDeparture(),
                equalTo(returnTrip.getDeparture()));
        assertThat("追加したレコードが正しい事/Arrival", actualBasicFares.get(1).getArrival(), equalTo(returnTrip.getArrival()));
        assertThat("追加したレコードが正しい事/Fare", actualBasicFares.get(1).getFare(), equalTo(returnTrip.getFare()));

        // トピックのメッセージを確認
        checkTopicBasicFare(expBasicFare, returnTrip);

        // レコードの更新をテスト

        BasicFareInfo expUpdatedBasicFare = BasicFareInfo.builder().departure("HND").arrival("CTS").fare(32000).build();
        jsonBasicFare = jsonMapper.writeValueAsString(expUpdatedBasicFare);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/basic-fare").with(postProcessor) //
                        .content(jsonBasicFare) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        actualBasicFares = basicFareRepository.findAll();

        assertThat("レコード数が変わらないこと", actualBasicFares.size(), equalTo(4));

        actualBasicFares = basicFareRepository.findAll().stream()
                .filter(a -> a.getDeparture().equals("CTS") || a.getArrival().equals("CTS"))
                .collect(Collectors.toList());
        actualBasicFares.sort(Comparator.comparing(BasicFare::getDeparture).reversed());
        // CTS -> HND
        returnTrip = BasicFareInfo.builder().departure(expUpdatedBasicFare.getArrival())
                .arrival(expUpdatedBasicFare.getDeparture()).fare(expUpdatedBasicFare.getFare()).build();

        // HND -> CTS
        assertThat("追加したレコードが正しい事/Departure", actualBasicFares.get(0).getDeparture(),
                equalTo(expUpdatedBasicFare.getDeparture()));
        assertThat("追加したレコードが正しい事/Arrival", actualBasicFares.get(0).getArrival(),
                equalTo(expUpdatedBasicFare.getArrival()));
        assertThat("追加したレコードが正しい事/Fare", actualBasicFares.get(0).getFare(), equalTo(expUpdatedBasicFare.getFare()));
        // CTS -> HND
        assertThat("追加したレコードが正しい事/Departure", actualBasicFares.get(1).getDeparture(),
                equalTo(returnTrip.getDeparture()));
        assertThat("追加したレコードが正しい事/Arrival", actualBasicFares.get(1).getArrival(), equalTo(returnTrip.getArrival()));
        assertThat("追加したレコードが正しい事/Fare", actualBasicFares.get(1).getFare(), equalTo(returnTrip.getFare()));

        // トピックのメッセージを確認
        checkTopicBasicFare(expUpdatedBasicFare, returnTrip);

    }

    // トピック（BasicFare）を確認する共通ロジック
    @SuppressWarnings("unchecked")
    private void checkTopicBasicFare(BasicFareInfo expForward, BasicFareInfo expReturn) throws Exception {
        List<Message<String>> sendMessages = new ArrayList<>();
        Optional<Message<String>> sendMessage;
        do {
            sendMessage = Optional
                    .ofNullable((Message<String>) messageCollector.forChannel(flightTopicSource.output()).poll());
            sendMessage.ifPresent(sendMessages::add);
        } while (sendMessage.isPresent());

        assertThat("トピックが2件取得できること", sendMessages.size(), equalTo(2));
        sendMessages.forEach(m -> {
            assertTrue("トピックヘッダに'x-payload-class'が設定されている事", m.getHeaders().containsKey("x-payload-class"));
            assertThat("トピックヘッダ'x-payload-class'の値が正しい事", m.getHeaders().get("x-payload-class"),
                    equalTo(BasicFareTopic.class.getSimpleName()));
        });
        String jsonActualMessages = sendMessages.stream().map(Message<String>::getPayload)
                .collect(Collectors.joining(",", "[", "]"));
        BasicFareTopic expForwardTopic = BasicFareTopic.builder().arrivalAirportId(expForward.getArrival()).departureAirportId(expForward.getDeparture()).fare(expForward.getFare()).build();
        BasicFareTopic expReturnTopic = BasicFareTopic.builder().arrivalAirportId(expReturn.getArrival()).departureAirportId(expReturn.getDeparture()).fare(expReturn.getFare()).build();
        JSONAssert.assertEquals("トピックのメッセージが正しい事",
                jsonMapper.writeValueAsString(new Object[] { expForwardTopic, expReturnTopic }), jsonActualMessages, false);

    }

    /**
     * Test for POST /basic-fare.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>入力パラメータチェックエラーが発生した場合、HttpStatus(400)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test302AddBasicFare400() throws Exception {
        BasicFare expBasicFare = BasicFare.builder().departure("HANEDA").arrival("CTS").fare(30000).build();
        String jsonBasicFare = jsonMapper.writeValueAsString(expBasicFare);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/basic-fare").with(postProcessor) //
                        .content(jsonBasicFare) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isBadRequest()) //
        ;

    }

    /**
     * Test for POST /basic-fare.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>認証していない場合、HttpStatus(401)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test303AddBasicFare401() throws Exception {
        // レコードの追加をテスト１件目

        BasicFare expBasicFare = BasicFare.builder().departure("HND").arrival("CTS").fare(30000).build();
        String jsonBasicFare = jsonMapper.writeValueAsString(expBasicFare);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/basic-fare") //
                        .content(jsonBasicFare) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is(401)) //
        ;
    }

    /**
     * Test for POST /basic-fare.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>処理ロジックでエラーが発生した場合、HttpStatus(500)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test304AddBasicFare500() throws Exception {

        BasicFareInfo expBasicFare = BasicFareInfo.builder().departure("HND").arrival("CTS").fare(30000).build();
        String jsonBasicFare = jsonMapper.writeValueAsString(expBasicFare);

        doThrow(new RuntimeException("Something error")).when(basicFareService).addBasicFare(expBasicFare);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/basic-fare").with(postProcessor) //
                        .content(jsonBasicFare) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;

    }

    /**
     * Test for GET /basic-fare/list.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>登録されている区間運賃情報が正しく返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test305FindBasicFareListCorrect() throws Exception {

        List<BasicFare> expBasicFares = basicFareRepository.findAll();
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/basic-fare/list").with(postProcessor) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expBasicFares))) //
        ;

        Example<BasicFare> example = Example.of(BasicFare.builder().departure("HND").build());
        expBasicFares = basicFareRepository.findAll(example);

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/basic-fare/list").with(postProcessor) //
                .param("d", "HND") //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expBasicFares))) //
        ;

        example = Example.of(BasicFare.builder().arrival("HND").build());
        expBasicFares = basicFareRepository.findAll(example);

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/basic-fare/list").with(postProcessor) //
                .param("a", "HND") //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expBasicFares))) //
        ;

        example = Example.of(BasicFare.builder().departure("HND").arrival("CTS").build());
        expBasicFares = basicFareRepository.findAll(example);

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/basic-fare/list").with(postProcessor) //
                .param("d", "HND") //
                .param("a", "CTS") //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expBasicFares))) //
        ;

    }

    /**
     * Test for GET /basic-fare/list.
     * 
     * <pre>
     *  異常系テスト。
     * <ul>
     * <li>認証していない場合、HttpStatus(401)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test306FindBasicFareList401() throws Exception {

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/basic-fare/list") //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is(401)) //
        ;

    }

    /**
     * Test for GET /basic-fare/list.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>処理ロジックでエラーが発生した場合、HttpStatus(500)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test307FindBasicFareList500() throws Exception {

        when(basicFareService.findBasicFareList(Optional.empty(), Optional.empty()))
                .thenThrow(new RuntimeException("Something error"));

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .get("/" + urlRoot + "/basic-fare/list").with(postProcessor) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;

    }

    /**
     * Test for POST /flight.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>レコードの追加、更新が正しく行われることを確認します。</li>
     * <li>追加したレコードが正しくトピックへ通知されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test401AddFlightCorrect() throws Exception {
        // レコードの追加をテスト１件目

        Example<Airplane> example = Example.of(Airplane.builder().name("B787").build());
        Airplane airplane = airplaneRepository.findOne(example).orElseThrow();
        BasicFare basicFare = basicFareRepository
                .findById(BasicFarePk.builder().departure("HND").arrival("CTS").build()).orElseThrow();
        FlightUpdateInfo expFlight = FlightUpdateInfo.builder().name("MSA001").airplaneId(airplane.getId())
                .departureAirportId("HND").departureTime(LocalDateTime.parse("1901-01-01T10:00:00"))
                .arrivalAirportId("CTS").arrivalTime(LocalDateTime.parse("1901-01-01T11:30:00")).build();
        String jsonFlight = jsonMapper.writeValueAsString(expFlight);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight").with(postProcessor) //
                        .content(jsonFlight) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        List<Flight> actualFlights = flightRepository.findAll();

        assertThat("レコードが追加されていること", actualFlights.size(), equalTo(1));

        assertThat("追加したレコードが正しい事/name", actualFlights.get(0).getName(), equalTo(expFlight.getName()));
        assertThat("追加したレコードが正しい事/airplaneId", actualFlights.get(0).getAirplane().getId(),
                equalTo(expFlight.getAirplaneId()));
        assertThat("追加したレコードが正しい事/departureAirportId", actualFlights.get(0).getDepartureAirport().getId(),
                equalTo(expFlight.getDepartureAirportId()));
        assertThat("追加したレコードが正しい事/departureTime", actualFlights.get(0).getDepartureTime(),
                equalTo(expFlight.getDepartureTime()));
        assertThat("追加したレコードが正しい事/arrivalAirportId", actualFlights.get(0).getArrivalAirport().getId(),
                equalTo(expFlight.getArrivalAirportId()));
        assertThat("追加したレコードが正しい事/departureTime", actualFlights.get(0).getArrivalTime(),
                equalTo(expFlight.getArrivalTime()));
        assertThat("追加したレコードが正しい事/basicFare", actualFlights.get(0).getBasicFare(), equalTo(basicFare));

        // トピックのメッセージを確認
        FlightTopic flightTopic = new FlightTopic();
        BeanUtils.copyProperties(expFlight, flightTopic);
        flightTopic.setDepartureTime(getTimeFromDateTime(expFlight.getDepartureTime()));
        flightTopic.setArrivalTime(getTimeFromDateTime(expFlight.getArrivalTime()));

        checkTopic(flightTopic, FlightTopic.class.getSimpleName());

        // レコードの追加をテスト２件目

        example = Example.of(Airplane.builder().name("B777").build());
        airplane = airplaneRepository.findOne(example).orElseThrow();
        basicFare = basicFareRepository.findById(BasicFarePk.builder().departure("CTS").arrival("HND").build())
                .orElseThrow();
        expFlight = FlightUpdateInfo.builder().name("MSA002").airplaneId(airplane.getId()).departureAirportId("CTS")
                .departureTime(LocalDateTime.parse("2000-12-31T10:10:00")).arrivalAirportId("HND")
                .arrivalTime(LocalDateTime.parse("2000-12-31T11:40:00")).build();
        jsonFlight = jsonMapper.writeValueAsString(expFlight);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight").with(postProcessor) //
                        .content(jsonFlight) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        actualFlights = flightRepository.findAll();
        assertThat("レコードが追加されていること", actualFlights.size(), equalTo(2));

        Optional<Flight> actualFlight = actualFlights.stream().filter(f -> f.getName().equals("MSA002")).findFirst();
        assertTrue("追加したレコードが存在すること", actualFlight.isPresent());

        // 日付は1901-01-01で登録されていること
        expFlight.setDepartureTime(expFlight.getDepartureTime().withYear(1901).withMonth(1).withDayOfMonth(1));
        expFlight.setArrivalTime(expFlight.getArrivalTime().withYear(1901).withMonth(1).withDayOfMonth(1));

        assertThat("追加したレコードが正しい事/name", actualFlight.orElseThrow().getName(), equalTo(expFlight.getName()));
        assertThat("追加したレコードが正しい事/airplaneId", actualFlight.orElseThrow().getAirplane().getId(),
                equalTo(expFlight.getAirplaneId()));
        assertThat("追加したレコードが正しい事/departureAirportId", actualFlight.orElseThrow().getDepartureAirport().getId(),
                equalTo(expFlight.getDepartureAirportId()));
        assertThat("追加したレコードが正しい事/departureTime", actualFlight.orElseThrow().getDepartureTime(),
                equalTo(expFlight.getDepartureTime()));
        assertThat("追加したレコードが正しい事/arrivalAirportId", actualFlight.orElseThrow().getArrivalAirport().getId(),
                equalTo(expFlight.getArrivalAirportId()));
        assertThat("追加したレコードが正しい事/departureTime", actualFlight.orElseThrow().getArrivalTime(),
                equalTo(expFlight.getArrivalTime()));
        assertThat("追加したレコードが正しい事/basicFare", actualFlight.orElseThrow().getBasicFare(), equalTo(basicFare));

        // トピックのメッセージを確認
        BeanUtils.copyProperties(expFlight, flightTopic);
        flightTopic.setDepartureTime(getTimeFromDateTime(expFlight.getDepartureTime()));
        flightTopic.setArrivalTime(getTimeFromDateTime(expFlight.getArrivalTime()));

        checkTopic(flightTopic, FlightTopic.class.getSimpleName());

        // レコードの更新をテスト

        example = Example.of(Airplane.builder().name("B777").build());
        airplane = airplaneRepository.findOne(example).orElseThrow();
        basicFare = basicFareRepository.findById(BasicFarePk.builder().departure("CTS").arrival("HND").build())
                .orElseThrow();
        FlightUpdateInfo expUpdatedFlight = FlightUpdateInfo.builder().name("MSA001").airplaneId(airplane.getId())
                .departureAirportId("CTS").departureTime(LocalDateTime.parse("1901-01-01T09:00:00"))
                .arrivalAirportId("HND").arrivalTime(LocalDateTime.parse("1901-01-01T10:30:00")).build();
        jsonFlight = jsonMapper.writeValueAsString(expUpdatedFlight);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight").with(postProcessor) //
                        .content(jsonFlight) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        actualFlights = flightRepository.findAll();

        assertThat("レコード数が変わらないこと", actualFlights.size(), equalTo(2));

        actualFlight = actualFlights.stream().filter(f -> f.getName().equals("MSA001")).findFirst();
        assertTrue("更新したレコードが存在すること", actualFlight.isPresent());

        assertThat("更新したレコードが正しい事/name", actualFlight.orElseThrow().getName(), equalTo(expUpdatedFlight.getName()));
        assertThat("更新したレコードが正しい事/airplaneId", actualFlight.orElseThrow().getAirplane().getId(),
                equalTo(expUpdatedFlight.getAirplaneId()));
        assertThat("更新したレコードが正しい事/departureAirportId", actualFlight.orElseThrow().getDepartureAirport().getId(),
                equalTo(expUpdatedFlight.getDepartureAirportId()));
        assertThat("更新したレコードが正しい事/departureTime", actualFlight.orElseThrow().getDepartureTime(),
                equalTo(expUpdatedFlight.getDepartureTime()));
        assertThat("更新したレコードが正しい事/arrivalAirportId", actualFlight.orElseThrow().getArrivalAirport().getId(),
                equalTo(expUpdatedFlight.getArrivalAirportId()));
        assertThat("更新したレコードが正しい事/departureTime", actualFlight.orElseThrow().getArrivalTime(),
                equalTo(expUpdatedFlight.getArrivalTime()));
        assertThat("更新したレコードが正しい事/basicFare", actualFlight.orElseThrow().getBasicFare(), equalTo(basicFare));

        // トピックのメッセージを確認
        BeanUtils.copyProperties(expUpdatedFlight, flightTopic);
        flightTopic.setDepartureTime(getTimeFromDateTime(expUpdatedFlight.getDepartureTime()));
        flightTopic.setArrivalTime(getTimeFromDateTime(expUpdatedFlight.getArrivalTime()));

        checkTopic(flightTopic, FlightTopic.class.getSimpleName());

    }

    /**
     * Test for POST /flight.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>入力パラメータチェックエラーが発生した場合、HttpStatus(400)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test402AddFlight400() throws Exception {
        FlightUpdateInfo expFlight = FlightUpdateInfo.builder().name("MSA001") //
                .departureAirportId("HND").departureTime(LocalDateTime.parse("1901-01-01T10:00:00"))
                .arrivalAirportId("CTS").arrivalTime(LocalDateTime.parse("1901-01-01T11:30:00")).build();
        String jsonFlight = jsonMapper.writeValueAsString(expFlight);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight").with(postProcessor) //
                        .content(jsonFlight) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isBadRequest()) //
        ;

    }

    /**
     * Test for POST /flight.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>認証していない場合、HttpStatus(401)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test403AddFlight401() throws Exception {
        // レコードの追加をテスト１件目

        Example<Airplane> example = Example.of(Airplane.builder().name("B787").build());
        Airplane airplane = airplaneRepository.findOne(example).orElseThrow();
        FlightUpdateInfo expFlight = FlightUpdateInfo.builder().name("MSA001").airplaneId(airplane.getId())
                .departureAirportId("HND").departureTime(LocalDateTime.parse("1901-01-01T10:00:00"))
                .arrivalAirportId("CTS").arrivalTime(LocalDateTime.parse("1901-01-01T11:30:00")).build();
        String jsonFlight = jsonMapper.writeValueAsString(expFlight);

        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight") //
                        .content(jsonFlight) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is(401)) //
        ;

    }

    /**
     * Test for POST /flight.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>処理ロジックでエラーが発生した場合、HttpStatus(500)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test404AddFlight500() throws Exception {

        Example<Airplane> example = Example.of(Airplane.builder().name("B787").build());
        Airplane airplane = airplaneRepository.findOne(example).orElseThrow();
        FlightUpdateInfo expFlight = FlightUpdateInfo.builder().name("MSA001").airplaneId(airplane.getId())
                .departureAirportId("HND").departureTime(LocalDateTime.parse("1901-01-01T10:00:00"))
                .arrivalAirportId("CTS").arrivalTime(LocalDateTime.parse("1901-01-01T11:30:00")).build();
        String jsonFlight = jsonMapper.writeValueAsString(expFlight);

        doThrow(new RuntimeException("test403AddFlight500")).when(flightService).addFlight(expFlight);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .post("/" + urlRoot + "/flight").with(postProcessor) //
                        .content(jsonFlight) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;

    }

    /**
     * Test for GET /flight/list.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>登録されているフライト情報が正しく返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test405FindFlightListCorrect() throws Exception {

        List<Flight> expFlights = flightRepository.findAll();

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/flight/list").with(postProcessor) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expFlights))) //
        ;

        Airport hndAirport = airportRepository.findById("CTS").orElseThrow();
        Example<Flight> example = Example.of(Flight.builder().departureAirport(hndAirport).build());
        expFlights = flightRepository.findAll(example);

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/flight/list").with(postProcessor)//
                .param("d", "CTS") //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expFlights))) //
        ;

        Airport ctsAirport = airportRepository.findById("HND").orElseThrow();
        example = Example.of(Flight.builder().arrivalAirport(ctsAirport).build());
        expFlights = flightRepository.findAll(example);

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/flight/list").with(postProcessor) //
                .param("a", "HND") //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expFlights))) //
        ;

        example = Example.of(Flight.builder().departureAirport(hndAirport).arrivalAirport(ctsAirport).build());
        expFlights = flightRepository.findAll(example);

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/flight/list").with(postProcessor) //
                .param("d", "CTS") //
                .param("a", "HND") //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) //
                .andExpect(content().json(jsonMapper.writeValueAsString(expFlights))) //
        ;

    }

    /**
     * Test for GET /flight/list.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>認証していない場合、HttpStatus(401)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test406FindFlightList401() throws Exception {

        mvc.perform(MockMvcRequestBuilders //
                .get("/" + urlRoot + "/flight/list") //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is(401)) //
        ;
    }

    /**
     * Test for GET /flight/list.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>処理ロジックでエラーが発生した場合、HttpStatus(500)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test407FindFlightList500() throws Exception {

        when(flightService.findFlightList(Optional.empty(), Optional.empty()))
                .thenThrow(new RuntimeException("test405FindFlightList500"));

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform( //
                MockMvcRequestBuilders //
                        .get("/" + urlRoot + "/flight/list").with(postProcessor) //
                        .accept(MediaType.APPLICATION_JSON_UTF8) //
                        .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;

    }

    /**
     * Test for POST /flight/seat/reserve.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>正常に空席確保された場合、フライトトピックにフライト情報を通知します。</li>
     * <li>フライト情報のメッセージには、ヘッダ情報として x-payload-class: VacantSeat が設定されます。</li>
      * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test501ReserveVacantSeatCorrect() throws Exception {

        List<Flight> flights = flightRepository.findAll();
        Flight flight = flights.get(0);
        Airplane airplane = flight.getAirplane();

        Long reserveId = 1L;
        LocalDate departureDate = LocalDate.of(2019, 12, 1);
        String flightName = flight.getName();
        // 空席
        int standardVacantSeats = airplane.getStandardSeats();
        int specialVacantSeats = airplane.getSpecialSeats();

        VacantSeatPk vacantSeatPk = VacantSeatPk.builder()//
                .flightName(flightName)//
                .departureDate(departureDate)//
                .build();
        vacantSeatRepository.findById(vacantSeatPk).ifPresent(v -> vacantSeatRepository.deleteById(vacantSeatPk));
        assertTrue("空席情報が存在しないこと。", vacantSeatRepository.findById(vacantSeatPk).isEmpty());
        log.info("予約可能な空席数。 n:{}, s:{}", standardVacantSeats, specialVacantSeats);

        reserveVacantSeatRepository.findById(reserveId)
                .ifPresent(r -> reserveVacantSeatRepository.deleteById(r.getReserveId()));
        assertTrue("空席確保情報が存在しないこと。", reserveVacantSeatRepository.findById(reserveId).isEmpty());

        // 空席確保します
        int reserveNormalSeats = 1;
        ReserveVacantSeatInfo reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId)
                .departureDate(departureDate).flightName(flightName).seatClass(SeatClass.N)
                .vacantSeatCount(reserveNormalSeats).build();
        String jsonReserveVacantSeat = jsonMapper.writeValueAsString(reserveVacantSeat);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform(MockMvcRequestBuilders //
                .post("/" + urlRoot + "/flight/seat/reserve").with(postProcessor) //
                .content(jsonReserveVacantSeat)//
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;
        VacantSeat vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
        log.info("予約可能な空席数。 n:{}, s:{}", vacantSeat.getVacantStandardSeatCount(),
                vacantSeat.getVacantSpecialSeatCount());
        assertEquals("一般席の空席(N)が確保されたこと。", vacantSeat.getVacantStandardSeatCount().intValue(),
                standardVacantSeats - reserveNormalSeats);

        // トピックのメッセージを確認
        FlightVacantSeatTopic flightVacantSeatTopic = new FlightVacantSeatTopic();
        BeanUtils.copyProperties(vacantSeat, flightVacantSeatTopic);
        checkTopic(flightVacantSeatTopic, FlightVacantSeatTopic.class.getSimpleName());

        reserveId = 2L;
        int reserveSpecialSeats = 2;
        reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId).departureDate(departureDate)
                .flightName(flightName).seatClass(SeatClass.S).vacantSeatCount(reserveSpecialSeats).build();
        jsonReserveVacantSeat = jsonMapper.writeValueAsString(reserveVacantSeat);

        mvc.perform(MockMvcRequestBuilders //
                .post("/" + urlRoot + "/flight/seat/reserve").with(postProcessor)//
                .content(jsonReserveVacantSeat)//
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
        log.info("予約可能な空席数。 n:{}, s:{}", vacantSeat.getVacantStandardSeatCount(),
                vacantSeat.getVacantSpecialSeatCount());
        assertEquals("特別席の空席(S)が確保されたこと。", vacantSeat.getVacantSpecialSeatCount().intValue(),
                specialVacantSeats - reserveSpecialSeats);

        // トピックのメッセージを確認
        BeanUtils.copyProperties(vacantSeat, flightVacantSeatTopic);
        checkTopic(flightVacantSeatTopic, FlightVacantSeatTopic.class.getSimpleName());
    }

    /**
     * Test for POST /flight/seat/reserve.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>認証していない場合、HttpStatus(401)が返却されることを確認します。</li>
      * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test502ReserveVacantSeat401() throws Exception {

        List<Flight> flights = flightRepository.findAll();
        Flight flight = flights.get(0);

        Long reserveId = 1L;
        LocalDate departureDate = LocalDate.of(2019, 12, 1);
        String flightName = flight.getName();

        // 空席確保します
        int reserveNormalSeats = 1;
        ReserveVacantSeatInfo reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId)
                .departureDate(departureDate).flightName(flightName).seatClass(SeatClass.N)
                .vacantSeatCount(reserveNormalSeats).build();
        String jsonReserveVacantSeat = jsonMapper.writeValueAsString(reserveVacantSeat);

        mvc.perform(MockMvcRequestBuilders //
                .post("/" + urlRoot + "/flight/seat/reserve") //
                .content(jsonReserveVacantSeat)//
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is(401)) //
        ;
    }

    /**
     * Test for POST /flight/seat/reserve.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>フライトが存在しない場合、HttpStatus(404)が返却されることを確認します</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test503ReserveVacantSeat404() throws Exception {

        String flightName = "dummy";
        flightRepository.findById(flightName).ifPresent(f -> flightRepository.deleteById(f.getName()));

        Long reserveId = 3L;
        LocalDate departureDate = LocalDate.of(2019, 12, 1);

        VacantSeatPk vacantSeatPk = VacantSeatPk.builder()//
                .flightName(flightName)//
                .departureDate(departureDate)//
                .build();
        vacantSeatRepository.findById(vacantSeatPk).ifPresent(v -> vacantSeatRepository.deleteById(vacantSeatPk));
        reserveVacantSeatRepository.findById(reserveId)
                .ifPresent(r -> reserveVacantSeatRepository.deleteById(r.getReserveId()));

        // 空席確保します
        int reserveNormalSeats = 1;
        ReserveVacantSeatInfo reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId)
                .departureDate(departureDate).flightName(flightName).seatClass(SeatClass.N)
                .vacantSeatCount(reserveNormalSeats).build();
        String jsonReserveVacantSeat = jsonMapper.writeValueAsString(reserveVacantSeat);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform(MockMvcRequestBuilders //
                .post("/" + urlRoot + "/flight/seat/reserve").with(postProcessor) //
                .content(jsonReserveVacantSeat)//
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isNotFound()) //
        ;
    }

    /**
     * Test for POST /flight/seat/reserve.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>空席が確保できない場合、HttpStatus(412)が返却されることを確認します</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test504ReserveVacantSeat412() throws Exception {
        List<Flight> flights = flightRepository.findAll();
        Flight flight = flights.get(0);
        Airplane airplane = flight.getAirplane();

        Long reserveId = 3L;
        LocalDate departureDate = LocalDate.of(2019, 12, 1);
        String flightName = flight.getName();

        // 空席確保します
        int reserveNormalSeats = airplane.getStandardSeats().intValue() + 1;
        ReserveVacantSeatInfo reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId)
                .departureDate(departureDate).flightName(flightName).seatClass(SeatClass.N)
                .vacantSeatCount(reserveNormalSeats).build();
        String jsonReserveVacantSeat = jsonMapper.writeValueAsString(reserveVacantSeat);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform(MockMvcRequestBuilders //
                .post("/" + urlRoot + "/flight/seat/reserve").with(postProcessor) //
                .content(jsonReserveVacantSeat)//
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is((HttpStatus.PRECONDITION_FAILED.value()))) //
        ;
    }

    /**
     * Test for POST /flight/seat/reserve.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
    * <li>処理ロジックでエラーが発生した場合、HttpStatus(500)が返却されることを確認します。</li>
       * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test505ReserveVacantSeat500() throws Exception {
        List<Flight> flights = flightRepository.findAll();
        Flight flight = flights.get(0);

        Long reserveId = 3L;
        LocalDate departureDate = LocalDate.of(2019, 12, 1);
        String flightName = flight.getName();

        // 空席確保します
        int reserveNormalSeats = 1;
        ReserveVacantSeatInfo reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId)
                .departureDate(departureDate).flightName(flightName).seatClass(SeatClass.N)
                .vacantSeatCount(reserveNormalSeats).build();
        String jsonReserveVacantSeat = jsonMapper.writeValueAsString(reserveVacantSeat);

        doThrow(new RuntimeException("Something error")).when(reserveVacantSeatService)
                .reserveVacantSeat(reserveVacantSeat);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        mvc.perform(MockMvcRequestBuilders //
                .post("/" + urlRoot + "/flight/seat/reserve").with(postProcessor) //
                .content(jsonReserveVacantSeat)//
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;
    }

    /**
     * Test for POST /flight/seat/cancel.
     * 
     * <pre>
     * 正常系テスト。
     * <ul>
     * <li>正常に空席確保取り消された場合、フライトトピックにフライト情報を通知します。</li>
     * <li>フライト情報のメッセージには、ヘッダ情報として x-payload-class: VacantSeat が設定されます。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test601CancelReservedSeatCorrect() throws Exception {

        List<Flight> flights = flightRepository.findAll();
        Flight flight = flights.get(0);

        Long reserveId = 1L;
        LocalDate departureDate = LocalDate.of(2019, 12, 1);
        String flightName = flight.getName();

        VacantSeatPk vacantSeatPk = VacantSeatPk.builder()//
                .flightName(flightName)//
                .departureDate(departureDate)//
                .build();
        VacantSeat vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
        // 空席
        int standardVacantSeats = vacantSeat.getVacantStandardSeatCount().intValue();
        int specialVacantSeats = vacantSeat.getVacantSpecialSeatCount().intValue();

        log.info("予約可能な空席数。 n:{}, s:{}", standardVacantSeats, specialVacantSeats);

        // 空席確保を取り消す
        ReserveVacantSeat reserveVacantSeat = reserveVacantSeatRepository.findById(reserveId).get();
        String jsonReserveVacantSeat = jsonMapper.writeValueAsString(reserveVacantSeat);

        int cancelNormalSeats = reserveVacantSeat.getVacantSeatCount().intValue();
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");

        mvc.perform(MockMvcRequestBuilders //
                .post(String.format("/%s/flight/seat/cancel", urlRoot)).with(postProcessor)
                .content(jsonReserveVacantSeat).accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;
        vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
        log.info("予約可能な空席数。 n:{}, s:{}", vacantSeat.getVacantStandardSeatCount(),
                vacantSeat.getVacantSpecialSeatCount());
        assertEquals("一般席の空席(N)の空席確保が取り消しされたこと。", vacantSeat.getVacantStandardSeatCount().intValue(),
                standardVacantSeats + cancelNormalSeats);

        // トピックのメッセージを確認
        FlightVacantSeatTopic flightVacantSeatTopic = new FlightVacantSeatTopic();
        BeanUtils.copyProperties(vacantSeat, flightVacantSeatTopic);
        checkTopic(flightVacantSeatTopic, FlightVacantSeatTopic.class.getSimpleName());

        reserveId = 2L;
        reserveVacantSeat = reserveVacantSeatRepository.findById(reserveId).get();
        jsonReserveVacantSeat = jsonMapper.writeValueAsString(reserveVacantSeat);

        int cancelSpecialSeats = reserveVacantSeat.getVacantSeatCount().intValue();
        mvc.perform(MockMvcRequestBuilders //
                .post(String.format("/%s/flight/seat/cancel", urlRoot)).with(postProcessor)
                .content(jsonReserveVacantSeat).accept(MediaType.APPLICATION_JSON_UTF8) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isOk()) //
        ;

        vacantSeat = vacantSeatRepository.findById(vacantSeatPk).get();
        log.info("予約可能な空席数。 n:{}, s:{}", vacantSeat.getVacantStandardSeatCount(),
                vacantSeat.getVacantSpecialSeatCount());
        assertEquals("特別席の空席(S)の空席確保が取り消しされたこと。", vacantSeat.getVacantSpecialSeatCount().intValue(),
                specialVacantSeats + cancelSpecialSeats);

        // トピックのメッセージを確認
        BeanUtils.copyProperties(vacantSeat, flightVacantSeatTopic);
        checkTopic(flightVacantSeatTopic, FlightVacantSeatTopic.class.getSimpleName());
    }

    /**
     * Test for POST /flight/seat/cancel.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
     * <li>認証していない場合、HttpStatus(401)が返却されることを確認します。</li>
     * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test602CancelReservedSeat401() throws Exception {
        List<Flight> flights = flightRepository.findAll();
        Flight flight = flights.get(0);
        Long reserveId = 1L;
        LocalDate departureDate = LocalDate.of(2019, 12, 1);
        String flightName = flight.getName();
        int reserveNormalSeats = 1;
        ReserveVacantSeatInfo reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId)
                .departureDate(departureDate).flightName(flightName).seatClass(SeatClass.N)
                .vacantSeatCount(reserveNormalSeats).build();
        String jsonReserveVacantSeat = jsonMapper.writeValueAsString(reserveVacantSeat);

        // 空席確保を取り消す
        mvc.perform(MockMvcRequestBuilders //
                .post(String.format("/%s/flight/seat/cancel", urlRoot)) //
                .content(jsonReserveVacantSeat).accept(MediaType.APPLICATION_JSON_UTF8) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().is(401)) //
        ;
    }

    /**
     * Test for POST /flight/seat/cancel.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
    * <li>空席情報が存在しない場合、HttpStatus(500)が返却されることを確認します。</li>
       * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test603CancelReservedSeat500() throws Exception {

        List<Flight> flights = flightRepository.findAll();
        Flight flight = flights.get(0);

        Long reserveId = 3L;
        LocalDate departureDate = LocalDate.of(2019, 12, 1);
        String flightName = flight.getName();

        VacantSeatPk vacantSeatPk = VacantSeatPk.builder()//
                .flightName(flightName)//
                .departureDate(departureDate)//
                .build();

        vacantSeatRepository.findById(vacantSeatPk).ifPresent(v -> vacantSeatRepository.deleteById(vacantSeatPk));
        int reserveNormalSeats = 1;
        ReserveVacantSeatInfo reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId)
                .departureDate(departureDate).flightName(flightName).seatClass(SeatClass.N)
                .vacantSeatCount(reserveNormalSeats).build();
        String jsonReserveVacantSeat = jsonMapper.writeValueAsString(reserveVacantSeat);
        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");
        // 空席確保を取り消す
        mvc.perform(MockMvcRequestBuilders //
                .post(String.format("/%s/flight/seat/cancel", urlRoot)).with(postProcessor)
                .content(jsonReserveVacantSeat).accept(MediaType.APPLICATION_JSON_UTF8) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;
    }

    /**
     * Test for POST /flight/seat/cancel.
     * 
     * <pre>
     * 異常系テスト。
     * <ul>
    * <li>処理ロジックでエラーが発生した場合、HttpStatus(500)が返却されることを確認する。</li>
       * </ul>
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void test604CancelReservedSeat500() throws Exception {
        List<Flight> flights = flightRepository.findAll();
        Flight flight = flights.get(0);
        Long reserveId = 1L;
        LocalDate departureDate = LocalDate.of(2019, 12, 1);
        String flightName = flight.getName();
        int reserveNormalSeats = 1;
        ReserveVacantSeatInfo reserveVacantSeat = ReserveVacantSeatInfo.builder().reserveId(reserveId)
                .departureDate(departureDate).flightName(flightName).seatClass(SeatClass.N)
                .vacantSeatCount(reserveNormalSeats).build();

        doThrow(new RuntimeException("Something error")).when(cancelReservedSeatService)
                .cancelReservedSeat(reserveVacantSeat);

        RequestPostProcessor postProcessor = oauthHelper.addBearerToken("0", "GUEST");

        String jsonReserveVacantSeat = jsonMapper.writeValueAsString(reserveVacantSeat);
        // 空席確保を取り消す
        mvc.perform(MockMvcRequestBuilders //
                .post(String.format("/%s/flight/seat/cancel", urlRoot)).with(postProcessor)
                .content(jsonReserveVacantSeat).accept(MediaType.APPLICATION_JSON_UTF8) //
                .accept(MediaType.APPLICATION_JSON_UTF8) //
                .contentType(MediaType.APPLICATION_JSON_UTF8) //
        ) //
                .andExpect(status().isInternalServerError()) //
        ;
    }

    /**
     * LocalDateTimeからTimeを抽出する。
     * 
     * @param localDateTime LocalDateTime
     * @return LocalTime
     */
    private LocalTime getTimeFromDateTime(LocalDateTime localDateTime) {
        return LocalTime.of(localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond());
    }
}
