/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NamingFuzzyWatchSyncResponseTest {
    
    protected static ObjectMapper mapper;
    
    @BeforeAll
    static void setUp() throws Exception {
        mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .changeDefaultPropertyInclusion(
                incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .changeDefaultPropertyInclusion(
                incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
            .build();
    }
    
    @Test
    void testSerializeSuccessResponse() throws JacksonException {
        NamingFuzzyWatchSyncResponse response = NamingFuzzyWatchSyncResponse.buildSuccessResponse();
        response.setResultCode(200);
        String json = mapper.writeValueAsString(response);
        assertTrue(json.contains("\"resultCode\":200"));
        assertTrue(json.contains("\"success\":true"));
    }
    
    @Test
    void testSerializeFailResponse() throws JacksonException {
        NamingFuzzyWatchSyncResponse response =
            NamingFuzzyWatchSyncResponse.buildFailResponse("test");
        String json = mapper.writeValueAsString(response);
        assertTrue(json.contains("\"resultCode\":500"));
        assertTrue(json.contains("\"errorCode\":500"));
        assertTrue(json.contains("\"message\":\"test\""));
        assertTrue(json.contains("\"success\":false"));
    }
    
    @Test
    void testDeserialize() throws JacksonException {
        String json = "{\"resultCode\":200,\"errorCode\":0,\"success\":true}";
        NamingFuzzyWatchSyncResponse response =
            mapper.readValue(json, NamingFuzzyWatchSyncResponse.class);
        assertTrue(response.isSuccess());
    }
}
