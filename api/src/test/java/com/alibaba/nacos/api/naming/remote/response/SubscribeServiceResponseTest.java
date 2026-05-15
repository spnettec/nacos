/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.naming.remote.response;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscribeServiceResponseTest {
    
    protected static ObjectMapper mapper;
    
    @BeforeAll
    static void setUp() throws Exception {
        mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }
    
    @Test
    void testSerializeSuccessResponse() throws JacksonException {
        SubscribeServiceResponse response =
            new SubscribeServiceResponse(200, null, new ServiceInfo());
        String json = mapper.writeValueAsString(response);
        assertTrue(json.contains("\"serviceInfo\":{"));
        assertTrue(json.contains("\"resultCode\":200"));
        assertTrue(json.contains("\"errorCode\":0"));
        assertTrue(json.contains("\"success\":true"));
    }
    
    @Test
    void testSerializeFailResponse() throws JacksonException {
        SubscribeServiceResponse response = new SubscribeServiceResponse(500, "test", null);
        String json = mapper.writeValueAsString(response);
        assertTrue(json.contains("\"resultCode\":500"));
        assertTrue(json.contains("\"errorCode\":0"));
        assertTrue(json.contains("\"message\":\"test\""));
        assertTrue(json.contains("\"success\":false"));
    }
    
    @Test
    void testDeserialize() throws JacksonException {
        String json =
            "{\"resultCode\":200,\"errorCode\":0,\"serviceInfo\":{\"cacheMillis\":1000,\"hosts\":[],"
                + "\"lastRefTime\":0,\"checksum\":\"\",\"allIPs\":false,\"reachProtectionThreshold\":false,"
                + "\"valid\":true},\"success\":true}";
        SubscribeServiceResponse response = mapper.readValue(json, SubscribeServiceResponse.class);
        assertNotNull(response.getServiceInfo());
    }
    
}
