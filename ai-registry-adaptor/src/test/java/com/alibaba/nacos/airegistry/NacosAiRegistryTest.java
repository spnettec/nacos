/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.airegistry;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.mock.env.MockEnvironment;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link NacosAiRegistry}.
 *
 * @author nacos
 */
class NacosAiRegistryTest {
    
    @Test
    void testConstruct() {
        assertNotNull(new NacosAiRegistry());
    }
    
    @Test
    void testRegistryScanExcludesNestedApplicationEntrypoints() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.setEnvironment(new MockEnvironment()
            .withProperty("nacos.ai.mcp.registry.enabled", "true"));
        scanner.addIncludeFilter(new AnnotationTypeFilter(SpringBootApplication.class));
        assertTrue(scanner.findCandidateComponents("com.alibaba.nacos.naming").stream()
            .anyMatch(
                bean -> "com.alibaba.nacos.naming.NamingApp".equals(bean.getBeanClassName())));
        
        ComponentScan scan = NacosAiRegistry.class.getAnnotation(ComponentScan.class);
        assertNotNull(scan);
        assertTrue(Arrays.asList(scan.basePackages()).contains("com.alibaba.nacos.naming"));
        for (ComponentScan.Filter filter : scan.excludeFilters()) {
            for (Class<?> excluded : filter.classes()) {
                if (FilterType.ANNOTATION == filter.type()
                    && SpringBootApplication.class.equals(excluded)) {
                    scanner.addExcludeFilter(new AnnotationTypeFilter(SpringBootApplication.class));
                }
            }
        }
        assertTrue(scanner.findCandidateComponents("com.alibaba.nacos.naming").isEmpty());
        scanner.addIncludeFilter(new AnnotationTypeFilter(
            org.springframework.web.bind.annotation.RestController.class));
        assertFalse(scanner.findCandidateComponents("com.alibaba.nacos.airegistry").isEmpty());
    }
    
    @Test
    void testMainDelegatesToSpringApplication() {
        String[] args = {"--server.port=0"};
        try (MockedStatic<SpringApplication> springApplication = Mockito
            .mockStatic(SpringApplication.class)) {
            NacosAiRegistry.main(args);
            springApplication.verify(() -> SpringApplication.run(NacosAiRegistry.class, args));
        }
    }
}
