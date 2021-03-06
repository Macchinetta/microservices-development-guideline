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
package com.example.m9amsa.flight.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
public class SecurityConfig extends ResourceServerConfigurerAdapter {

    @Value("${info.url.root-path}")
    private String rootPath;

    private ResourceServerProperties resource;

    public SecurityConfig(ResourceServerProperties resource) {
        this.resource = resource;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()//
                .httpBasic().disable()//
                .formLogin().disable()//
                .logout().disable()//
                .authorizeRequests()//
                .antMatchers(String.format("/%s/airplane", rootPath), String.format("/%s/airplane/*", rootPath))
                .authenticated()//
                .regexMatchers(HttpMethod.GET, String.format("/%s/airport/[A-Z]{3}$", rootPath)) //
                .permitAll()// IDを指定した空港情報取得（空港名称取得）はすべてのアクセスを許可します
                .antMatchers(String.format("/%s/airport", rootPath), String.format("/%s/airport/*", rootPath))
                .authenticated()//
                .antMatchers(String.format("/%s/basic-fare", rootPath), String.format("/%s/basic-fare/*", rootPath))
                .authenticated()//
                .antMatchers(String.format("/%s/flight", rootPath), String.format("/%s/flight/**", rootPath))
                .authenticated()//
                .antMatchers("/actuator", "/actuator/**").permitAll() //
                .anyRequest().denyAll();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(resource.getResourceId());
    }
}
