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

class LabelTest {
    
    ObjectMapper mapper = new ObjectMapper();
    
    @BeforeEach
    void setUp() throws Exception {
        mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }
    
    @Test
    void testSerialization() throws JacksonException {
        Label label = new Label();
        label.setName("test-label");
        label.setDescription("CMDB description");
        label.setValues(Collections.singletonMap("test-value", "test-value").keySet());
        String actual = mapper.writeValueAsString(label);
        System.out.println(actual);
        assertTrue(actual.contains("\"name\":\"test-label\""));
        assertTrue(actual.contains("\"description\":\"CMDB description\""));
        assertTrue(actual.contains("\"values\":[\"test-value\"]"));
    }
    
    @Test
    void testDeserialization() throws JacksonException {
        String json =
            "{\"values\":[\"test-value\"],\"name\":\"test-label\",\"description\":\"CMDB description\"}";
        Label label = mapper.readValue(json, Label.class);
        assertEquals("test-label", label.getName());
        assertEquals("CMDB description", label.getDescription());
        assertEquals(1, label.getValues().size());
        assertEquals("test-value", label.getValues().iterator().next());
    }
    
}
