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
package com.example.m9amsa.reserve.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

import feign.RequestInterceptor;

@Configuration
@EnableResourceServer
public class SecurityConfig extends ResourceServerConfigurerAdapter {

    @Value("${security.oauth2.client.access-token-uri}")
    private String oauth2ClientAccessTokenUri;

    @Value("${security.oauth2.client.client-id}")
    private String oauth2ClientId;

    @Value("${security.oauth2.client.client-secret}")
    private String oauth2ClientSecret;

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
                .antMatchers(String.format("/%s/reserve/**", rootPath)).authenticated()//
                .antMatchers("/actuator", "/actuator/**").permitAll() //
                .anyRequest().denyAll();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(resource.getResourceId());
    }

    /**
     * Feignのリクエストに対するRequestInterceptorのBean定義
     * 
     * @param context OAuth2のクライアント情報定義
     * @return クライアント情報定義を設定したOAuth2FeignRequestInterceptor
     */
    @Bean
    public RequestInterceptor oauth2feinRequestInterceptor(OAuth2ClientContext context) {
        return new OAuth2FeignRequestInterceptor(context, oAuth2ProtectedResourceDetails());
    }

    /**
     * OAuth2の保護情報リソース(ここではOAuth2のクライアント情報)を生成します
     * 
     * @return OAuth2のクライアント情報を設定したリソース
     */
    private OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails() {
        ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
        details.setAccessTokenUri(oauth2ClientAccessTokenUri);
        details.setClientId(oauth2ClientId);
        details.setClientSecret(oauth2ClientSecret);
        return details;
    }
}
