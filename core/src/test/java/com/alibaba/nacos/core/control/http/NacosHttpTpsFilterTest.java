/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.nacos.core.control.http;

import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.core.code.ControllerMethodsCache;
import com.alibaba.nacos.core.remote.HealthCheckRequestHandler;
import com.alibaba.nacos.plugin.control.ControlManagerCenter;
import com.alibaba.nacos.plugin.control.tps.TpsControlManager;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import org.apache.catalina.core.AsyncContextImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NacosHttpTpsFilterTest {
    
    NacosHttpTpsFilter nacosHttpTpsFilter;
    
    MockedStatic<ControlManagerCenter> controlManagerCenterMockedStatic;
    
    @Mock
    ControllerMethodsCache controllerMethodsCache;
    
    @Mock
    private ControlManagerCenter controlManagerCenter;
    
    @Mock
    private TpsControlManager tpsControlManager;
    
    @BeforeEach
    void before() {
        controlManagerCenterMockedStatic = Mockito.mockStatic(ControlManagerCenter.class);
        controlManagerCenterMockedStatic.when(() -> ControlManagerCenter.getInstance()).thenReturn(controlManagerCenter);
        when(controlManagerCenter.getTpsControlManager()).thenReturn(tpsControlManager);
        nacosHttpTpsFilter = new NacosHttpTpsFilter(controllerMethodsCache);
        
    }
    
    @AfterEach
    void after() {
        controlManagerCenterMockedStatic.close();
    }
    
    /**
     * test tps check passed ,response is null.
     */
    @Test
    void testPass() throws Exception {
        HttpTpsCheckRequestParserRegistry.register(new HttpTpsCheckRequestParser() {
            @Override
            public TpsCheckRequest parse(HttpServletRequest httpServletRequest) {
                return new TpsCheckRequest();
            }
            
            @Override
            public String getPointName() {
                return "HealthCheck";
            }
            
            @Override
            public String getName() {
                return "HealthCheck";
            }
        });
        
        TpsCheckResponse tpsCheckResponse = new TpsCheckResponse(true, 200, "success");
        when(tpsControlManager.check(any(TpsCheckRequest.class))).thenReturn(tpsCheckResponse);
        
        //mock http tps control method
        Method method = HealthCheckRequestHandler.class.getMethod("handle", Request.class, RequestMeta.class);
        
        MockHttpServletRequest httpServletRequest = Mockito.mock(MockHttpServletRequest.class);
        MockHttpServletResponse httpServletResponse = Mockito.mock(MockHttpServletResponse.class);
        MockFilterChain filterChain = Mockito.mock(MockFilterChain.class);
        when(controllerMethodsCache.getMethod(eq(httpServletRequest))).thenReturn(method);
        Mockito.doNothing().when(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        //execute test.
        nacosHttpTpsFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        
        //verify
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(httpServletRequest, httpServletResponse);
        
    }
    
    /**
     * test tps check rejected ,response is not null.
     */
    @Test
    void testRejected() throws Exception {
        HttpTpsCheckRequestParserRegistry.register(new HttpTpsCheckRequestParser() {
            @Override
            public TpsCheckRequest parse(HttpServletRequest httpServletRequest) {
                return new TpsCheckRequest();
            }
            
            @Override
            public String getPointName() {
                return "HealthCheck";
            }
            
            @Override
            public String getName() {
                return "HealthCheck";
            }
        });
        
        TpsCheckResponse tpsCheckResponse = new TpsCheckResponse(false, 5031, "rejected");
        when(tpsControlManager.check(any(TpsCheckRequest.class))).thenReturn(tpsCheckResponse);
        
        //mock http tps control method
        Method method = HealthCheckRequestHandler.class.getMethod("handle", Request.class, RequestMeta.class);
        
        MockHttpServletRequest httpServletRequest = Mockito.mock(MockHttpServletRequest.class);
        MockHttpServletResponse httpServletResponse = Mockito.mock(MockHttpServletResponse.class);
        MockFilterChain filterChain = Mockito.mock(MockFilterChain.class);
        when(controllerMethodsCache.getMethod(eq(httpServletRequest))).thenReturn(method);
        AsyncContextImpl asyncContext = Mockito.mock(AsyncContextImpl.class);
        Mockito.when(httpServletRequest.startAsync()).thenReturn(asyncContext);
        //execute test.
        nacosHttpTpsFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        
        //verify
        Mockito.verify(filterChain, Mockito.times(0)).doFilter(any(), any());
        Thread.sleep(1100L);
        Mockito.verify(httpServletResponse, Mockito.times(1)).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        
    }
    
    /**
     * test tps check exception ,return null skip.
     */
    @Test
    void testTpsCheckException() throws Exception {
        HttpTpsCheckRequestParserRegistry.register(new HttpTpsCheckRequestParser() {
            @Override
            public TpsCheckRequest parse(HttpServletRequest httpServletRequest) {
                return new TpsCheckRequest();
            }
            
            @Override
            public String getPointName() {
                return "HealthCheck";
            }
            
            @Override
            public String getName() {
                return "HealthCheck";
            }
        });
        
        when(tpsControlManager.check(any(TpsCheckRequest.class))).thenThrow(new RuntimeException("324565"));
        
        //mock http tps control method
        Method method = HealthCheckRequestHandler.class.getMethod("handle", Request.class, RequestMeta.class);
        
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        MockFilterChain filterChain = Mockito.mock(MockFilterChain.class);
        
        when(controllerMethodsCache.getMethod(eq(httpServletRequest))).thenReturn(method);
        //execute test.
        nacosHttpTpsFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        
        //verify
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(httpServletRequest, httpServletResponse);
        
    }
}
