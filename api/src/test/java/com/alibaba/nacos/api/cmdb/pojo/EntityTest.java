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

package com.alibaba.nacos.api.cmdb.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityTest {
    
    ObjectMapper mapper = new ObjectMapper();
    
    @BeforeEach
    void setUp() throws Exception {
        mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .changeDefaultPropertyInclusion(
                incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .changeDefaultPropertyInclusion(
                incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
            .build();
    }
    
    @Test
    void testSerialization() throws JacksonException {
        Entity entity = new Entity();
        entity.setName("test-entity");
        entity.setType(PreservedEntityTypes.ip.name());
        entity.setLabels(Collections.singletonMap("test-label-key", "test-label-value"));
        String actual = mapper.writeValueAsString(entity);
        assertTrue(actual.contains("\"type\":\"ip\""));
        assertTrue(actual.contains("\"name\":\"test-entity\""));
        assertTrue(actual.contains("\"labels\":{\"test-label-key\":\"test-label-value\"}"));
    }
    
    @Test
    void testDeserialization() throws JacksonException {
        String json =
            "{\"type\":\"service\",\"name\":\"test-entity\",\"labels\":{\"test-label-key\":\"test-label-value\"}}";
        Entity entity = mapper.readValue(json, Entity.class);
        assertEquals("test-entity", entity.getName());
        assertEquals(PreservedEntityTypes.service.name(), entity.getType());
        assertEquals(1, entity.getLabels().size());
        assertTrue(entity.getLabels().containsKey("test-label-key"));
        assertEquals("test-label-value", entity.getLabels().get("test-label-key"));
    }
}
