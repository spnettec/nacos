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

package com.alibaba.nacos.airegistry.config;

import com.alibaba.nacos.core.web.NacosWebBean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

/**
 * Keeps shared backend services in the parent Core context.
 *
 * @author nacos
 */
public class AiRegistryBeanTypeFilter implements TypeFilter {
    
    @Override
    public boolean match(MetadataReader reader, MetadataReaderFactory factory) {
        AnnotationMetadata metadata = reader.getAnnotationMetadata();
        if (metadata.hasAnnotation(SpringBootApplication.class.getName())) {
            return true;
        }
        if (reader.getClassMetadata().getClassName().startsWith("com.alibaba.nacos.airegistry.")) {
            return false;
        }
        return !metadata.hasAnnotation(RestController.class.getName())
            && !metadata.hasAnnotation(Controller.class.getName())
            && !metadata.hasAnnotation(ControllerAdvice.class.getName())
            && !metadata.hasAnnotation(NacosWebBean.class.getName());
    }
}
