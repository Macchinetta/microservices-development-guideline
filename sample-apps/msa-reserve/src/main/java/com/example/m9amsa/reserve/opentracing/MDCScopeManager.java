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
package com.example.m9amsa.reserve.opentracing;

import org.slf4j.MDC;

import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.ThreadLocalScopeManager;

public class MDCScopeManager extends ThreadLocalScopeManager {

    @Override
    public Scope activate(Span span, boolean finishOnClose) {
        return new ScopeWrapper(super.activate(span, finishOnClose), span);
    }

    @Override
    public Scope activate(Span span) {
        return new ScopeWrapper(super.activate(span), span);
    }

    private static class ScopeWrapper implements Scope {

        private final Scope scope;
        private final Span span;
        private final String previousTraceId;
        private final String previousSpanId;
        private final String previousParentSpanId;

        ScopeWrapper(Scope scope, Span span) {
            this.scope = scope;
            this.span = span;
            this.previousTraceId = lookup("traceId");
            this.previousSpanId = lookup("spanId");
            this.previousParentSpanId = lookup("parentSpanId");

            JaegerSpanContext ctx = (JaegerSpanContext) span.context();
            String traceId = ctx.getTraceId();
            String spanId = Long.toHexString(ctx.getSpanId());
            String parentSpanId = Long.toHexString(ctx.getParentId());

            replace("traceId", traceId);
            replace("spanId", spanId);
            replace("parentSpanId", parentSpanId);
        }

        @Override
        public void close() {
            this.scope.close();
            replace("traceId", previousTraceId);
            replace("spanId", previousSpanId);
            replace("parentSpanId", previousParentSpanId);
        }

        @Override
        public Span span() {
            return this.span;
        }
    }

    private static String lookup(String key) {
        return MDC.get(key);
    }

    private static void replace(String key, String value) {
        if (value == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, value);
        }
    }
}
