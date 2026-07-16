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

package com.alibaba.nacos.persistence.utils;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.persistence.constants.PersistenceConstant;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.core.env.Environment;

import java.util.Locale;

/**
 * get datasource platform util.
 *
 * @author lixiaoshuang
 */
public class DatasourcePlatformUtil {

    private static final String DATASOURCE_TYPE_PROPERTY = "DB_TYPE";

    private static final String EMPTY_DATASOURCE_TYPE = "NONE";

    private static final String PLATFORM_PROPERTY_SUFFIX = ".PLATFORM";
    
    /**
     * get datasource platform.
     *
     * @param defaultPlatform default platform.
     * @return
     */
    public static String getDatasourcePlatform(String defaultPlatform) {
        String platform = EnvUtil.getProperty(PersistenceConstant.DATASOURCE_PLATFORM_PROPERTY_NEW);
        if (StringUtils.isBlank(platform)) {
            platform = EnvUtil.getProperty(PersistenceConstant.DATASOURCE_PLATFORM_PROPERTY);
        }
        if (StringUtils.isBlank(platform)) {
            platform = EnvUtil.getProperty(DATASOURCE_TYPE_PROPERTY);
        }
        return normalizeDatasourcePlatform(platform, defaultPlatform);
    }

    /**
     * get datasource platform from Spring Environment.
     * @param environment environment
     * @param defaultPlatform default platform
     * @return datasource platform
     */
    public static String getDatasourcePlatform(Environment environment, String defaultPlatform) {
        String platform = environment.getProperty(PersistenceConstant.DATASOURCE_PLATFORM_PROPERTY_NEW);
        if (StringUtils.isBlank(platform)) {
            platform = environment.getProperty(PersistenceConstant.DATASOURCE_PLATFORM_PROPERTY);
        }
        if (StringUtils.isBlank(platform)) {
            platform = environment.getProperty(DATASOURCE_TYPE_PROPERTY);
        }
        if (StringUtils.isBlank(platform) || platform.contains("${")) {
            String dataSourceType = environment.getProperty(DATASOURCE_TYPE_PROPERTY);
            if (StringUtils.isNotBlank(dataSourceType) && !EMPTY_DATASOURCE_TYPE.equalsIgnoreCase(dataSourceType)) {
                platform = environment.getProperty(dataSourceType + PLATFORM_PROPERTY_SUFFIX);
            }
        }
        return normalizeDatasourcePlatform(platform, defaultPlatform);
    }

    private static String normalizeDatasourcePlatform(String platform, String defaultPlatform) {
        if (StringUtils.isBlank(platform) || EMPTY_DATASOURCE_TYPE.equalsIgnoreCase(platform)) {
            return defaultPlatform;
        }
        return platform.toLowerCase(Locale.ROOT);
    }
}
