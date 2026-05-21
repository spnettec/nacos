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

package com.alibaba.nacos.core.remote;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.model.response.ConnectionInfo;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.core.remote.grpc.GrpcConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionTest {
    
    ConnectionMeta connectionMeta;
    
    Connection connection;
    
    @BeforeEach
    void setUp() {
        connectionMeta = new ConnectionMeta("1739168690942_127.0.0.1_18080", "127.0.0.1",
            "127.0.0.1", 8080, 18080,
            "grpc", "3.0.0", "test", Collections.singletonMap(Constants.APPNAME, "test"));
        connectionMeta.setNamespaceId("public");
        connection = new GrpcConnection(connectionMeta, null, null);
        connection.setAbilityTable(Collections.emptyMap());
    }
    
    @AfterEach
    void tearDown() {
    }
    
    @Test
    public void testSerialize() {
        String json = JacksonUtils.toJson(connection);
        JsonNode root = JacksonUtils.toObj(json);
        JsonNode metaInfo = root.get("metaInfo");
        assertFalse(root.get("traced").asBoolean());
        assertEquals("grpc", metaInfo.get("connectType").asText());
        assertEquals("127.0.0.1", metaInfo.get("clientIp").asText());
        assertEquals("127.0.0.1", metaInfo.get("remoteIp").asText());
        assertEquals(8080, metaInfo.get("remotePort").asInt());
        assertEquals(18080, metaInfo.get("localPort").asInt());
        assertEquals("3.0.0", metaInfo.get("version").asText());
        assertEquals("1739168690942_127.0.0.1_18080", metaInfo.get("connectionId").asText());
        assertEquals(connection.getMetaInfo().getCreateTime().getTime(),
            parseDateMillis(metaInfo.get("createTime")));
        assertEquals(connection.getMetaInfo().getLastActiveTime(),
            metaInfo.get("lastActiveTime").asLong());
        assertEquals("test", metaInfo.get("appName").asText());
        assertEquals("test", metaInfo.get("labels").get("AppName").asText());
        assertFalse(metaInfo.get("sdkSource").asBoolean());
        assertFalse(metaInfo.get("clusterSource").asBoolean());
        assertFalse(root.get("connected").asBoolean());
        assertTrue(root.get("abilityTable").isEmpty());
        assertEquals("public", metaInfo.get("namespaceId").asText());
    }

    private long parseDateMillis(JsonNode node) {
        return node.isNumber() ? node.asLong() : Instant.parse(node.asText()).toEpochMilli();
    }
    
    @Test
    public void testDeserializeToConnectionInfo() {
        String json = JacksonUtils.toJson(connection);
        ConnectionInfo connectionInfo = JacksonUtils.toObj(json, ConnectionInfo.class);
        assertEquals(connection.isTraced(), connectionInfo.isTraced());
        assertEquals(connection.getAbilityTable(), connectionInfo.getAbilityTable());
        assertEquals(connection.getMetaInfo().getConnectType(),
            connectionInfo.getMetaInfo().getConnectType());
        assertEquals(connection.getMetaInfo().getClientIp(),
            connectionInfo.getMetaInfo().getClientIp());
        assertEquals(connection.getMetaInfo().getRemoteIp(),
            connectionInfo.getMetaInfo().getRemoteIp());
        assertEquals(connection.getMetaInfo().getRemotePort(),
            connectionInfo.getMetaInfo().getRemotePort());
        assertEquals(connection.getMetaInfo().getLocalPort(),
            connectionInfo.getMetaInfo().getLocalPort());
        assertEquals(connection.getMetaInfo().getVersion(),
            connectionInfo.getMetaInfo().getVersion());
        assertEquals(connection.getMetaInfo().getConnectionId(),
            connectionInfo.getMetaInfo().getConnectionId());
        assertEquals(connection.getMetaInfo().getCreateTime().getTime(),
            connectionInfo.getMetaInfo().getCreateTime().getTime());
        assertEquals(connection.getMetaInfo().getLastActiveTime(),
            connectionInfo.getMetaInfo().getLastActiveTime());
        assertEquals(connection.getMetaInfo().getAppName(),
            connectionInfo.getMetaInfo().getAppName());
        assertEquals(connection.getMetaInfo().getLabels(),
            connectionInfo.getMetaInfo().getLabels());
        assertEquals(connection.getMetaInfo().getNamespaceId(),
            connectionInfo.getMetaInfo().getNamespaceId());
    }
}
