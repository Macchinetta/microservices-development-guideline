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
package com.example.m9amsa.purchase.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * SpringのBeanコンテナからオブジェクトタイプを指定してBeanを取得するUtilクラス。
 * 
 */
@Component
public class BeanUtil implements ApplicationContextAware {
    private static ApplicationContext context;

    /**
     * アプリケーションコンテキストを設定します。
     * 
     * @param applicationContext アプリケーションのコンテキスト。
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * 指定されたオブジェクトタイプが存在する場合、一意に一致するBeanインスタンスを返します。
     * 
     * @param <T>       Type Parameter。
     * @param beanClass 一致したいタイプBean。 インターフェイスまたはスーパークラスにすることができます。
     * @return 一意に一致するBeanインスタンスを返します。
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

}
