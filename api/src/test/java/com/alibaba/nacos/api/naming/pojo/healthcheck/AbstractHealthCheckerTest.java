/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.naming.pojo.healthcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.NamedType;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractHealthCheckerTest {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerSubtypes(new NamedType(TestChecker.class, TestChecker.TYPE))
            .build();
    }
    
    @Test
    void testSerialize() throws JacksonException {
        TestChecker testChecker = new TestChecker();
        testChecker.setTestValue("");
        String actual = objectMapper.writeValueAsString(testChecker);
        assertTrue(actual.contains("\"testValue\":\"\""));
        assertTrue(actual.contains("\"type\":\"TEST\""));
    }
    
    @Test
    void testDeserialize() throws IOException {
        String testChecker = "{\"type\":\"TEST\",\"testValue\":\"\"}";
        TestChecker actual = objectMapper.readValue(testChecker, TestChecker.class);
        assertEquals("", actual.getTestValue());
        assertEquals(TestChecker.TYPE, actual.getType());
    }
    
    @Test
    void testClone() throws CloneNotSupportedException {
        AbstractHealthChecker none = new AbstractHealthChecker.None().clone();
        assertEquals(AbstractHealthChecker.None.class, none.getClass());
    }
}
