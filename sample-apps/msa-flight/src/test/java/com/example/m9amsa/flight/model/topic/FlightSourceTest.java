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
package com.example.m9amsa.flight.model.topic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "URL_ROOT=msaref", "DB_HOSTNAME_FLIGHT=localhost:5432", "JAEGER_HOST=localhost",
        "HOSTNAME_ACCOUNT=localhost" })
public class FlightSourceTest {

    @Test
    public void testFlightSource() {
        assertThat("バインド名が正しいこと", FlightTopicSource.OUTPUT, equalTo("flight_output"));

        try {
            Method m = FlightTopicSource.class.getMethod("output");

            assertTrue("output()メソッドに@Outputが指定されていること", m.isAnnotationPresent(Output.class));
            assertThat("@Output(\"flight_output\")であること", m.getAnnotation(Output.class).value(),
                    equalTo(FlightTopicSource.OUTPUT));

        } catch (Exception e) {
            fail("outputメソッドが定義されていない：" + e.getMessage());
        }
    }
}
