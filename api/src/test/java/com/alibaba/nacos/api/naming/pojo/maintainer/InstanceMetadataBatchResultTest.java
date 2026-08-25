/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.naming.pojo.maintainer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstanceMetadataBatchResultTest {
    
    private ObjectMapper mapper;
    
    private InstanceMetadataBatchResult instanceMetadataBatchResult;
    
    @BeforeEach
    void setUp() throws Exception {
        mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
        instanceMetadataBatchResult = new InstanceMetadataBatchResult();
        instanceMetadataBatchResult.setUpdated(Collections.singletonList("1.1.1.1"));
    }
    
    @Test
    void testSerialize() throws JacksonException {
        String json = mapper.writeValueAsString(instanceMetadataBatchResult);
        assertTrue(json.contains("\"updated\":[\"1.1.1.1\"]"));
    }
    
    @Test
    void testDeserialize() throws IOException {
        String jsonString = "{\"updated\":[\"1.1.1.1\"]}";
        InstanceMetadataBatchResult metricsInfo1 =
            mapper.readValue(jsonString, InstanceMetadataBatchResult.class);
        assertEquals(instanceMetadataBatchResult.getUpdated(), metricsInfo1.getUpdated());
    }
    
    @Test
    void testConstructorWithParameter() {
        InstanceMetadataBatchResult result =
            new InstanceMetadataBatchResult(Collections.singletonList("2.2.2.2"));
        assertEquals(1, result.getUpdated().size());
        assertEquals("2.2.2.2", result.getUpdated().get(0));
    }
}
