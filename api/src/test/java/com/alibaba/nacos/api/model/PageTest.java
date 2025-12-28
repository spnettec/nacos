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

package com.alibaba.nacos.api.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageTest {
    
    private ObjectMapper mapper;
    
    private Page<String> page;
    
    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        page = new Page<>();
        page.setPagesAvailable(10);
        page.setPageNumber(1);
        page.setTotalCount(10);
        page.setPageItems(Collections.singletonList("test"));
    }
    
    @Test
    void setPageItems() {
        Page<Object> page = new Page<>();
        assertEquals(0, page.getPageItems().size());
        page.setPageItems(Collections.singletonList(new Object()));
        assertEquals(1, page.getPageItems().size());
    }
    
    @Test
    void testSerialize() throws Exception {
        String json =  mapper.writeValueAsString(page);
        assertTrue(json.contains("\"totalCount\":10"));
        assertTrue(json.contains("\"pageNumber\":1"));
        assertTrue(json.contains("\"pagesAvailable\":10"));
        assertTrue(json.contains("\"pageItems\":[\"test\"]"));
    }
    
    @Test
    void testDeserialize() throws Exception {
        String json =  "{\"totalCount\":10,\"pageNumber\":1,\"pagesAvailable\":10,\"pageItems\":[\"test\"]}";
        Page<String> page = mapper.readValue(json, new TypeReference<Page<String>>() {
        });
        assertEquals(10, page.getPagesAvailable());
        assertEquals(1, page.getPageNumber());
        assertEquals(10, page.getTotalCount());
        assertEquals(1, page.getPageItems().size());
        assertEquals("test", page.getPageItems().get(0));
    }
}