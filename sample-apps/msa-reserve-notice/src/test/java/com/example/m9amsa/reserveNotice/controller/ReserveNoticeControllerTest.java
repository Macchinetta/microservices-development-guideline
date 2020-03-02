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
package com.example.m9amsa.reserveNotice.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.m9amsa.reserveNotice.service.ReserveNoticeService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_RESERVE_NOTICE=localhost:5432",
        "JAEGER_HOST=localhost", "HOSTNAME_APIGATEWAY=http://localhost", "HOSTNAME_FLIGHT=localhost:28081" })
public class ReserveNoticeControllerTest {

    @Mock
    private ReserveNoticeService service;

    @InjectMocks
    private ReserveNoticeController controller;

    @Captor
    ArgumentCaptor<String> captor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetReserveCompleteNotice() throws Exception {
        String emailId = "001@ntt.com";
        controller.getReserveCompleteNotice(emailId);
        verify(service).getReserveCompleteNotice(captor.capture());
        assertThat("ReserveNoticeServiceへのパラメータが正しいこと", captor.getValue(), equalTo(emailId));

    }
}
