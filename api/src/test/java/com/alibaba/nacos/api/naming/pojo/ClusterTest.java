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

package com.alibaba.nacos.api.naming.pojo;

import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Http;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Tcp;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterTest {
    
    private static ObjectMapper mapper;
    
    @BeforeAll
    static void setUp() throws Exception {
        mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }
    
    @Test
    void testSetAndGet() {
        Cluster actual = new Cluster();
        assertNull(actual.getName());
        assertNull(actual.getServiceName());
        assertEquals(Tcp.TYPE, actual.getHealthChecker().getType());
        assertEquals(80, actual.getDefaultPort());
        assertEquals(80, actual.getDefaultCheckPort());
        assertTrue(actual.isUseIpPort4Check());
        assertTrue(actual.getMetadata().isEmpty());
        actual.setName("cluster");
        actual.setServiceName("group@@service");
        actual.setHealthChecker(new Http());
        actual.setDefaultPort(81);
        actual.setDefaultCheckPort(82);
        actual.setUseIpPort4Check(false);
        actual.setMetadata(Collections.singletonMap("a", "a"));
        assertEquals("cluster", actual.getName());
        assertEquals("group@@service", actual.getServiceName());
        assertEquals(Http.TYPE, actual.getHealthChecker().getType());
        assertEquals(81, actual.getDefaultPort());
        assertEquals(82, actual.getDefaultCheckPort());
        assertFalse(actual.isUseIpPort4Check());
        assertFalse(actual.getMetadata().isEmpty());
        assertTrue(actual.getMetadata().containsKey("a"));
        assertEquals("a", actual.getMetadata().get("a"));
    }
    
    @Test
    void testJsonSerialize() throws JacksonException {
        Cluster actual = new Cluster("cluster");
        actual.setServiceName("group@@service");
        actual.setHealthChecker(new Http());
        actual.setDefaultPort(81);
        actual.setDefaultCheckPort(82);
        actual.setUseIpPort4Check(false);
        actual.setMetadata(Collections.singletonMap("a", "a"));
        String json = mapper.writeValueAsString(actual);
        assertTrue(json.contains("\"serviceName\":\"group@@service\""));
        assertTrue(json.contains("\"name\":\"cluster\""));
        assertTrue(json.contains("\"type\":\"HTTP\""));
        assertTrue(json.contains("\"defaultPort\":81"));
        assertTrue(json.contains("\"defaultCheckPort\":82"));
        assertTrue(json.contains("\"useIpPort4Check\":false"));
        assertTrue(json.contains("\"metadata\":{\"a\":\"a\"}"));
    }
    
    @Test
    void testJsonDeserialize() throws JacksonException {
        String json = "{\"serviceName\":\"group@@service\",\"name\":\"cluster\","
                + "\"healthChecker\":{\"type\":\"HTTP\",\"path\":\"\",\"headers\":\"\",\"expectedResponseCode\":200},"
                + "\"defaultPort\":81,\"defaultCheckPort\":82,\"useIpPort4Check\":false,\"metadata\":{\"a\":\"a\"}}";
        Cluster actual = mapper.readValue(json, Cluster.class);
        assertEquals("cluster", actual.getName());
        assertEquals("group@@service", actual.getServiceName());
        assertEquals(Http.TYPE, actual.getHealthChecker().getType());
        assertEquals(81, actual.getDefaultPort());
        assertEquals(82, actual.getDefaultCheckPort());
        assertFalse(actual.isUseIpPort4Check());
        assertFalse(actual.getMetadata().isEmpty());
        assertTrue(actual.getMetadata().containsKey("a"));
        assertEquals("a", actual.getMetadata().get("a"));
    }
}