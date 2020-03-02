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
package com.example.m9amsa.reserveNotice.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * RSS Root要素。
 * 
 */
@Data
@NoArgsConstructor
@XmlRootElement
@SuperBuilder(toBuilder = true)
@JacksonXmlRootElement(localName = "rss")
public class RssRoot implements Serializable {

    private static final long serialVersionUID = -5397480313017039587L;

    /**
     * RSSバージョン。
     */
    @JacksonXmlProperty(localName = "version", isAttribute = true)
    private String version;

    /**
     * チャネル。
     */
    @JacksonXmlProperty
    private RssChannel channel;

}
