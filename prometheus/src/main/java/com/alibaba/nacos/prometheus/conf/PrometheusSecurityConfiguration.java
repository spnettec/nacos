/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.prometheus.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static com.alibaba.nacos.prometheus.api.ApiConstants.*;

/**
 * prometheus auth configuration, avoid spring security configuration override.
 *
 * @author vividfish
 */
@Configuration
public class PrometheusSecurityConfiguration {

    @Bean
    public WebSecurityCustomizer prometheusWebSecurityCustomizer() {
        return web -> {
            web.ignoring().requestMatchers(AntPathRequestMatcher.antMatcher(PROMETHEUS_CONTROLLER_PATH),
                    AntPathRequestMatcher.antMatcher(PROMETHEUS_CONTROLLER_NAMESPACE_PATH),
                    AntPathRequestMatcher.antMatcher(PROMETHEUS_CONTROLLER_SERVICE_PATH));
        };
    }

}
