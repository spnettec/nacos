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

package com.alibaba.nacos.api.config.ability;

import com.alibaba.nacos.api.ability.ClientAbilities;
import com.alibaba.nacos.api.remote.ability.ServerRemoteAbility;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerRemoteAbilityTest {
    
    private static ObjectMapper mapper;
    
    private ServerRemoteAbility serverAbilities;
    
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        mapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
    }
    
    @BeforeEach
    void setUp() throws Exception {
        serverAbilities = new ServerRemoteAbility();
    }
    
    @Test
    void testSerialize() throws JacksonException {
        serverAbilities = new ServerRemoteAbility();
        String json = mapper.writeValueAsString(serverAbilities);
        assertTrue(json.contains("\"supportRemoteConnection\":false"));
        assertTrue(json.contains("\"grpcReportEnabled\":true"));
    }
    
    @Test
    void testDeserialize() throws JacksonException {
        String json = "{\"supportRemoteConnection\":true,\"grpcReportEnabled\":true}";
        ServerRemoteAbility abilities = mapper.readValue(json, ServerRemoteAbility.class);
        assertTrue(abilities.isSupportRemoteConnection());
        assertTrue(abilities.isGrpcReportEnabled());
    }
    
    @Test
    void testEqualsAndHashCode() {
        assertEquals(serverAbilities, serverAbilities);
        assertEquals(serverAbilities.hashCode(), serverAbilities.hashCode());
        assertNotEquals(null, serverAbilities);
        assertNotEquals(serverAbilities, new ClientAbilities());
        ServerRemoteAbility test = new ServerRemoteAbility();
        assertEquals(serverAbilities, test);
        assertEquals(serverAbilities.hashCode(), test.hashCode());
        test.setSupportRemoteConnection(true);
        assertNotEquals(serverAbilities, test);
        assertNotEquals(serverAbilities.hashCode(), test.hashCode());
        test.setSupportRemoteConnection(false);
        test.setGrpcReportEnabled(false);
        assertNotEquals(serverAbilities, test);
        assertNotEquals(serverAbilities.hashCode(), test.hashCode());
    }
}