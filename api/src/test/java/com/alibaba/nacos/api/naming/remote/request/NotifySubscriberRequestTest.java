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

package com.alibaba.nacos.api.naming.remote.request;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static com.alibaba.nacos.api.common.Constants.Naming.NAMING_MODULE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotifySubscriberRequestTest {
    
    private static final String SERVICE = "service";
    
    private static final String GROUP = "group";
    
    private static final String NAMESPACE = "namespace";
    
    private static ObjectMapper mapper;
    
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
    void testSerialize() throws JacksonException {
        ServiceInfo serviceInfo = new ServiceInfo(GROUP + "@@" + SERVICE);
        NotifySubscriberRequest request =
            NotifySubscriberRequest.buildNotifySubscriberRequest(serviceInfo);
        request.setServiceName(SERVICE);
        request.setGroupName(GROUP);
        request.setNamespace(NAMESPACE);
        String json = mapper.writeValueAsString(request);
        checkSerializeBasedInfo(json);
        assertTrue(json.contains("\"serviceInfo\":{"));
    }
    
    @Test
    void testDeserialize() throws JacksonException {
        String json =
            "{\"headers\":{},\"namespace\":\"namespace\",\"serviceName\":\"service\",\"groupName\":\"group\","
                + "\"serviceInfo\":{\"name\":\"service\",\"groupName\":\"group\",\"cacheMillis\":1000,\"hosts\":[],"
                + "\"lastRefTime\":0,\"checksum\":\"\",\"allIPs\":false,\"reachProtectionThreshold\":false,"
                + "\"valid\":true},\"module\":\"naming\"}";
        NotifySubscriberRequest actual = mapper.readValue(json, NotifySubscriberRequest.class);
        checkRequestBasedInfo(actual);
        assertEquals(GROUP + "@@" + SERVICE, actual.getServiceInfo().getKey());
    }
    
    private void checkRequestBasedInfo(NotifySubscriberRequest request) {
        assertEquals(SERVICE, request.getServiceName());
        assertEquals(GROUP, request.getGroupName());
        assertEquals(NAMESPACE, request.getNamespace());
        assertEquals(NAMING_MODULE, request.getModule());
    }
    
    private void checkSerializeBasedInfo(String json) {
        assertTrue(json.contains("\"serviceName\":\"" + SERVICE + "\""));
        assertTrue(json.contains("\"groupName\":\"" + GROUP + "\""));
        assertTrue(json.contains("\"namespace\":\"" + NAMESPACE + "\""));
        assertTrue(json.contains("\"module\":\"" + NAMING_MODULE + "\""));
    }
}
