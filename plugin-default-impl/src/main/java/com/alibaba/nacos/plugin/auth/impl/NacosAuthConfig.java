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

package com.alibaba.nacos.plugin.auth.impl;

import com.alibaba.nacos.auth.config.AuthConfigs;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.code.ControllerMethodsCache;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthSystemTypes;
import com.alibaba.nacos.plugin.auth.impl.filter.JwtAuthenticationTokenFilter;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserDetailsServiceImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsUtils;

import javax.annotation.PostConstruct;

/**
 * Spring security config.
 *
 * @author Nacos
 */
@Configuration
@EnableMethodSecurity
public class NacosAuthConfig {
    
    private static final String SECURITY_IGNORE_URLS_SPILT_CHAR = ",";
    
    private static final String LOGIN_ENTRY_POINT = "/v1/auth/login";
    
    private static final String TOKEN_BASED_AUTH_ENTRY_POINT = "/v1/auth/**";
    
    private static final String DEFAULT_ALL_PATH_PATTERN = "/**";
    
    private static final String PROPERTY_IGNORE_URLS = "nacos.security.ignore.urls";
    
    private final Environment env;
    
    private final JwtTokenManager tokenProvider;
    
    private final AuthConfigs authConfigs;
    
    private final NacosUserDetailsServiceImpl userDetailsService;
    
    private final LdapAuthenticationProvider ldapAuthenticationProvider;
    
    private final ControllerMethodsCache methodsCache;
    
    public NacosAuthConfig(Environment env, JwtTokenManager tokenProvider, AuthConfigs authConfigs,
            NacosUserDetailsServiceImpl userDetailsService,
            ObjectProvider<LdapAuthenticationProvider> ldapAuthenticationProvider,
            ControllerMethodsCache methodsCache) {

        this.env = env;
        this.tokenProvider = tokenProvider;
        this.authConfigs = authConfigs;
        this.userDetailsService = userDetailsService;
        this.ldapAuthenticationProvider = ldapAuthenticationProvider.getIfAvailable();
        this.methodsCache = methodsCache;

    }
    
    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        methodsCache.initClassMethod("com.alibaba.nacos.plugin.auth.impl.controller");
    }
    
    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain authFilterChain(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        http.csrf().disable().cors(); // We don't need CSRF for JWT based authentication
        String ignoreUrls = null;
        if (AuthSystemTypes.NACOS.name().equalsIgnoreCase(authConfigs.getNacosAuthSystemType())) {
            ignoreUrls = DEFAULT_ALL_PATH_PATTERN;
        } else if (AuthSystemTypes.LDAP.name().equalsIgnoreCase(authConfigs.getNacosAuthSystemType())) {
            ignoreUrls = DEFAULT_ALL_PATH_PATTERN;
        }
        if (StringUtils.isBlank(authConfigs.getNacosAuthSystemType())) {
            ignoreUrls = env.getProperty(PROPERTY_IGNORE_URLS, DEFAULT_ALL_PATH_PATTERN);
        }

        if (StringUtils.isNotBlank(ignoreUrls)) {
            for (String each : ignoreUrls.trim().split(SECURITY_IGNORE_URLS_SPILT_CHAR)) {
                http.authorizeHttpRequests().requestMatchers(AntPathRequestMatcher.antMatcher(each.trim()))
                        .permitAll();
            }
        }
        if (StringUtils.isBlank(authConfigs.getNacosAuthSystemType())) {
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                    .authorizeHttpRequests().requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                    .requestMatchers(AntPathRequestMatcher.antMatcher(LOGIN_ENTRY_POINT)).permitAll().and().authorizeHttpRequests()
                    .requestMatchers(AntPathRequestMatcher.antMatcher(TOKEN_BASED_AUTH_ENTRY_POINT)).authenticated().and().exceptionHandling()
                    .authenticationEntryPoint(new JwtAuthenticationEntryPoint());
            // disable cache
            http.headers().cacheControl();
            
            http.addFilterBefore(new JwtAuthenticationTokenFilter(tokenProvider),
                    UsernamePasswordAuthenticationFilter.class);
        }

        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        if (AuthSystemTypes.NACOS.name().equalsIgnoreCase(authConfigs.getNacosAuthSystemType())) {
            authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        } else if (AuthSystemTypes.LDAP.name().equalsIgnoreCase(authConfigs.getNacosAuthSystemType())) {
            authenticationManagerBuilder.authenticationProvider(ldapAuthenticationProvider);
        }

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
