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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientSubscriberInfoTest {
    
    private ObjectMapper mapper;
    
    private ClientSubscriberInfo clientSubscriberInfo;
    
    @BeforeEach
    void setUp() throws Exception {
        mapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        clientSubscriberInfo = new ClientSubscriberInfo();
        clientSubscriberInfo.setClientId("clientId");
        clientSubscriberInfo.setAppName("appName");
        clientSubscriberInfo.setAgent("agent");
        clientSubscriberInfo.setAddress("1.1.1.1:8080");
    }
    
    @Test
    void testSerialize() throws JacksonException {
        String json = mapper.writeValueAsString(clientSubscriberInfo);
        assertTrue(json.contains("\"clientId\":\"clientId\""));
        assertTrue(json.contains("\"appName\":\"appName\""));
        assertTrue(json.contains("\"agent\":\"agent\""));
        assertTrue(json.contains("\"address\":\"1.1.1.1:8080\""));
    }
    
    @Test
    void testDeserialize() throws IOException {
        String jsonString = "{\"clientId\":\"clientId\",\"appName\":\"appName\",\"agent\":\"agent\",\"address\":\"1.1.1.1:8080\"}";
        ClientSubscriberInfo clientSubscriberInfo1 = mapper.readValue(jsonString, ClientSubscriberInfo.class);
        assertEquals(clientSubscriberInfo.getClientId(), clientSubscriberInfo1.getClientId());
        assertEquals(clientSubscriberInfo.getAppName(), clientSubscriberInfo1.getAppName());
        assertEquals(clientSubscriberInfo.getAgent(), clientSubscriberInfo1.getAgent());
        assertEquals(clientSubscriberInfo.getAddress(), clientSubscriberInfo1.getAddress());
    }
}