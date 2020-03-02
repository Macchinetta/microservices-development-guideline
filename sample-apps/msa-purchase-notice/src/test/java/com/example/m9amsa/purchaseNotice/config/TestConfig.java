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
package com.example.m9amsa.purchaseNotice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

// PurchaseNoticeApplication*Testから@Importして使う想定
// テストクラスで@AutoConfigureWireMockを設定すること
public class TestConfig {

    @Autowired
    Environment env;

    @Bean
    ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

    @Bean
    public ServerList<Server> ribbonServerList() {
        return new StaticServerList<Server>(
                new Server("localhost", Integer.valueOf(this.env.getProperty("wiremock.server.port"))));
    }
}
