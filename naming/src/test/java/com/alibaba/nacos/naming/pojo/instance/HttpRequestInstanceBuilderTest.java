/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.pojo.instance;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.CommonParams;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.StandardEnvironment;

import jakarta.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
// todo remove this
@MockitoSettings(strictness = Strictness.LENIENT)
class HttpRequestInstanceBuilderTest {
    
    private static final String SERVICE = "service";
    
    private static final String IP = "127.0.0.1";
    
    private static final String PORT = "8848";
    
    @Mock
    private HttpServletRequest request;
    
    private HttpRequestInstanceBuilder builder;
    
    @BeforeAll
    static void setUpBeforeClass() {
        NacosServiceLoader.load(InstanceExtensionHandler.class);
        EnvUtil.setEnvironment(new StandardEnvironment());
    }
    
    @BeforeEach
    void setUp() throws Exception {
        builder = HttpRequestInstanceBuilder.newBuilder();
        when(request.getParameter(CommonParams.SERVICE_NAME)).thenReturn("service");
        when(request.getParameter("ip")).thenReturn(IP);
        when(request.getParameter("port")).thenReturn(PORT);
        builder.setDefaultInstanceEphemeral(true);
    }
    
    @Test
    void testBuildSimple() throws NacosException {
        Instance actual = builder.setRequest(request).build();
        assertThat(actual.getServiceName(), is(SERVICE));
        assertThat(actual.getIp(), is(IP));
        assertThat(actual.getPort(), is(Integer.parseInt(PORT)));
        assertThat(actual.getClusterName(), is(UtilsAndCommons.DEFAULT_CLUSTER_NAME));
        assertThat(actual.getWeight(), is(1.0));
        assertTrue(actual.isEphemeral());
        assertTrue(actual.isEnabled());
        assertTrue(actual.isHealthy());
        assertThat(actual.getInstanceId(), is(IP + "#" + PORT + "#" + UtilsAndCommons.DEFAULT_CLUSTER_NAME + "#" + SERVICE));
        assertThat(actual.getMetadata().size(), is(1));
        assertThat(actual.getMetadata().get("mock"), is("mock"));
        verify(request).getParameter("mock");
    }
    
    @Test
    void testBuildFull() throws NacosException {
        when(request.getParameter("weight")).thenReturn("2");
        when(request.getParameter("healthy")).thenReturn("false");
        when(request.getParameter("enabled")).thenReturn("false");
        when(request.getParameter("ephemeral")).thenReturn("false");
        when(request.getParameter("metadata")).thenReturn("{\"a\":\"b\"}");
        when(request.getParameter(CommonParams.CLUSTER_NAME)).thenReturn("cluster");
        Instance actual = builder.setRequest(request).build();
        assertThat(actual.getServiceName(), is(SERVICE));
        assertThat(actual.getIp(), is(IP));
        assertThat(actual.getPort(), is(Integer.parseInt(PORT)));
        assertThat(actual.getClusterName(), is("cluster"));
        assertThat(actual.getWeight(), is(2.0));
        assertFalse(actual.isEphemeral());
        assertFalse(actual.isEnabled());
        assertFalse(actual.isHealthy());
        assertThat(actual.getInstanceId(), is(IP + "#" + PORT + "#" + "cluster" + "#" + SERVICE));
        assertThat(actual.getMetadata().size(), is(2));
        assertThat(actual.getMetadata().get("mock"), is("mock"));
        assertThat(actual.getMetadata().get("a"), is("b"));
        verify(request).getParameter("mock");
    }
    
    @Test
    void testBuildWithIllegalWeight() throws NacosException {
        assertThrows(NacosException.class, () -> {
            when(request.getParameter("weight")).thenReturn("10001");
            Instance actual = builder.setRequest(request).build();
        });
    }
}
