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

package com.alibaba.nacos.api.selector;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.NamedType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionSelectorTest {
    
    ObjectMapper mapper;
    
    @BeforeEach
    void setUp() throws Exception {
        mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
                .registerSubtypes(new NamedType(ExpressionSelector.class, SelectorType.label.name()))
                .build();
    }
    
    @Test
    void testSerialization() throws JacksonException {
        ExpressionSelector selector = new ExpressionSelector();
        selector.setExpression("test expression");
        String actual = mapper.writeValueAsString(selector);
        assertTrue(actual.contains("\"type\":\"" + SelectorType.label.name() + "\""));
        assertTrue(actual.contains("\"expression\":\"test expression\""));
    }
    
    @Test
    void testDeserialization() throws JacksonException {
        String json = "{\"type\":\"label\",\"expression\":\"test expression\"}";
        AbstractSelector actual = mapper.readValue(json, AbstractSelector.class);
        assertEquals(SelectorType.label.name(), actual.getType());
        ExpressionSelector selector = (ExpressionSelector) actual;
        assertEquals("test expression", selector.getExpression());
    }
}
