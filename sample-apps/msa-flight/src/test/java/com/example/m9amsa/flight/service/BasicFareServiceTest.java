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
package com.example.m9amsa.flight.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.flight.entity.BasicFare;
import com.example.m9amsa.flight.entity.BasicFareRepository;
import com.example.m9amsa.flight.model.BasicFareInfo;

import reactor.util.function.Tuple2;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class BasicFareServiceTest {

    @Autowired
    private BasicFareService basicFareService;

    @MockBean
    private BasicFareRepository basicFareRepository;

    @Captor
    private ArgumentCaptor<BasicFare> basicFareCaptor;

    @Captor
    private ArgumentCaptor<Example<BasicFare>> exampleCaptor;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        reset(basicFareRepository);
    }

    /**
     * Test for addBasicFare.
     */
    @Test
    public void testAddBasicFare() {
        BasicFareInfo basicFare = BasicFareInfo.builder().departure("HND").arrival("OSA").fare(30000).build();
        BasicFareInfo returnFare = BasicFareInfo.builder().departure(basicFare.getArrival())
                .arrival(basicFare.getDeparture()).fare(basicFare.getFare()).build();

        when(basicFareRepository.save(any(BasicFare.class))).then(i -> i.getArgument(0));

        clearInvocations(basicFareRepository);
        Tuple2<BasicFare, BasicFare> actualBasicFare = basicFareService.addBasicFare(basicFare);

        verify(basicFareRepository, times(2)).save(basicFareCaptor.capture());

        assertThat("basicFareRepositoryへのパラメータが正しいこと", basicFareCaptor.getAllValues(),
                contains(basicFare.asEntity(), returnFare.asEntity()));

        assertThat("BasicFare.departure/1", actualBasicFare.getT1().getDeparture(), equalTo(basicFare.getDeparture()));
        assertThat("BasicFare.arrival/1", actualBasicFare.getT1().getArrival(), equalTo(basicFare.getArrival()));
        assertThat("BasicFare.fare/1", actualBasicFare.getT1().getFare(), equalTo(basicFare.getFare()));
        assertThat("BasicFare.departure/2", actualBasicFare.getT2().getDeparture(), equalTo(returnFare.getDeparture()));
        assertThat("BasicFare.arrival/2", actualBasicFare.getT2().getArrival(), equalTo(returnFare.getArrival()));
        assertThat("BasicFare.fare/2", actualBasicFare.getT2().getFare(), equalTo(returnFare.getFare()));
    }

    /**
     * Test for findBasicFareList.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testFindBasicFareList() {
        BasicFare basicFare = BasicFare.builder().departure("HND").arrival("OSA").fare(30000).build();
        BasicFare returnFare = BasicFare.builder().departure(basicFare.getArrival()).arrival(basicFare.getDeparture())
                .fare(basicFare.getFare()).build();

        Example<BasicFare> dummyExp = Example.of(basicFare);
        when(basicFareRepository.findAll(any(dummyExp.getClass()))).thenReturn(Arrays.asList(basicFare, returnFare));

        // パラメータ null, null
        clearInvocations(basicFareRepository);
        List<BasicFare> actualList = basicFareService.findBasicFareList(Optional.empty(), Optional.empty());

        verify(basicFareRepository).findAll(exampleCaptor.capture());

        assertThat("検索条件が正しいこと", exampleCaptor.getValue().getProbe(), equalTo(BasicFare.builder().build()));

        assertThat("リストサイズは2であること", actualList.size(), equalTo(2));
        assertThat("リストの内容、順序が正しいこと", actualList, contains(basicFare, returnFare));

        // パラメータ HND, null
        clearInvocations(basicFareRepository);
        actualList = basicFareService.findBasicFareList(Optional.of("HND"), Optional.empty());

        verify(basicFareRepository).findAll(exampleCaptor.capture());

        assertThat("検索条件が正しいこと", exampleCaptor.getValue().getProbe(),
                equalTo(BasicFare.builder().departure("HND").build()));

        // パラメータ null, OSA
        clearInvocations(basicFareRepository);
        actualList = basicFareService.findBasicFareList(Optional.empty(), Optional.of("OSA"));

        verify(basicFareRepository).findAll(exampleCaptor.capture());

        assertThat("検索条件が正しいこと", exampleCaptor.getValue().getProbe(),
                equalTo(BasicFare.builder().arrival("OSA").build()));

        // パラメータ HND, OSA
        clearInvocations(basicFareRepository);
        actualList = basicFareService.findBasicFareList(Optional.of("HND"), Optional.of("OSA"));

        verify(basicFareRepository).findAll(exampleCaptor.capture());

        assertThat("検索条件が正しいこと", exampleCaptor.getValue().getProbe(),
                equalTo(BasicFare.builder().departure("HND").arrival("OSA").build()));
    }
}
